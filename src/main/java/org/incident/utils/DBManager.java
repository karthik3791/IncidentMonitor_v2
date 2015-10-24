package org.incident.utils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.dbcp.BasicDataSource;
import org.incident.monitor.IncidentMonitorConstants;

public class DBManager implements Serializable {

	private static final long serialVersionUID = 4409175170759185357L;

	// private BasicDataSource datasource;

	private static DBManager db = new DBManager();

	private DBManager() {
		// datasource =
		// (BasicDataSource)DBCPDataSourceFactory.getDataSource(IncidentMonitorConstants.database_type);
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

	public DBQuery executeLikeSql_v2(String sql, Object... params) {
		BasicDataSource datasource = (BasicDataSource) DBCPDataSourceFactory
				.getDataSource(IncidentMonitorConstants.database_type);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int i = 1;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			for (Object arg : params) {
				if (arg instanceof Date) {
					ps.setTimestamp(i++, new Timestamp(((Date) arg).getTime()));
				} else if (arg instanceof Integer) {
					ps.setString(i++, "%" + (Integer) arg + "%");
				} else if (arg instanceof Long) {
					ps.setString(i++, "%" + (Long) arg + "%");
				} else if (arg instanceof Double) {
					ps.setString(i++, "%" + (Double) arg + "%");
				} else if (arg instanceof Float) {
					ps.setString(i++, "%" + (Float) arg + "%");
				} else {
					ps.setString(i++, "%" + (String) arg + "%");
				}
			}
			rs = ps.executeQuery();
			DBQuery dq = new DBQuery(con, rs, ps);
			return dq;
		} catch (SQLException e) {
			e.printStackTrace();
			rs = null;
		}
		return null;
	}

	public ResultSet executeLikeSql(String sql, Object... params) {
		BasicDataSource datasource = (BasicDataSource) DBCPDataSourceFactory
				.getDataSource(IncidentMonitorConstants.database_type);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int i = 1;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			for (Object arg : params) {
				if (arg instanceof Date) {
					ps.setTimestamp(i++, new Timestamp(((Date) arg).getTime()));
				} else if (arg instanceof Integer) {
					ps.setString(i++, "%" + (Integer) arg + "%");
				} else if (arg instanceof Long) {
					ps.setString(i++, "%" + (Long) arg + "%");
				} else if (arg instanceof Double) {
					ps.setString(i++, "%" + (Double) arg + "%");
				} else if (arg instanceof Float) {
					ps.setString(i++, "%" + (Float) arg + "%");
				} else {
					ps.setString(i++, "%" + (String) arg + "%");
				}
			}
			rs = ps.executeQuery();

		} catch (SQLException e) {
			e.printStackTrace();
			rs = null;
		}
		return rs;
	}

	public DBQuery executeExactSql(String sql, Object... params) {
		BasicDataSource datasource = (BasicDataSource) DBCPDataSourceFactory
				.getDataSource(IncidentMonitorConstants.database_type);
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int i = 1;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			for (Object arg : params) {
				if (arg instanceof Date) {
					ps.setTimestamp(i++, new Timestamp(((Date) arg).getTime()));
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
			rs = ps.executeQuery();
			DBQuery dq = new DBQuery(con, rs, ps);
			return dq;

		} catch (SQLException e) {
			e.printStackTrace();
			rs = null;
		}
		return null;
	}

}
