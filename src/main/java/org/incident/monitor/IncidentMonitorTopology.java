package org.incident.monitor;

import org.incident.bolt.FilterTemplateBolt;
import org.incident.bolt.IncidentPersistBolt;
import org.incident.bolt.NLPBolt;
import org.incident.bolt.NormalizerBolt;
import org.incident.spout.FileBasedEmailSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

public class IncidentMonitorTopology {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("FileBasedEmailSpout", new FileBasedEmailSpout(), 1);
		builder.setBolt("FilterTemplateBolt", new FilterTemplateBolt(), 1).shuffleGrouping("FileBasedEmailSpout",
				"rawemail");
		builder.setBolt("NLPBolt", new NLPBolt(), 1).shuffleGrouping("FilterTemplateBolt", "unstructuredMail");
		builder.setBolt("NormalizerBolt", new NormalizerBolt(), 1).shuffleGrouping("NLPBolt", "structuredNLPMail")
				.shuffleGrouping("FilterTemplateBolt", "structuredMail");
		builder.setBolt("IncidentPersistBolt", new IncidentPersistBolt(), 1).shuffleGrouping("NormalizerBolt",
				"normalizedMail");

		Config conf = new Config();
		conf.setDebug(false);
		conf.registerSerialization(Email.class);
		conf.registerSerialization(Incident.class);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, builder.createTopology());
		// Utils.sleep(10000);
		// cluster.killTopology("test");
		// cluster.shutdown();

	}
}
