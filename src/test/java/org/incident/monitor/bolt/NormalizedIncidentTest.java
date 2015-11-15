package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.incident.monitor.Location;
import org.incident.monitor.NormalizedIncident;
import org.junit.Test;

public class NormalizedIncidentTest {

	private void doTestEqualsNormalizedIncident(boolean isEqual, String incidentName1, String incidentName2,
			double lat1, double long1, double lat2, double long2) {
		NormalizedIncident referencedIncident1;
		NormalizedIncident referencedIncident2;
		Date d = new Date();

		Location l1 = new Location();
		l1.setLatitude(lat1);
		l1.setLongitude(long1);
		referencedIncident1 = new NormalizedIncident(incidentName1, d, l1);

		Location l2 = new Location();
		l2.setLatitude(lat2);
		l2.setLongitude(long2);
		referencedIncident2 = new NormalizedIncident(incidentName2, d, l2);
		assertEquals(isEqual, referencedIncident1.equals(referencedIncident2));
	}

	@Test
	public void testNameDifferentLocationDifferent() {
		doTestEqualsNormalizedIncident(false, "Fire report", "Elections results", 1.335762D, 1.335762D, 1.335762D,
				103.845832D);
	}

	@Test
	public void testNameSameLocationDifferent() {
		doTestEqualsNormalizedIncident(false, "Fire report", "Fire report", 1.335762D, 1.335762D, 1.335762D,
				103.845832D);
	}

	@Test
	public void testNameDifferentLocationSame() {
		doTestEqualsNormalizedIncident(false, "Fire report", "Elections results", 1.335762D, 103.845832D, 1.335762D,
				103.845832D);
	}

	@Test
	public void testNameSameLocationSame() {
		doTestEqualsNormalizedIncident(true, "Fire report", "Fire report", 1.335762D, 103.845832D, 1.335762D,
				103.845832D);
	}

	@Test
	public void testNameDifferentLocationDifferentButWithinThreshold() {
		doTestEqualsNormalizedIncident(true, "Fire report", "Fire", 1.335762D, 103.845832D, 1.435762D, 101.845832D);
	}

}
