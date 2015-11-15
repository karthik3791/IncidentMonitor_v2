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

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location l) {
		this.location = l;
	}

	public void setDate(Date d) {
		this.date = d;
	}

	private static final long serialVersionUID = 759L;

	public NormalizedIncident(String name, Date date, Location location) {
		this.name = name;
		this.date = date;
		this.location = location;
	}

	@Override
	public boolean equals(Object obj) {
		NormalizedIncident otherIncident = (NormalizedIncident) obj;
		if (this.getLocation().equals(otherIncident.getLocation()))
			if (this.getName().toLowerCase().contains(otherIncident.getName().toLowerCase())
					|| otherIncident.getName().toLowerCase().contains(this.getName().toLowerCase()))
				return true;
		return false;
	}
}
