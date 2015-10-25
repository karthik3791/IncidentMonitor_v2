package org.incident.utils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4237722184943907461L;
	private Connection con;
	private ResultSet rs;
	private PreparedStatement ps;

	public DBQuery(Connection con, ResultSet rs, PreparedStatement ps) {
		this.con = con;
		this.rs = rs;
		this.ps = ps;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public ResultSet getRs() {
		return rs;
	}

	public void setRs(ResultSet rs) {
		this.rs = rs;
	}

	public void setPs(PreparedStatement ps) {
		this.ps = ps;
	}

	public void close() {
		try {
			this.con.close();
			this.ps.close();
			this.rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
