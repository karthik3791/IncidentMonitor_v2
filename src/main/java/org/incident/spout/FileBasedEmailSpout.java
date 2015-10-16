package org.incident.spout;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.incident.monitor.Email;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class FileBasedEmailSpout extends BaseRichSpout {

	SpoutOutputCollector _collector;
	LinkedBlockingQueue<Email> queue = null;

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		queue = new LinkedBlockingQueue<Email>(100);
		_collector = collector;

		final EmailListener listener = new EmailListener() {
			public void onEmail(Email email) {
				queue.offer(email);
			}
		};

		Thread t = new Thread(new Runnable() {
			public void run() {
				EmailProducer source = new FileBasedEmailProducer(listener);
				while (true) {
					try {
						source.produce_email();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	public void nextTuple() {
		Email email = queue.poll();
		if (email == null) {
			Utils.sleep(50);
		} else {
			_collector.emit("rawemail", new Values(email));
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declareStream("rawemail", new Fields("email"));
	}

}
