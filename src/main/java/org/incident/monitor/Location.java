package org.incident.monitor;

import java.io.Serializable;

import org.incident.utils.LocationUtil;

public class Location implements Serializable {

	private static final long serialVersionUID = 42L;
	private double longitude;
	private double latitude;
	private String routeName;
	private String country;
	private String locality; // incorporated city or town political entity
	private String neighborhood;
	private String adminAreaLevel1;
	private String formattedAddress;

	public Location() {
		this.longitude = 0;
		this.latitude = 0;
		this.routeName = "";
		this.country = "";
		this.locality = "";
		this.neighborhood = "";
		this.adminAreaLevel1 = "";
		this.formattedAddress = "";
	}

	public Location(double longitude, double latitude, String routeName, String country, String locality,
			String neighborhood, String adminAreaLevel1, String formattedAddress) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.routeName = routeName;
		this.country = country;
		this.locality = locality;
		this.neighborhood = neighborhood;
		this.adminAreaLevel1 = adminAreaLevel1;
		this.formattedAddress = formattedAddress;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongLat(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public String getRouteName() {
		return routeName;
	}

	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	public String getAdminAreaLevel1() {
		return adminAreaLevel1;
	}

	public void setAdminAreaLevel1(String adminAreaLevel1) {
		this.adminAreaLevel1 = adminAreaLevel1;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}

	@Override
	public boolean equals(Object obj) {
		Location otherLocation = (Location) obj;
		if (LocationUtil.getDistanceBetweenLocations(this,
				otherLocation) < IncidentMonitorConstants.incidentsDistanceThreshold)
			return true;
		return false;
	}
}
