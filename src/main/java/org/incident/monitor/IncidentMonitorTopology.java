/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.incident.monitor;

import org.incident.bolt.FilterTemplateBolt;
import org.incident.bolt.NLPBolt;
import org.incident.spout.FileBasedEmailSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

/**
 * This is a basic example of a Storm topology.
 */
public class IncidentMonitorTopology {

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("FileBasedEmailSpout", new FileBasedEmailSpout(), 1);
		builder.setBolt("FilterTemplateBolt", new FilterTemplateBolt(), 1).shuffleGrouping("FileBasedEmailSpout",
				"rawemail");
		// builder.setBolt("SocialMediaEnrichBolt", new SocialMediaEnrichBolt(),
		// 1).shuffleGrouping("FilterTemplateBolt");
		builder.setBolt("NLPBolt", new NLPBolt(), 1).shuffleGrouping("FilterTemplateBolt", "unstructuredMail");
		// builder.setBolt("NormalizerBolt", new NormalizerBolt(),
		// 1).shuffleGrouping("NLPBolt")
		// .shuffleGrouping("FilterTemplateBolt");
		// builder.setBolt("IncidentPersistBolt", new IncidentPersistBolt(),
		// 1).shuffleGrouping("NormalizerBolt");

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
