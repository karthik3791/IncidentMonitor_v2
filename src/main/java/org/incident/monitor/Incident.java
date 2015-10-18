package org.incident.monitor;

import java.io.Serializable;

public class Incident implements Serializable {

	private String name;
	private String date;
	private String location;

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public String getLocation() {
		return location;
	}

	private static final long serialVersionUID = 89L;

	public Incident(String name, String date, String location) {
		this.name = name;
		this.date = date;
		this.location = location;

	}

}
