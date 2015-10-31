package org.incident.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TextTools {

	public static String createStringFromList(List<String> listOfParts) {
		StringBuilder builder = new StringBuilder();
		for (String part : listOfParts) {
			builder.append(part);
			builder.append(" ");
		}
		return builder.toString().trim();
	}

	public static String getSortedValuesFromMap(Map<Integer, String> map) {
		List<Integer> sortedKeys = new ArrayList<Integer>(map.keySet());
		Collections.sort(sortedKeys);
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < sortedKeys.size(); i++) {
			Integer wordpos = (Integer) sortedKeys.get(i);
			builder.append(map.get(wordpos));
			builder.append(" ");
		}
		return builder.toString().trim();
	}
}
