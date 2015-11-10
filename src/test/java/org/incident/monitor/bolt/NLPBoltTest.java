package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.incident.bolt.NLPBolt;
import org.incident.monitor.Incident;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NLPBoltTest {

	private NLPBolt nlpTest;
	private Properties props;
	private StanfordCoreNLP pipeline;

	public NLPBoltTest() {
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
		nlpTest = new NLPBolt();
	}

	public List<Incident> checkGetIncidents(String testString, String defaultDate) {
		return nlpTest.getIncidents(testString, this.pipeline, defaultDate);
	}

	public void assertDateandLocation(Incident expectedIncident, Incident actualIncident) {
		assertEquals(expectedIncident.getLocation(), actualIncident.getLocation());
		assertEquals(expectedIncident.getDate(), actualIncident.getDate());
	}

	public void assertIncident(Incident expectedIncident, Incident actualIncident) {
		assertEquals(expectedIncident.getLocation(), actualIncident.getLocation());
		assertEquals(expectedIncident.getDate(), actualIncident.getDate());
		assertEquals(expectedIncident.getName(), actualIncident.getName());
	}
}
