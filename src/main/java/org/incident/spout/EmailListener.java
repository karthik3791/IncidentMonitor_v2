package org.incident.spout;

import org.incident.monitor.Email;

public interface EmailListener {
	public void onEmail(Email email);
}
