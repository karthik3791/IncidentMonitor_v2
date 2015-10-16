package org.incident.monitor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

public class IncidentMonitor_Test {

	/**
	 * The stem used to create file names for the text file and the directory
	 * that contains the attachments.
	 */
	private String fileNameStem;
	private MAPIMessage msg;

	public IncidentMonitor_Test() {
	}

	public IncidentMonitor_Test(String fileName) throws IOException {
		File msgFile = new File(getClass().getClassLoader().getResource("Emails/" + fileName).getFile());
		if (msgFile.exists()) {
			fileNameStem = msgFile.getAbsolutePath();
			 if(fileNameStem.endsWith(".msg") || fileNameStem.endsWith(".MSG")) {
	             fileNameStem = fileNameStem.substring(0, fileNameStem.length() - 4);
	         }
			msg = new MAPIMessage(new BufferedInputStream(new FileInputStream(msgFile)));
		} else {
			throw new IOException("File Does not Exist");
		}
	}

	/**
	 * Processes the message.
	 * 
	 * @throws IOException
	 *             if an exception occurs while writing the message out
	 */
	public void processMessage() throws IOException {

		String txtFileName = fileNameStem + ".txt";
		String attDirName = fileNameStem + "-att";
		PrintWriter txtOut = new PrintWriter(txtFileName);

		try {
			String displayFrom = msg.getDisplayFrom().trim();
			txtOut.println("From: " + displayFrom);
			String displayTo = msg.getDisplayTo().trim();
			txtOut.println("To: " + displayTo);
			String displayCC = msg.getDisplayCC().trim();
			txtOut.println("CC: " + displayCC);
			String displayBCC = msg.getDisplayBCC().trim();
			txtOut.println("BCC: " + displayBCC);
			String subject = msg.getSubject().trim();
			txtOut.println("Subject: " + subject);
			String body = msg.getTextBody().trim().replace("\n", "").replace("\r", "");
			txtOut.println("Body : ");
			txtOut.println(body);

			// Attachments handling
			AttachmentChunks[] attachments = msg.getAttachmentFiles();
			if (attachments.length > 0) {
				File d = new File(attDirName);
				if (d.exists())
					FileUtils.cleanDirectory(d);
				else {
					if (d.mkdir()) {
					} else {
						System.err.println("Can't create directory " + attDirName);
					}
				}
				for (AttachmentChunks attachment : attachments) {
					processAttachment(attachment, d);
				}
			}

		} catch (ChunkNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("No Message Body");
		} finally {
			if (txtOut != null) {
				txtOut.close();
			}
		}
	}

	/**
	 * Processes a single attachment: reads it from the Outlook MSG file and
	 * writes it to disk as an individual file.
	 *
	 * @param attachment
	 *            the chunk group describing the attachment
	 * @param dir
	 *            the directory in which to write the attachment file
	 * @throws IOException
	 *             when any of the file operations fails
	 */
	public void processAttachment(AttachmentChunks attachment, File dir) throws IOException {
		String fileName = attachment.attachFileName.toString();
		if (attachment.attachLongFileName != null) {
			fileName = attachment.attachLongFileName.toString();
		}
		File f = new File(dir, fileName);
		OutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(f);
			fileOut.write(attachment.attachData.getValue());
		} finally {
			if (fileOut != null) {
				fileOut.close();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length <= 0) {
			System.err.println("No files names provided");
		} else {
			for (int i = 0; i < args.length; i++) {
				try {
					IncidentMonitor_Test processor = new IncidentMonitor_Test(args[i]);
					processor.processMessage();
				} catch (IOException e) {
					System.err.println("Could not process " + args[i] + ": " + e);
				}
			}
		}
	}

}
