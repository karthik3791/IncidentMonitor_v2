package org.incident.monitor;

import java.io.Serializable;
import java.util.Date;

public class NormalizedIncident implements Serializable {
	private String name;
	private Date date;
	private Location location;

	public String getName() {
		return name;
	}

	public Date getDate() {
		return date;
	}

	public Location getLocation() {
		return location;
	}

	private static final long serialVersionUID = 759L;

	public NormalizedIncident(String name, Date date, Location location) {
		this.name = name;
		this.date = date;
		this.location = location;
	}
}
