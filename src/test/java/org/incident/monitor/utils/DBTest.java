package org.incident.monitor.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.incident.monitor.IncidentMonitorConstants;
import org.incident.utils.DBManager;
import org.incident.utils.DBQuery;
import org.junit.Test;

public class DBTest {

	@Test
	public void testDBQuery() {
		DBManager db = DBManager.getDBInstance();
		DBQuery dq = db.executeSqlWithoutParameters(IncidentMonitorConstants.check_raw_filter_query);
		ResultSet rs = dq.getRs();
		try {
			if (rs.next()) {
				String display_from = rs.getString(1);
				String subject = rs.getString(2);
				assertEquals(subject, "n/a");
			} else {
				fail("Should have gotten a result back");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			dq.close();
		}
	}
}