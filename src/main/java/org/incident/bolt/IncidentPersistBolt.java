package org.incident.bolt;

import java.util.Date;
import java.util.Vector;

import org.incident.monitor.Email;
import org.incident.monitor.Location;
import org.incident.monitor.NormalizedIncident;
import org.incident.utils.DBManager;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class IncidentPersistBolt extends BaseBasicBolt {

	private DBManager db;

	public IncidentPersistBolt() {
		db = DBManager.getDBInstance();
	}

	private void persistNormalizedIncident(NormalizedIncident nm) {

		String eventName = nm.getName();
		Location eventLoc = nm.getLocation();
		Date eventDate = nm.getDate();

	}

	private void persistEmail(Email rawEmail) {
		Vector<NormalizedIncident> normalizedIncidents = rawEmail.getNormalizedIncidents();
		for (int i = 0; i < normalizedIncidents.size(); i++) {
			persistNormalizedIncident(normalizedIncidents.get(i));
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
