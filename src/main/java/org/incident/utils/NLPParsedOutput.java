package org.incident.utils;

import java.util.ArrayList;
import java.util.List;

public class NLPParsedOutput {
	private List<String> dateParts;
	private List<String> locationParts;
	private List<String> nameParts;

	public NLPParsedOutput() {
		dateParts = new ArrayList<String>();
		locationParts = new ArrayList<String>();
		nameParts = new ArrayList<String>();
	}

	public List<String> getDateParts() {
		return dateParts;
	}

	public void removeDatePart(String datePart) {
		this.dateParts.remove(datePart);
	}

	public void addDatePart(String datePart) {
		this.dateParts.add(datePart);
	}

	public List<String> getLocationParts() {
		return locationParts;
	}

	public void addLocationPart(String locationPart) {
		this.locationParts.add(locationPart);
	}

	public List<String> getNameParts() {
		return nameParts;
	}

	public void addNamePart(String namePart) {
		this.nameParts.add(namePart);
	}

}
