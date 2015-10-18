package org.incident.utils;

import java.util.List;

public class TextTools {

	public static String createStringFromList(List<String> listOfParts) {
		StringBuilder builder = new StringBuilder();
		for (String part : listOfParts) {
			builder.append(part);
			builder.append(" ");
		}
		return builder.toString().trim();
	}
}
