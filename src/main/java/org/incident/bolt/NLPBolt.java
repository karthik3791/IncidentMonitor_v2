package org.incident.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.incident.monitor.Email;
import org.incident.monitor.Incident;
import org.incident.monitor.IncidentMonitorConstants;
import org.incident.utils.TextTools;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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
			processEmail(unstructuredMail, this.pipeline);

//			Annotation document = unstructuredMail.getDisplayFrom().startsWith("Ganesh")
//					? new Annotation(unstructuredMail.getSubject()) : null;
			//
			// if (document != null) {
//				List<Incident> incidents = processEmailWithNLP(document, this.pipeline);
//				for (Incident incident : incidents) {
//					unstructuredMail.addIncident(incident);
//				}
//			}

			System.out.println("NLP Email Created !");
			collector.emit("structuredNLPMail", new Values(unstructuredMail));
		}

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("structuredNLPMail", new Fields("email"));
	}

	private void processEmail(Email unstructuredMail, StanfordCoreNLP pipeline) {
		String subject = unstructuredMail.getSubject(), body = unstructuredMail.getBody();
		List<Incident> combinedIncidents = new ArrayList<Incident>();
		
		if (StringUtils.isNotBlank(subject)) {
			combinedIncidents.addAll(getIncidents(subject, pipeline, unstructuredMail.getMessageDate()));
		}
		if (StringUtils.isNotBlank(body))
			combinedIncidents.addAll(getIncidents(subject, pipeline, ""));

		for (Incident i : combinedIncidents) {
			unstructuredMail.addIncident(i);
		}
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

	private Incident getIncident(CoreMap sentence, String defaultDate) {
		String finalDate, finalLocation, finalName;

		List<ArrayList<String>> incidentInfo = parseIncidentInfo(sentence);
		ArrayList<String> dateParts = incidentInfo.get(0);
		ArrayList<String> locationParts = incidentInfo.get(1);
		ArrayList<String> nameParts = incidentInfo.get(2);

		// No incident created if no location found
		if (locationParts.size() == 0) {
			return null;
		}

		finalLocation = TextTools.createStringFromList(locationParts);

		// Priority to parsed date in content, else use default date if
		// available
		finalDate = dateParts.size() > 0 ? TextTools.createStringFromList(dateParts)
				: (StringUtils.isNotBlank(defaultDate) ? defaultDate : null);

		// No incident created if no date assigned
		if (StringUtils.isBlank(finalDate)) {
			return null;
		}

		// Priority to parsed name, else use shortened sentence
		finalName = nameParts.size() > 0 ? TextTools.createStringFromList(nameParts)
				: sentence.toShorterString("PartOfSpeech");

		return new Incident(finalDate, finalLocation, finalName);
	}

	private List<ArrayList<String>> parseIncidentInfo(CoreMap sentence) {
		List<String> dateParts = new ArrayList<String>(), locationParts = new ArrayList<String>(),
				nameParts = new ArrayList<String>();

		List<ArrayList<String>> incidentInfo = new ArrayList<ArrayList<String>>();

		String prevWord = "", prevNamedEntity = IncidentMonitorConstants.NLPUnknownEntityIdentifier;
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String currWord = token.get(TextAnnotation.class);
			String currNamedEntity = token.get(NamedEntityTagAnnotation.class);
			if (currNamedEntity.equals(IncidentMonitorConstants.NLPDateEntityIdentifier)) {
				dateParts.add(currWord);
			} else if (currNamedEntity.equals(IncidentMonitorConstants.NLPLocationEntityIdentifier)) {
				// handle cases when location parts contains num that fall
				// within date range
				// eg. Fire at 2 Duxton Road
				if (prevNamedEntity.equals(IncidentMonitorConstants.NLPDateEntityIdentifier)) {
					dateParts.remove(prevWord);
					locationParts.add(prevWord);
				}
				locationParts.add(currWord);
			} else {
				String pos = token.get(PartOfSpeechAnnotation.class);
				if (currNamedEntity.equals(IncidentMonitorConstants.NLPUnknownEntityIdentifier)
						&& pos.matches(IncidentMonitorConstants.NLPNounEntityIdentifier)) {
					nameParts.add(currWord);
				}
			}
			prevWord = currWord;
			prevNamedEntity = currNamedEntity;
		}

		incidentInfo.add((ArrayList<String>) dateParts);
		incidentInfo.add((ArrayList<String>) locationParts);
		incidentInfo.add((ArrayList<String>) nameParts);

		return incidentInfo;
	}

	private List<CoreMap> getAnnotatedSentences(String content, StanfordCoreNLP pipeline) {
		try {
			Annotation document = new Annotation(content);
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
