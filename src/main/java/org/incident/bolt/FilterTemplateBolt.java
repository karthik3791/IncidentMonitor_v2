package org.incident.bolt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.incident.monitor.Email;
import org.incident.monitor.Incident;
import org.incident.monitor.IncidentMonitorConstants;
import org.incident.utils.DBManager;
import org.incident.utils.DBQuery;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class FilterTemplateBolt extends BaseBasicBolt {

	private static final String BODY = "BODY";
	private static final long serialVersionUID = 1L;
	private DBManager db;

	public FilterTemplateBolt() {
		db = DBManager.getDBInstance();
	}

	public boolean checkFilter(Email rawEmail) {
		boolean filter_status = false;
		String displayFrom = rawEmail.getDisplayFrom();
		String subject = rawEmail.getSubject();
		DBQuery dq = db.executeExactSql(IncidentMonitorConstants.check_filter_query, displayFrom, subject);
		ResultSet rs = dq.getRs();
		try {
			// use the first matched filter
			filter_status = rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			dq.close();
		}
		return filter_status;
	}

	public boolean processTemplate(Email rawEmail, String date_regex, String date_component, String location_regex,
			String location_component, String name_regex, String name_component) {

		Pattern date_pattern = Pattern.compile(date_regex);
		Pattern name_pattern = Pattern.compile(name_regex);
		Pattern location_pattern = Pattern.compile(location_regex);

		String email_body = rawEmail.getBody().replaceAll("(\\r|\\n)", "~");
		email_body = email_body.replaceAll("~~~~ ~~~~", "~");
		email_body = email_body.replaceAll("~~~~", " ");

		// process date
		Matcher date_matcher, name_matcher, location_matcher;
		if (date_component.equals(BODY)) {
			date_matcher = date_pattern.matcher(email_body);
		} else {
			date_matcher = date_pattern.matcher(rawEmail.getSubject());
		}
		if (name_component.equals(BODY)) {
			name_matcher = name_pattern.matcher(email_body);
		} else {
			name_matcher = name_pattern.matcher(rawEmail.getSubject());
		}
		if (location_component.equals(BODY)) {
			location_matcher = location_pattern.matcher(email_body);
		} else {
			location_matcher = location_pattern.matcher(rawEmail.getSubject());
		}

		if (!date_matcher.find() || !name_matcher.find() || !location_matcher.find()) {
			return false;
		}

		String name = name_matcher.group(1);
		String location = location_matcher.group(1);
		String date = date_matcher.group(1);

		// enrich RawEmail Object
		rawEmail.addIncident(new Incident(name, date, location));
		return true;
	}

	public boolean checkAndProcessTemplate(Email rawEmail) {
		boolean template_status = false;
		DBQuery dq = db.executeExactSql(IncidentMonitorConstants.check_template_query, rawEmail.getDisplayFrom(),
				rawEmail.getSubject());
		ResultSet rs = dq.getRs();
		try {
			// use the first matched template
			if (rs.next()) {
				template_status = processTemplate(rawEmail, rs.getString("date_regex"), rs.getString("date_component"),
						rs.getString("location_regex"), rs.getString("location_component"), rs.getString("name_regex"),
						rs.getString("name_component"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			dq.close();
		}
		return template_status;
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		if (input.getSourceStreamId().equals("rawemail")) {
			Email rawEmail = (Email) input.getValue(0);
			if (this.checkFilter(rawEmail)) {
				System.out.println("Email from " + rawEmail.getDisplayFrom() + " and subject " + rawEmail.getSubject()
						+ " was filtered.");
			} else {
				if (this.checkAndProcessTemplate(rawEmail)) {
					// send to Normalizer Bolt directly
					System.out.println("Structured Email Found !");
					collector.emit("structuredMail", new Values(rawEmail));
				} else {
					// send to NLP Bolt
					System.out.println("Email is unstructured !");
					collector.emit("unstructuredMail", new Values(rawEmail));
				}
			}
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("structuredMail", new Fields("email"));
		declarer.declareStream("unstructuredMail", new Fields("email"));
	}
}
