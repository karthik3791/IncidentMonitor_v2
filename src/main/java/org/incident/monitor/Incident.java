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

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(1024);
		str.append("Incident name: ").append(this.name).append("\n");
		str.append("Incident date: ").append(this.date).append("\n");
		str.append("Incident location: ").append(this.location).append("\n");
		return str.toString();
	}
}
