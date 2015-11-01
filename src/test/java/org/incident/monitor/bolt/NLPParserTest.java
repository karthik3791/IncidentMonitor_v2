package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.incident.bolt.NLPBolt;
import org.incident.monitor.Incident;
import org.junit.Test;

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

	public List<Incident> checkGetIncidents(String testString, String defaultDate) {
		return nlpTest.getIncidents(testString, this.pipeline, defaultDate);
	}

	public void assertIncident(String expectedlocation, String expectedDate, String expectedEventName,
			Incident actualIncident) {
		assertEquals(expectedlocation, actualIncident.getLocation());
		assertEquals(expectedDate, actualIncident.getDate());
		assertEquals(expectedEventName, actualIncident.getName());
	}

	@Test
	public void testSimpleNameLocDate() {
		List<Incident> i = checkGetIncidents("Power outage in Singapore on 20 October 2011", "");
		assertEquals(1, i.size());
		assertIncident("Singapore", "20 October 2011", "Power outage", i.get(0));
	}

	@Test
	public void testSimpleButMultipleNameLocDate() {
		List<Incident> i = checkGetIncidents(
				"Power outage in Singapore on 20 October 2011. Fire reported at North Carolina today.", "");
		assertEquals(2, i.size());
		assertIncident("Singapore", "20 October 2011", "Power outage", i.get(0));
		assertIncident("North Carolina", "today", "Fire report", i.get(1));
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
		// assert date on the subject is used instead of default date
		assertIncident("Singapore", "20 October 2011", "Power outage", i.get(0));
	}

	@Test
	public void testSubjectWithNoDateWithCapitalizedWords() {
		List<Incident> i = checkGetIncidents("Power Outage in Singapore", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident("Singapore", "2015-01-01", "Power Outage", i.get(0));
	}

	@Test
	public void testSubjectWithNoDateWithOrganization() {
		List<Incident> i = checkGetIncidents("Fire in Singapore at Police Department Headquarters.", "2015-01-01");
		assertEquals(1, i.size());
		// assert default date and organization after proposition are used
		assertIncident("Singapore Police Department Headquarters", "2015-01-01", "Fire", i.get(0));
	}

	@Test
	public void testSubjectWithNoDateNoEvent() {
		List<Incident> i = checkGetIncidents("Flooded Singapore reported.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident("Singapore", "2015-01-01", "flood report", i.get(0));
	}

	@Test
	public void testSubjectMultipleSentences() {
		List<Incident> i = checkGetIncidents(
				"Power outage in Singapore on 20 October 2011. Wild fire reported at North Carolina. Power outage today.",
				"2015-01-01");
		// assert that last sentence is not used
		assertEquals(2, i.size());
		assertIncident("Singapore", "20 October 2011", "Power outage", i.get(0));
		assertIncident("North Carolina", "2015-01-01", "Wild fire report", i.get(1));
	}

	@Test
	public void testSubjectWithDateBeforeLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 30 Brooklyn Avenue, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident("30 Brooklyn Avenue America", "2015-01-01", "Fire report", i.get(0));
	}

	@Test
	public void testSubjectWithNumberBeforeLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 300 Brooklyn Avenue, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident("300 Brooklyn Avenue America", "2015-01-01", "Fire report", i.get(0));
	}

	@Test
	public void testSubjectWithNumberAfterLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 300 Brooklyn Avenue 209332, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident("300 Brooklyn Avenue 209332 America", "2015-01-01", "Fire report", i.get(0));
	}

	@Test
	public void testBodyWithNoLocation() {
		List<Incident> i = checkGetIncidents("Power outage today.", "");
		assertEquals(0, i.size());
	}

	@Test
	public void testBodyWithParagraph() {
		List<Incident> i = checkGetIncidents(
				"As of April 13, 2015, nearly 35,000 cases of H1N1 have been reported across India with the death toll "
						+ "mounting to 2,200 for this year alone. Some other details that we don't need to parse.",
				"");
		assertEquals(1, i.size());
		assertIncident("India", "April 13 2015", "cases H1N1 report death toll mount", i.get(0));
	}

}
