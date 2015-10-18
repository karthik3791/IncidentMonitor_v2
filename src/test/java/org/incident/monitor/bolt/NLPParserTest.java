package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.incident.bolt.NLPBolt;
import org.incident.monitor.Incident;
import org.junit.Test;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import junit.framework.TestSuite;

public class NLPParserTest extends TestSuite {
	
	private NLPBolt nlpTest = new NLPBolt();
	Properties props = new Properties();
	private StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	public NLPParserTest() {
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	}

	public List<Incident> createSimpleNameLocDate(String testString) {
		return nlpTest.processEmailWithNLP(new Annotation(testString), this.pipeline);
	}

	@Test
	public void testSimpleNameLocDate() {
		List<Incident> i = createSimpleNameLocDate("Power outage in Singapore on 20 October 2011");
		assertEquals(1, i.size());
		assertEquals("20 October 2011", i.get(0).getDate());
	}

	@Test
	public void testComplexNameLocDate() {
		List<Incident> i = createSimpleNameLocDate(
				"Power outage in Singapore on 20 October 2011. Fire reported at North Carolina today.");
		assertEquals(2, i.size());
		assertEquals("North Carolina", i.get(1).getLocation());
	}

}
