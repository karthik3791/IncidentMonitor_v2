package org.incident.bolt;

import java.util.Date;
import java.util.Map;

import org.incident.monitor.Email;
import org.incident.monitor.Incident;
import org.incident.monitor.Location;
import org.incident.monitor.NormalizedIncident;
import org.incident.utils.DateUtil;
import org.incident.utils.LocationUtil;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class NormalizerBolt extends BaseRichBolt {

	private OutputCollector collector;
	private LocationUtil locUtil;
	private DateUtil dateUtil;

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.locUtil = new LocationUtil();
		this.dateUtil = new DateUtil();
	}

	public void execute(Tuple input) {
		if (input.getSourceGlobalStreamid().equals("structuredNLPMail")
				|| input.getSourceStreamId().equals("structuredMail")) {
			Email email = (Email) input.getValue(0);
			normalizeEmail(email);
			if (!email.getNormalizedIncidents().isEmpty()) {
				System.out.println("Email is normalized successfully.");
				collector.emit("normalizedMail", new Values(email));
			} else {
				System.out.println("Cannot normalize email");
			}
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("normalizedMail", new Fields("email"));
	}

	private void normalizeEmail(Email email) {
		for (int i = 0; i < email.getIncidents().size(); i++) {
			Incident incident = email.getIncidents().get(i);
			String locationStr = incident.getLocation();
			String dateStr = incident.getDate();
			System.out.println(incident.getName());
			System.out.println(locationStr);
			System.out.println(dateStr);
			try {
				Location location = locUtil.getLocationFromString(locationStr).get(0);
				Date date = dateUtil.parseDateFromString(dateStr).get(0);
				if (location != null && date != null) {
					NormalizedIncident normalized = new NormalizedIncident(incident.getName(), date, location);
					email.addNormalizedIncicent(normalized);
					System.out.println(incident.getName());
					System.out.println(date.toString());
					System.out.println(location.getFormattedAddress());
					System.out.println();
				}
			} catch (Exception e) {
				System.err.println("Cannot normalize incident: " + incident.getName() + ". " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}