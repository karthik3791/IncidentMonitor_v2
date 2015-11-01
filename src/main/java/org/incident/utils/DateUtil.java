package org.incident.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class DateUtil {

	private final Parser parser;

	public DateUtil() {
		parser = new Parser();
	}

	public List<String> seperateInputString() {
		List<String> outputStrings = new ArrayList<String>();
		// Do some pattern matching to split several dates in one string
		return outputStrings;
	}

	public List<Date> parseDateFromString(String inputString) {
		List<Date> parsedDates = new ArrayList<Date>();
		List<DateGroup> groups = parser.parse(inputString);
		for (DateGroup group : groups) {
			List<Date> dates = group.getDates();
			parsedDates.addAll(dates);
		}
		return parsedDates;
	}
}