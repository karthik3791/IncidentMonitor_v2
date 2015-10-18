package org.incident.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.incident.monitor.Email;
import org.incident.monitor.Incident;
import org.incident.utils.TextTools;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
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
	private StanfordCoreNLP pipeline;
	private OutputCollector collector;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
		this.collector = collector;
	}

	public void execute(Tuple input) {
		if (input.getSourceStreamId().equals("unstructuredMail")) {
			Email unstructuredMail = (Email) input.getValue(0);
			// System.out.println(rawEmail);
			Annotation document = unstructuredMail.getDisplayFrom().startsWith("Ganesh")
					? new Annotation(unstructuredMail.getSubject())
					: null;

			if (document != null) {
				List<Incident> incidents = processEmailWithNLP(document, this.pipeline);
				for (Incident incident : incidents) {
					unstructuredMail.addIncident(incident);
				}
			}
		}

	}

	public List<Incident> processEmailWithNLP(Annotation document, StanfordCoreNLP pipeline) {
		// run all Annotators on this text

		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as
		// keys
		// and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		List<Incident> incidents = new ArrayList<Incident>();
		for (CoreMap sentence : sentences) {
			// System.out.println(sentence.get(NamedEntityTagAnnotation.class));
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific
			// methods
			List<String> dateParts = new ArrayList<String>(), locationParts = new ArrayList<String>(),
					nameParts = new ArrayList<String>();
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);

				if (ne.equals("DATE")) {
					dateParts.add(word);
				} else if (ne.equals("LOCATION")) {
					locationParts.add(word);
				} else {
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);
					if (ne.equals("O") && pos.matches("NN.*")) {
						nameParts.add(word);
					}
				}
			}
			String finalDate = null, finalLocation = null, finalName = null;
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

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

}
