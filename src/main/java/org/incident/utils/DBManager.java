package org.incident.utils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.dbcp.BasicDataSource;
import org.incident.monitor.IncidentMonitorConstants;

public class DBManager implements Serializable {

	private static final long serialVersionUID = 4409175170759185357L;

	private static DBManager db = new DBManager();

	private DBManager() {
	}

	public static DBManager getDBInstance() {
		return db;
	}

	public DBQuery executeSqlWithoutParameters(String sql) {
		BasicDataSource datasource = (BasicDataSource) DBCPDataSourceFactory
				.getDataSource(IncidentMonitorConstants.database_type);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			DBQuery dq = new DBQuery(con, rs, ps);
			return dq;
		} catch (SQLException e) {
			e.printStackTrace();
			rs = null;
		}
		return null;
	}

	public boolean updateExactSql(String sql, Object... params) {
		BasicDataSource datasource = (BasicDataSource) DBCPDataSourceFactory
				.getDataSource(IncidentMonitorConstants.database_type);
		Connection con = null;
		PreparedStatement ps = null;
		boolean done = false;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			appendParams(ps, params);
			if (ps.executeUpdate() > 0)
				done = true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return done;
	}

	public DBQuery executeExactSql(String sql, Object... params) {
		BasicDataSource datasource = (BasicDataSource) DBCPDataSourceFactory
				.getDataSource(IncidentMonitorConstants.database_type);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			appendParams(ps, params);
			rs = ps.executeQuery();
			DBQuery dq = new DBQuery(con, rs, ps);
			return dq;

		} catch (SQLException e) {
			e.printStackTrace();
			rs = null;
		}
		return null;
	}

	private void appendParams(PreparedStatement ps, Object[] params) throws SQLException {
		int i = 1;
		for (Object arg : params) {
			if (arg instanceof Date) {
				ps.setDate(i++, new java.sql.Date(((Date) arg).getTime()));
			} else if (arg instanceof Integer) {
				ps.setInt(i++, (Integer) arg);
			} else if (arg instanceof Long) {
				ps.setLong(i++, (Long) arg);
			} else if (arg instanceof Double) {
				ps.setDouble(i++, (Double) arg);
			} else if (arg instanceof Float) {
				ps.setFloat(i++, (Float) arg);
			} else {
				ps.setString(i++, (String) arg);
			}
		}
	}
}
