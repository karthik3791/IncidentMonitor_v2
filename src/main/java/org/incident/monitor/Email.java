package org.incident.monitor;

import java.io.Serializable;
import java.util.Vector;

public class Email implements Serializable {

	private static final long serialVersionUID = 20L;

	private String displayFrom;
	private String displayTo;
	private String displayCC;
	private String displayBCC;
	private String subject;
	private String body;
	private String[] recipientEmailList;
	private String messageDate;
	private Vector<Incident> incidents;

	public String[] getRecipientAddress() {
		return recipientEmailList;
	}

	public void setRecipientAddress(String[] recipientAddress) {
		this.recipientEmailList = recipientAddress;
	}

	public Email() {
		incidents = new Vector<Incident>();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(1024);
		str.append("\n-------- EMAIL START ------");
		str.append("\n Message Date :");
		str.append(this.getMessageDate());
		str.append("\nDisplay From : ");
		str.append(this.getDisplayFrom());
		if (this.getRecipientAddress() != null) {
			str.append("\nRecipent Mail List :");
			for (String recipient : this.getRecipientAddress())
				str.append("\n" + recipient);
		}
		str.append("\nDisplay To : ");
		str.append(this.getDisplayTo());
		str.append("\nSubject : ");
		str.append(this.getSubject());
		str.append("\n-------- EMAIL END ------");
		return str.toString();

	}

	public String getDisplayFrom() {
		return displayFrom;
	}

	public void setDisplayFrom(String displayFrom) {
		this.displayFrom = displayFrom;
	}

	public String getDisplayTo() {
		return displayTo;
	}

	public void setDisplayTo(String displayTo) {
		this.displayTo = displayTo;
	}

	public String getDisplayCC() {
		return displayCC;
	}

	public void setDisplayCC(String displayCC) {
		this.displayCC = displayCC;
	}

	public String getDisplayBCC() {
		return displayBCC;
	}

	public void setDisplayBCC(String displayBCC) {
		this.displayBCC = displayBCC;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(String messageDate) {
		this.messageDate = messageDate;
	}

	public void addIncident(Incident i) {
		this.incidents.add(i);
	}

}
