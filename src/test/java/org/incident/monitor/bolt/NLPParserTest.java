package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.incident.monitor.Incident;
import org.junit.Test;

public class NLPParserTest extends NLPBoltTest {

	@Test
	public void testSimpleNameLocDate() {
		List<Incident> i = checkGetIncidents("Power outage in Singapore on 20 October 2011", "");
		assertEquals(1, i.size());
		assertIncident(new Incident("Power outage", "20 October 2011", "Singapore"), i.get(0));
	}

	@Test
	public void testSimpleButMultipleNameLocDate() {
		List<Incident> i = checkGetIncidents(
				"Power outage in Singapore on 20 October 2011. Fire reported at North Carolina today.", "");
		assertEquals(2, i.size());
		assertIncident(new Incident("Power outage", "20 October 2011", "Singapore"), i.get(0));
		assertIncident(new Incident("Fire report", "today", "North Carolina"), i.get(1));
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
		assertIncident(new Incident("Power outage", "20 October 2011", "Singapore"), i.get(0));
	}

	@Test
	public void testSubjectWithNoDateWithCapitalizedWords() {
		List<Incident> i = checkGetIncidents("Power Outage in Singapore", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident(new Incident("Power Outage", "2015-01-01", "Singapore"), i.get(0));
	}

	@Test
	public void testSubjectWithNoDateWithOrganization() {
		List<Incident> i = checkGetIncidents("Fire in Singapore at Police Department Headquarters.", "2015-01-01");
		assertEquals(1, i.size());
		// assert default date and organization after proposition are used
		assertIncident(new Incident("Fire", "2015-01-01", "Singapore Police Department Headquarters"), i.get(0));
	}

	@Test
	public void testSubjectWithNoDateNoEvent() {
		List<Incident> i = checkGetIncidents("Flooded Singapore reported.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident(new Incident("flood report", "2015-01-01", "Singapore"), i.get(0));
	}

	@Test
	public void testSubjectMultipleSentences() {
		List<Incident> i = checkGetIncidents(
				"Power outage in Singapore on 20 October 2011. Wild fire reported at North Carolina. Power outage today.",
				"2015-01-01");
		// assert that last sentence is not used
		assertEquals(2, i.size());
		assertIncident(new Incident("Power outage", "20 October 2011", "Singapore"), i.get(0));
		assertIncident(new Incident("Wild fire report", "2015-01-01", "North Carolina"), i.get(1));
	}

	@Test
	public void testSubjectWithDateBeforeLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 30 Brooklyn Avenue, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident(new Incident("Fire report", "2015-01-01", "30 Brooklyn Avenue America"), i.get(0));
	}

	@Test
	public void testSubjectWithNumberBeforeLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 300 Brooklyn Avenue, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident(new Incident("Fire report", "2015-01-01", "300 Brooklyn Avenue America"), i.get(0));
	}

	@Test
	public void testSubjectWithNumberAfterLocation() {
		List<Incident> i = checkGetIncidents("Fire reported in 300 Brooklyn Avenue 209332, America.", "2015-01-01");
		assertEquals(1, i.size());
		assertIncident(new Incident("Fire report", "2015-01-01", "300 Brooklyn Avenue 209332 America"), i.get(0));
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
		assertIncident(new Incident("cases H1N1 report death toll mount", "April 13 2015", "India"), i.get(0));
	}

	@Test
	public void testSentenceWithNoNoun() {
		List<Incident> i = checkGetIncidents("BBC News reported that California flooded today.", "");
		assertEquals(1, i.size());
		assertIncident(new Incident("BBC News report flood", "today", "California"), i.get(0));
	}

	@Test
	public void testSentenceWithNoNoun1() {
		List<Incident> i = checkGetIncidents("BBC News predicts that California will experience rainfall on Saturday.",
				"");
		assertEquals(1, i.size());
		assertIncident(new Incident("BBC News predict experience rainfall", "Saturday", "California"), i.get(0));
	}

	@Test
	public void testSentenceWithNoNoun2() {
		List<Incident> i = checkGetIncidents("Docklands, Australia – Protest Outside the Transurban Office – May 27",
				"");
		assertEquals(1, i.size());
		assertIncident(new Incident("Docklands Protest", "May 27", "Australia Transurban Office"), i.get(0));
	}

	@Test
	public void testSimpleBracketSentence() {
		List<Incident> i = checkGetIncidents("FW: Power Outage in Canada (Vancouver) on May 27", "");
		assertEquals(1, i.size());
		assertIncident(new Incident("Power Outage", "May 27", "Canada Vancouver"), i.get(0));
	}
}
