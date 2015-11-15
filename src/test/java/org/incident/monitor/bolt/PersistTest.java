package org.incident.monitor.bolt;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.incident.bolt.IncidentPersistBolt;
import org.incident.monitor.IncidentMonitorConstants;
import org.incident.monitor.Location;
import org.incident.monitor.NormalizedIncident;
import org.incident.utils.DBManager;
import org.junit.After;
import org.junit.Test;

public class PersistTest {

	private void doTestNormalizedPersistIncident(String incidentName1, double lat1, double long1) {
		IncidentPersistBolt ipbolt = new IncidentPersistBolt();
		NormalizedIncident referencedIncident1;
		Date d = new Date();

		Location l1 = new Location();
		l1.setLatitude(lat1);
		l1.setLongitude(long1);
		l1.setCountry("Singapore");
		referencedIncident1 = new NormalizedIncident(incidentName1, d, l1);

		assertEquals(true, ipbolt.persistNormalizedIncident(referencedIncident1));

	}

	@Test
	public void testPersistLogic() {
		doTestNormalizedPersistIncident("Fire report", 1.335762D, 1.335762D);
	}

	@After
	public void tearDown() {
		DBManager db = DBManager.getDBInstance();
		db.updateExactSql(IncidentMonitorConstants.tear_down_incidents);
	}

}
