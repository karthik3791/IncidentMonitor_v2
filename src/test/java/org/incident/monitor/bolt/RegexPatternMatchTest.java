package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.incident.bolt.FilterTemplateBolt;
import org.incident.monitor.Email;
import org.junit.Test;

public class RegexPatternMatchTest {

	private String _testTruckString = "truck\r\n" + "zDoug\r\n" + "Doug's house\r\n" + "(123) 456-7890\r\n"
			+ "Edoug@doug.com\r\n" + "30\r\n" + "61234.56\r\n" + "8/10/2003\r\n" + "\r\n" + "vehicle\r\n" + "eRob\r\n"
			+ "Rob's house\r\n" + "(987) 654-3210\r\n" + "Frob@rob.com\r\n";

	private String _testTruckOutput = "truck\r\n" + "zDoug\r\n" + "Doug's house\r\n" + "(123) 456-7890\r\n"
			+ "Edoug@doug.com\r\n" + "30\r\n" + "61234.56\r\n" + "8/10/2003";

	// Adding this to test the function directly without inserting into db each
	// time to test regex entries.
	private void doTestProcessTemplate(boolean expectedToMatch, Email rawEmail, String date_regex, String date_component,
			String location_regex, String location_component, String name_regex, String name_component) {
		FilterTemplateBolt ftbolt = new FilterTemplateBolt();
		assertEquals(expectedToMatch, ftbolt.processTemplate(rawEmail, date_regex, date_component, location_regex,
				location_component, name_regex, name_component));
	}

	private void doTestSimpleMatchRegex(String input, String regex, int groupIndex, String expectedOutput) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if(matcher.find()) {
			assertEquals(expectedOutput, matcher.group(groupIndex));
		} else {
			fail("Failed to match regex");
		}
	}
	
	@Test
	public void testBasicCase() {
		doTestSimpleMatchRegex("Alice lived in Alice's Wonderland", "Wonderland.*", 0, "Wonderland");
	}

	@Test
	public void testStackoverflowTruckCase() {
		doTestSimpleMatchRegex(_testTruckString, "(?m)^truck(?:(?:\r\n|[\r\n]).+$)*", 0, _testTruckOutput);
	}
}
