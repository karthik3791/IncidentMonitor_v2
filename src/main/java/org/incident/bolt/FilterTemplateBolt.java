package org.incident.bolt;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.incident.monitor.Email;
import org.incident.monitor.IncidentMonitorConstants;
import org.incident.utils.DBManager;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class FilterTemplateBolt extends BaseBasicBolt {
	private DBManager db;

	public FilterTemplateBolt() {
		db = DBManager.getDBInstance();
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		if (input.getSourceStreamId().equals("rawemail")) {
			Email rawEmail = (Email) input.getValue(0);
			String displayFrom = rawEmail.getDisplayFrom();
			String subject = rawEmail.getSubject();
			ResultSet rs = db.executeLikeSql(IncidentMonitorConstants.check_filter_query, displayFrom, subject);
			try {
				while (rs.next()) {
					System.out.println(
							"Matched Filter :  DisplayFrom =" + rs.getString(1) + ", Subject=" + rs.getString(2));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

}
