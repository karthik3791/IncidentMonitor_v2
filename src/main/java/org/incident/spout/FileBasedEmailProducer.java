package org.incident.spout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
			em.setDisplayFrom(msg.getDisplayFrom().trim());
			em.setDisplayTo(msg.getDisplayTo().trim());
			em.setDisplayCC(msg.getDisplayCC().trim());
			em.setDisplayBCC(msg.getDisplayBCC().trim());
			em.setSubject(msg.getSubject().trim());
			em.setBody(msg.getTextBody().trim());
			em.setRecipientAddress(msg.getRecipientEmailAddressList());

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
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						String processed_mail_path = emailFile.replace("Emails", "Processed_Emails");
						msg_file.renameTo(new File(processed_mail_path));
					}
				}
			}
		} else {
			throw new Exception("Emails Directory not found.");
		}
	}
}
