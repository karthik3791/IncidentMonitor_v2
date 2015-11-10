package org.incident.bolt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.incident.monitor.Email;
import org.incident.monitor.Incident;
import org.incident.monitor.IncidentMonitorConstants;
import org.incident.utils.NLPParsedOutput;
import org.incident.utils.TextTools;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class NLPBolt extends BaseRichBolt {

	private static final long serialVersionUID = 1L;
	private StanfordCoreNLP pipeline;
	private OutputCollector collector;

	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,
			OutputCollector collector) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
		this.collector = collector;
	}

	public void execute(Tuple input) {
		if (input.getSourceStreamId().equals("unstructuredMail")) {
			Email unstructuredMail = (Email) input.getValue(0);
			if (processEmail(unstructuredMail, this.pipeline)) {
				System.out.println("NLP Email Created !");
				collector.emit("structuredNLPMail", new Values(unstructuredMail));
			} else {
				System.out.println("No incidents Found");
			}
		}

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("structuredNLPMail", new Fields("email"));
	}

	private boolean processEmail(Email unstructuredMail, StanfordCoreNLP pipeline) {
		boolean status = false;
		String subject = unstructuredMail.getSubject(), body = unstructuredMail.getBody();
		List<Incident> combinedIncidents = new ArrayList<Incident>();

		if (StringUtils.isNotBlank(subject))
			combinedIncidents.addAll(getIncidents(subject, pipeline, unstructuredMail.getMessageDate()));

		if (StringUtils.isNotBlank(body))
			combinedIncidents.addAll(getIncidents(body, pipeline, ""));

		if (combinedIncidents.size() > 0) {
			status = true;
			for (Incident i : combinedIncidents) {
				unstructuredMail.addIncident(i);
			}
		}

		return status;
	}

	/*
	 * Following function is public for testing
	 */
	public List<Incident> getIncidents(String content, StanfordCoreNLP pipeline, String defaultDate) {
		List<Incident> incidents = new ArrayList<Incident>();
		List<CoreMap> sentences = getAnnotatedSentences(content, pipeline);

		if (sentences == null || sentences.isEmpty()) {
			return incidents;
		}

		for (CoreMap sentence : sentences) {
			Incident parsedIncident = getIncident(sentence, defaultDate);
			if (parsedIncident != null)
				incidents.add(parsedIncident);
		}
		return incidents;
	}

	private String getDate(NLPParsedOutput output, String defaultDate) {
		String date = TextTools.getSortedValuesFromMap(output.getDateMap());
		return StringUtils.isBlank(date) ? defaultDate : date;
	}

	private String getLocation(NLPParsedOutput output) {

		Map<Integer, String> combinedLocationMap = new HashMap<Integer, String>();
		combinedLocationMap.putAll(output.getLocationMap());
		combinedLocationMap.putAll(output.getOrganizationPreceededByPrepositionMap());

		String finalLocation = TextTools.getSortedValuesFromMap(combinedLocationMap);
		if (!StringUtils.isBlank(finalLocation)) {
			return finalLocation;
		}

		finalLocation = TextTools.getSortedValuesFromMap(output.getOrganizationNotPreceededByPrepositionMap());
		// clear the map so that eventName does not copy same details when
		// already used by Location
		output.resetPlainOrganizationPart();
		return StringUtils.isBlank(finalLocation) ? null : finalLocation;
	}

	private String getEventNameForUnstructuredSentence(NLPParsedOutput output) {
		Map<Integer, String> combinedLocationMap = new HashMap<Integer, String>();
		combinedLocationMap.putAll(output.getAdjectiveMap());
		combinedLocationMap.putAll(output.getVerbMap());
		combinedLocationMap.putAll(output.getNounMap());
		combinedLocationMap.putAll(output.getOrganizationNotPreceededByPrepositionMap());

		String finalEventName = TextTools.getSortedValuesFromMap(combinedLocationMap);

		return StringUtils.isBlank(finalEventName) ? null : finalEventName;

	}

	private String getEventNameForStructuredSentence(NLPParsedOutput output) {

		String finalVerb = null, lemmaFinalVerb = null, finalNoun = null;

		List<String> nsubjVerbList = new ArrayList<String>();
		List<String> nsubjVerbLemmaList = new ArrayList<String>();
		List<String> aftVerbComponents = new ArrayList<String>();
		List<String> beforeNounComponents = new ArrayList<String>();

		Collection<TypedDependency> td = output.getTypedDependencyList();

		// 1.Check if its a normal well constructed email with verbs.
		Object[] list = td.toArray();
		TypedDependency typedDependency;
		for (Object object : list) {
			typedDependency = (TypedDependency) object;
			if (typedDependency.reln().toString().matches(IncidentMonitorConstants.NLPSubjectIdentifier)) {
				if (typedDependency.gov().tag().matches(IncidentMonitorConstants.NLPVerbEntityIdentifier)) {
					nsubjVerbList.add(typedDependency.gov().originalText());
					nsubjVerbLemmaList.add(typedDependency.gov().lemma());
				}
			}
		}

		if (nsubjVerbList.size() == 0 || nsubjVerbList.size() > 2) {
			return getEventNameForUnstructuredSentence(output);
		} else if (nsubjVerbList.size() == 2) {
			for (Object object : list) {
				typedDependency = (TypedDependency) object;
				if (typedDependency.reln().toString().matches(IncidentMonitorConstants.NLPCCompId)) {
					if (typedDependency.gov().originalText().matches(nsubjVerbList.get(0))) {
						finalVerb = nsubjVerbList.get(1);
						lemmaFinalVerb = nsubjVerbLemmaList.get(1);
						break;
					} else {
						finalVerb = nsubjVerbList.get(0);
						lemmaFinalVerb = nsubjVerbLemmaList.get(0);
						break;
					}
				}
			}
		} else {
			finalVerb = nsubjVerbList.get(0);
			lemmaFinalVerb = nsubjVerbLemmaList.get(0);
		}

		// Check for xcomp and dobj associated with the Final Verb.
		for (Object object : list) {
			typedDependency = (TypedDependency) object;
			if ((typedDependency.reln().toString().matches(IncidentMonitorConstants.NLPXCompId)
					|| typedDependency.reln().toString().matches(IncidentMonitorConstants.NLPDObj))
					&& typedDependency.gov().originalText().equals(finalVerb)) {
				aftVerbComponents.add(typedDependency.dep().originalText());
			}
		}

		// Find main noun.
		for (Object object : list) {
			typedDependency = (TypedDependency) object;
			if (typedDependency.reln().toString().matches(IncidentMonitorConstants.NLPSubjectIdentifier)
					&& typedDependency.gov().originalText().equals(finalVerb)) {
				finalNoun = typedDependency.dep().originalText();
			}
		}

		// Find the Before Main Noun component.
		for (Object object : list) {
			typedDependency = (TypedDependency) object;
			if (typedDependency.reln().toString().matches(IncidentMonitorConstants.beforeNounComponents)
					&& typedDependency.gov().originalText().equals(finalNoun)) {
				beforeNounComponents.add(typedDependency.dep().originalText());
			}
		}

		List<String> compiledList = new ArrayList<String>();
		compiledList.addAll(beforeNounComponents);
		compiledList.add(finalNoun);
		compiledList.add(lemmaFinalVerb);
		compiledList.addAll(aftVerbComponents);

		// Now construct the Event Name using all these components
		return TextTools.createStringFromList(compiledList);

	}

	private Incident getIncident(CoreMap sentence, String defaultDate) {
		String finalDate, finalLocation, finalEventName;

		NLPParsedOutput nlpout = getNLPParsedOutput(sentence);
		finalLocation = getLocation(nlpout);

		if (StringUtils.isBlank(finalLocation)) {
			return null;
		}
		finalDate = getDate(nlpout, defaultDate);
		if (StringUtils.isBlank(finalDate)) {
			return null;
		}
		finalEventName = getEventNameForStructuredSentence(nlpout);
		return StringUtils.isBlank(finalEventName) ? null : new Incident(finalEventName, finalDate, finalLocation);
	}

	/*
	 * Linguistic Rules for First occurrence of Organization. 1. A sentence with
	 * Organization and no Location will treat Organization as Location. 2. A
	 * sentence with Organization preceded by a preposition will append the
	 * Organization to Location (irrespective of whether another Location is
	 * there or not). 3. A sentence with Organization NOT preceded by a
	 * preposition and with another Location will not include this Organization
	 * into the Location Part. We should add such an organization to the Event
	 * Name Part.
	 * 
	 * Linguistic Rules for Location 1. A number preceding a Location will be
	 * added to the Location. 2. MARCH Special case. Can be tagged as date but
	 * if not preceded by a preposition then consider it part of NameEvent.
	 * 
	 * Linguistic Rules for NAME Parts : - An adjective in the sentence + A noun
	 * that is neither LOCATION nor DATE nor an ORGANIZATION preceded by
	 * preposition + lemmatized form of the verb in the sentence. *
	 */

	private NLPParsedOutput getNLPParsedOutput(CoreMap sentence) {
		NLPParsedOutput nlpout = new NLPParsedOutput();
		String prevWord = "", prevNamedEntity = IncidentMonitorConstants.NLPUnknownEntityIdentifier;
		boolean prevPrepositionflag = false, prevDeterminantFlag = false;
		int wordpos = 0;
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			wordpos++;
			String currWord = token.get(TextAnnotation.class);
			String currNamedEntity = token.get(NamedEntityTagAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);

			if (pos.equals(IncidentMonitorConstants.NLPPrepositionIdentifier)) {
				prevPrepositionflag = true;
				continue;
			}

			if (pos.equals(IncidentMonitorConstants.NLPDeterminantIdentifier)) {
				prevDeterminantFlag = true;
				continue;
			}
			if (pos.matches(IncidentMonitorConstants.NLPUnwantedIdentifiers)) {
				continue;
			}

			if (currNamedEntity.equals(IncidentMonitorConstants.NLPDateEntityIdentifier)) {

				// For March spl case
				// If MARCH and preposition flag is false then NOUN else DATE
				if (currWord.toUpperCase().equals(IncidentMonitorConstants.MarchConstant)
						&& prevDeterminantFlag == true) {
					nlpout.addNounPart(wordpos, currWord);
					currNamedEntity = IncidentMonitorConstants.NLPUnknownEntityIdentifier;
				} else if (prevNamedEntity.equals(IncidentMonitorConstants.NLPDateEntityIdentifier)
						|| nlpout.getDateMap().size() == 0)
					nlpout.addDatePart(wordpos, currWord);
				else
					currNamedEntity = IncidentMonitorConstants.NLPUnknownEntityIdentifier;

				prevPrepositionflag = false;
				prevDeterminantFlag = false;
			} else if (currNamedEntity.equals(IncidentMonitorConstants.NLPLocationEntityIdentifier)) {

				if (prevNamedEntity.equals(IncidentMonitorConstants.NLPNumberIdentifier))
					nlpout.addLocationMap(wordpos - 1, prevWord);

				if (prevNamedEntity.equals(IncidentMonitorConstants.NLPDateEntityIdentifier)) {
					nlpout.getDateMap().remove(wordpos - 1);
					nlpout.addLocationMap(wordpos - 1, prevWord);
				}

				nlpout.addLocationMap(wordpos, currWord);

				prevPrepositionflag = false;
				prevDeterminantFlag = false;

			} else if (currNamedEntity.equals(IncidentMonitorConstants.NLPNumberIdentifier)) {

				if (prevNamedEntity.equals(IncidentMonitorConstants.NLPLocationEntityIdentifier)) {
					nlpout.addLocationMap(wordpos, currWord);
					currNamedEntity = IncidentMonitorConstants.NLPLocationEntityIdentifier;
				}

				// prevPrepositionflag = false;
				prevDeterminantFlag = false;

			} else if (currNamedEntity.equals(IncidentMonitorConstants.NLPOrganizationEntityIdentifier)) {

				if (prevPrepositionflag) {
					if (prevNamedEntity.equals(IncidentMonitorConstants.NLPOrganizationEntityIdentifier)
							|| nlpout.getOrganizationPreceededByPrepositionMap().size() == 0) {

						if (prevNamedEntity.equals(IncidentMonitorConstants.NLPNumberIdentifier))
							nlpout.addOrganizationPreceededByPreposition(wordpos - 1, prevWord);

						nlpout.addOrganizationPreceededByPreposition(wordpos, currWord);
					} else
						currNamedEntity = IncidentMonitorConstants.NLPUnknownEntityIdentifier;
				} else {

					if (prevNamedEntity.equals(IncidentMonitorConstants.NLPNumberIdentifier))
						nlpout.addPlainOrganizationPart(wordpos - 1, prevWord);

					nlpout.addPlainOrganizationPart(wordpos, currWord);
				}

				prevDeterminantFlag = false;

			} else if (currNamedEntity.matches(IncidentMonitorConstants.NLPNEROtherIdentifiers)) {

				if (pos.matches(IncidentMonitorConstants.NLPNounEntityIdentifier)
						&& !currWord.toUpperCase().matches(IncidentMonitorConstants.NounFilters)) {
					nlpout.addNounPart(wordpos, currWord);
				} else if (pos.matches(IncidentMonitorConstants.NLPVerbEntityIdentifier)) {
					String lemmaVerb = token.get(LemmaAnnotation.class);
					nlpout.addVerbPart(wordpos, lemmaVerb);
				} else if (pos.matches(IncidentMonitorConstants.NLPAdjectiveEntityIdentifier)) {
					nlpout.addAdjectivePart(wordpos, currWord);
				}
				prevPrepositionflag = false;
				prevDeterminantFlag = false;
			} else {
				continue;
			}
			prevWord = currWord;
			prevNamedEntity = currNamedEntity;
		}

		return enrichParsedOutputWithDependencyTree(sentence, nlpout);
	}

	private NLPParsedOutput enrichParsedOutputWithDependencyTree(CoreMap sentence, NLPParsedOutput nlp) {
		Tree tree = sentence.get(TreeAnnotation.class);
		// Get dependency tree
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
		nlp.setTypedDependencyList(td);
		return nlp;
	}

	private List<CoreMap> getAnnotatedSentences(String content, StanfordCoreNLP pipeline) {
		try {
			String content_refine = content.replaceAll("[–]", ",");
			Annotation document = new Annotation(content_refine);
			pipeline.annotate(document);
			return document.get(SentencesAnnotation.class);
		} catch (Exception ex) {
			System.out.println("[ERROR] Failure to annotate and retrieve sentences.");
			ex.printStackTrace();
			return null;
		}
	}

	/*
	 * Older version of code
	 */

	public List<Incident> processEmailWithNLP(Annotation document, StanfordCoreNLP pipeline) {
		List<Incident> incidents = new ArrayList<Incident>();

		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			List<String> dateParts = new ArrayList<String>(), locationParts = new ArrayList<String>(),
					nameParts = new ArrayList<String>();
			String finalDate = null, finalLocation = null, finalName = null;

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				String ne = token.get(NamedEntityTagAnnotation.class);
				if (ne.equals("DATE")) {
					dateParts.add(word);
				} else if (ne.equals("LOCATION")) {
					locationParts.add(word);
				} else {
					String pos = token.get(PartOfSpeechAnnotation.class);
					if (ne.equals("O") && pos.matches("NN.*")) {
						nameParts.add(word);
					}
				}
			}

			if (dateParts.size() > 0) {
				finalDate = TextTools.createStringFromList(dateParts);
			}
			if (locationParts.size() > 0) {
				finalLocation = TextTools.createStringFromList(locationParts);
			}
			if (nameParts.size() > 0) {
				finalName = TextTools.createStringFromList(nameParts);
			}

			if (!finalDate.isEmpty() && !finalLocation.isEmpty() && !finalName.isEmpty()) {
				incidents.add(new Incident(finalName, finalDate, finalLocation));
			}
		}
		return incidents;
	}

}
