package org.incident.monitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.incident.utils.DBCPDataSourceFactory;

public class DB_test {
	public static void main(String[] args) {
		testDBCPDataSource("sqlite");
	}

	private static void testDBCPDataSource(String dbType) {
		DataSource ds = DBCPDataSourceFactory.getDataSource(dbType);

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = ds.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery("select * from email_filters");
			while (rs.next()) {
				System.out.println("Display From =" + rs.getString(1) + ", Subject=" + rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
