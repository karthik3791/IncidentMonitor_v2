package org.incident.bolt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Vector;

import org.incident.monitor.Email;
import org.incident.monitor.IncidentMonitorConstants;
import org.incident.monitor.Location;
import org.incident.monitor.NormalizedIncident;
import org.incident.utils.DBManager;
import org.incident.utils.DBQuery;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class IncidentPersistBolt extends BaseBasicBolt {

	private static final long serialVersionUID = -6760428944287625823L;
	private DBManager db;

	public IncidentPersistBolt() {
		db = DBManager.getDBInstance();
	}

	private boolean shouldPersist(NormalizedIncident nm) {
		boolean doPersist = true;
		// select possibly related incidents
		// country, eventdate + or - 1 day
		LocalDateTime endDateWindow = LocalDateTime.from(nm.getDate().toInstant())
				.plusDays(IncidentMonitorConstants.numberOfDaysWindow),
				startDateWindow = LocalDateTime.from(nm.getDate().toInstant())
						.minusDays(IncidentMonitorConstants.numberOfDaysWindow);

		DBQuery dq = db.executeExactSql(IncidentMonitorConstants.check_persisted_incidents_query,
				nm.getLocation().getCountry(), Date.from(startDateWindow.atZone(ZoneId.systemDefault()).toInstant()),
				Date.from(endDateWindow.atZone(ZoneId.systemDefault()).toInstant()));

		ResultSet rs = dq.getRs();
		try {
			// for each incident persisted in db, check if incident is equal
			while (rs.next()) {
				NormalizedIncident dbIncident = new NormalizedIncident(rs.getString(1), rs.getDate(2),
						new Location(rs.getDouble(3), rs.getDouble(4), rs.getString(5), rs.getString(6),
								rs.getString(7), rs.getString(8), rs.getString(9), rs.getString(10)));
				if (dbIncident.equals(nm)) {
					doPersist = false;
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			dq.close();
		}

		return doPersist;
	}

	private void persistNormalizedIncident(NormalizedIncident incident) {
		String eventName = incident.getName();
		Location eventLoc = incident.getLocation();
		Date eventDate = incident.getDate();
		// insert into incident master
		db.updateExactSql(IncidentMonitorConstants.update_incidents_table_statement, eventName, eventDate.toString(),
				eventLoc.getCountry(), eventLoc.getLatitude(), eventLoc.getLongitude(), eventLoc.getRouteName(),
				eventLoc.getLocality(), eventLoc.getNeighborhood(), eventLoc.getAdminAreaLevel1(),
				eventLoc.getFormattedAddress());
	}

	private void persistEmail(Email rawEmail) {
		Vector<NormalizedIncident> normalizedIncidents = rawEmail.getNormalizedIncidents();
		for (int i = 0; i < normalizedIncidents.size(); i++) {
			NormalizedIncident incident = normalizedIncidents.get(i);
			if (shouldPersist(incident)) {
				persistNormalizedIncident(incident);
			}
		}
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		if (input.getSourceStreamId().equals("normalizedMail")) {
			Email rawEmail = (Email) input.getValue(0);
			persistEmail(rawEmail);
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// Wont Emit anything.
	}

}
