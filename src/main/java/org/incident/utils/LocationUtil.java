package org.incident.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.incident.monitor.Location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;

import edu.emory.mathcs.backport.java.util.Arrays;

public class LocationUtil {
	private static final String GOOGLE_API_KEY = "AIzaSyCkabUaIGG7jKJuXi-O4wym7ZH8wT3A53g";
	private static GeoApiContext context;

	public LocationUtil() {
		context = new GeoApiContext().setApiKey(GOOGLE_API_KEY);
	}

	public String getLongLat() {
		return "";
	}

	public List<Location> getLocationFromString(String inputString) throws Exception {
		System.out.println("getLocationFromString");
		GeocodingResult[] results = getGeocodingResults(inputString);
		return getLocations(results);
	}

	private GeocodingResult[] getGeocodingResults(String address) throws Exception {
		System.out.println("getGeocodingResults");
		return GeocodingApi.geocode(context, address).await();
	}

	private List<Location> getLocations(GeocodingResult[] results) {
		List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < results.length; i++) {
			Location l = new Location();
			l.setLongLat(results[i].geometry.location.lng, results[i].geometry.location.lat);
			l.setFormattedAddress(results[i].formattedAddress);
			AddressComponent[] addComponents = results[i].addressComponents;
			setAddressComponents(l, addComponents);
			// Checking for Country, Latitude and Longitude. These 3 are
			// mandatory for a location.
			if (StringUtils.isNotBlank(l.getCountry()) && l.getLatitude() != 0 && l.getLongitude() != 0)
				locations.add(l);
		}
		return locations;
	}

	private void setAddressComponents(Location l, AddressComponent[] components) {
		for (int i = 0; i < components.length; i++) {
			if (contains(AddressComponentType.COUNTRY, components[i].types)) {
				l.setCountry(components[i].longName.trim());
			}
			if (contains(AddressComponentType.ROUTE, components[i].types)) {
				l.setRouteName(components[i].longName.trim());
			}
			if (contains(AddressComponentType.LOCALITY, components[i].types)) {
				l.setLocality(components[i].longName.trim());
			}
			if (contains(AddressComponentType.NEIGHBORHOOD, components[i].types)) {
				l.setNeighborhood(components[i].longName.trim());
			}
			if (contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1, components[i].types)) {
				l.setAdminAreaLevel1(components[i].longName.trim());
			}
		}
	}

	private boolean contains(AddressComponentType type, AddressComponentType[] types) {
		return Arrays.asList(types).contains(type);
	}

	public double getDistanceBetweenLocations(Location location1, Location location2) {
		double lon1 = location1.getLongitude();
		double lat1 = location1.getLatitude();
		double lon2 = location2.getLongitude();
		double lat2 = location2.getLatitude();

		return getDistanceByLongLat(lon1, lat1, lon2, lat2);
	}

	// calculation distance between longitude and latitude points. Source:
	// http://www.geodatasource.com/developers/java
	public double getDistanceByLongLat(double lon1, double lat1, double lon2, double lat2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		return dist * 60 * 1.1515 * 1.609344; // return calculated distance in
												// km
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
}
