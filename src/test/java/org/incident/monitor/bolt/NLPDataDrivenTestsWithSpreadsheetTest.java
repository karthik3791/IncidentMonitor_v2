package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.incident.monitor.Incident;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NLPDataDrivenTestsWithSpreadsheetTest extends NLPBoltTest {

	private String input, defaultEmailDate;
	private boolean validCase;
	private Incident expectedIncident;
	private static InputStream NLPParserTestSheet;

	@SuppressWarnings("rawtypes")
	@Parameters
	public static Collection spreadsheetData() throws IOException {
		NLPParserTestSheet = new FileInputStream(NLPDataDrivenTestsWithSpreadsheetTest.class.getClassLoader()
				.getResource("NLPParserTestSheet.xls").getFile());
		return new SpreadsheetData(NLPParserTestSheet).getData();
	}

	public NLPDataDrivenTestsWithSpreadsheetTest(String input, String defaultEmailDate, String validCase,
			String eventLocation, String eventDate, String eventName) {
		super();
		this.input = input;
		this.defaultEmailDate = StringUtils.isBlank(defaultEmailDate) ? "" : defaultEmailDate;
		this.validCase = validCase.equals("Y");
		this.expectedIncident = this.validCase ? new Incident(eventName.trim(), eventDate.trim(), eventLocation.trim())
				: null;
	}

	@Test
	public void assertIncidentCreated() {
		List<Incident> actualIncident = checkGetIncidents(input, defaultEmailDate);
		if (validCase) {
			assertEquals(1, actualIncident.size());
			assertIncident(expectedIncident, actualIncident.get(0));
		} else {
			assertEquals(0, actualIncident.size());
		}
	}
}
