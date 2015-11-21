package org.incident.spout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.incident.monitor.Email;

public class FileBasedEmailProducer implements EmailProducer {
	private String emailFile;
	private MAPIMessage msg;
	private File mail_dir;
	EmailListener listener;

	public FileBasedEmailProducer(EmailListener listener) {
		this.listener = listener;
		mail_dir = new File(getClass().getClassLoader().getResource("Emails").getFile());
	}

	public Email formEmail() throws InterruptedException {
		Email em = new Email();
		try {
			String displayFrom = msg.getDisplayFrom();
			if (StringUtils.isNotBlank(displayFrom))
				em.setDisplayFrom(displayFrom.trim());

			String displayTo = msg.getDisplayTo();
			if (StringUtils.isNotBlank(displayTo))
				em.setDisplayTo(displayTo.trim());

			String textSubject = msg.getSubject();
			if (StringUtils.isNotBlank(textSubject))
				em.setSubject(textSubject.trim());

			String textBody = msg.getTextBody();
			if (StringUtils.isNotBlank(textBody))
				em.setDisplayFrom(textBody.trim());

			Calendar msgdate = msg.getMessageDate();
			Date dt = msgdate.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			em.setMessageDate(format1.format(dt));

		} catch (ChunkNotFoundException e) {
			System.out.println("No Message Body");
		}
		return em;

	}

	public void produce_email() throws Exception {
		if (mail_dir.exists()) {
			for (File msg_file : mail_dir.listFiles()) {
				if (msg_file.getName().endsWith(".msg") || msg_file.getName().endsWith(".MSG")) {
					emailFile = msg_file.getPath();
					try {
						msg = new MAPIMessage(new BufferedInputStream(new FileInputStream(emailFile)));
						Email em = formEmail();
						listener.onEmail(em);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						String tmp = emailFile.replace("Emails", "Processed_Emails");
						String timestamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new java.util.Date());
						String processed_mail_path = tmp.replaceFirst("\\.(msg|MSG)", timestamp.concat(".msg"));
						msg_file.renameTo(new File(processed_mail_path));
					}
				}
			}
		} else {
			throw new Exception("Emails Directory not found.");
		}
	}
}
