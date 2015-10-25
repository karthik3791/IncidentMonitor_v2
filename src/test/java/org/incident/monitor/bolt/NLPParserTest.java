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

	private NLPBolt nlpTest;
	private Properties props;
	private StanfordCoreNLP pipeline;

	public NLPParserTest() {
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
		nlpTest = new NLPBolt();
	}

	public List<Incident> createSimpleNameLocDate(String testString) {
		return nlpTest.processEmailWithNLP(new Annotation(testString), this.pipeline);
	}

	public List<Incident> checkGetIncidents(String testString, String defaultDate) {
		return nlpTest.getIncidents(testString, this.pipeline, defaultDate);
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

	@Test
	public void testSubjectWithNoLocation() {
		List<Incident> i = checkGetIncidents("Power outage today.", "2015-01-01");
		assertEquals(0, i.size());
	}

	@Test
	public void testSubjectWithDateLocationEvent() {
		List<Incident> i = checkGetIncidents("Power outage in Singapore on 20 October 2011", "2015-01-01");

		assertEquals(1, i.size());
		assertEquals("Singapore", i.get(0).getLocation());
		assertEquals("20 October 2011", i.get(0).getDate());
		assertEquals("Power outage", i.get(0).getName());
	}

	@Test
	public void testSubjectWithNoDate() {
		List<Incident> i = checkGetIncidents("Power Outage in Singapore", "2015-01-01");
		assertEquals(1, i.size());
		assertEquals("Singapore", i.get(0).getLocation());
		assertEquals("2015-01-01", i.get(0).getDate());
		assertEquals("Power Outage", i.get(0).getName());
	}

	@Test
	public void testSubjectWithNoDate2() {
		List<Incident> i = checkGetIncidents("Fire in Singapore at Police Department Headquarters.", "2015-01-01");
		assertEquals(1, i.size());
		assertEquals("Singapore", i.get(0).getLocation());
		assertEquals("2015-01-01", i.get(0).getDate());
		assertEquals("Power Outage", i.get(0).getName());
	}

	@Test
	public void testSubjectWithNoDateNoEvent() {
		List<Incident> i = checkGetIncidents("Flooded Singapore reported.", "2015-01-01");
		assertEquals(1, i.size());
		assertEquals("Singapore", i.get(0).getLocation());
		assertEquals("2015-01-01", i.get(0).getDate());
		assertEquals("Flooded Singapore reported", i.get(0).getName());
	}

	@Test
	public void testSubjectMultipleSentences() {
		List<Incident> i = checkGetIncidents(
				"Power outage in Singapore on 20 October 2011. Fire reported at North Carolina. Power outage today.",
				"2015-01-01");
		assertEquals(2, i.size());
		assertEquals("Singapore", i.get(0).getLocation());
		assertEquals("20 October 2011", i.get(0).getDate());
		assertEquals("Power outage", i.get(0).getName());

		assertEquals("North Carolina", i.get(1).getLocation());
		assertEquals("2015-01-01", i.get(1).getDate());
		assertEquals("Fire", i.get(1).getName());
	}

	@Test
	public void testSubjectWithDateBeforeLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 30 Brooklyn Avenue, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertEquals("30 Brooklyn Avenue America", i.get(0).getLocation());
		assertEquals("2015-01-01", i.get(0).getDate());
		assertEquals("Fire", i.get(0).getName());
	}

	@Test
	public void testSubjectWithNumberBeforeLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 300 Brooklyn Avenue, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertEquals("300 Brooklyn Avenue America", i.get(0).getLocation());
		assertEquals("2015-01-01", i.get(0).getDate());
		assertEquals("Fire", i.get(0).getName());
	}

	@Test
	public void testBodyWithNoLocation() {
		List<Incident> i = checkGetIncidents("Power outage today.", "");
		assertEquals(0, i.size());
	}

	@Test
	public void testBodyWithParagraph() {
		List<Incident> i = checkGetIncidents(
				"Earlier I was knowing that there are just three parties in the grand alliance: JD(U), "
						+ "RJD and Congress Party but I’ve just come to know that a fourth partner too has joined "
						+ "them, a tantrik (black magician)”, said PM Modi in a veiled reference to a video clipping "
						+ "showing Bihar chief minister Nitish Kumar meeting with a tantrik went viral on social "
						+ "media on Saturday.",
				"");
		assertEquals(1, i.size());
		assertEquals("Bihar", i.get(0).getLocation());
		assertEquals("Saturday", i.get(0).getDate());

	}

}
