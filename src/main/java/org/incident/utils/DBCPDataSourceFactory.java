package org.incident.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.incident.monitor.IncidentMonitorConstants;

public class DBCPDataSourceFactory {

	private static BasicDataSource ds;

	public static synchronized DataSource getDataSource(String dbType) {
		if (ds == null) {
			Properties props = new Properties();
			FileInputStream fis = null;
			ds = new BasicDataSource();

			try {
				fis = new FileInputStream(
						new File(DBCPDataSourceFactory.class.getClassLoader().getResource("db.properties").getFile()));

				props.load(fis);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			if ("mysql".equals(dbType)) {
				ds.setDriverClassName(props.getProperty("MYSQL_DB_DRIVER_CLASS"));
				ds.setUrl(props.getProperty("MYSQL_DB_URL"));
				ds.setUsername(props.getProperty("MYSQL_DB_USERNAME"));
				ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
				ds.setInitialSize(IncidentMonitorConstants.num_db_connections);
			} else if ("oracle".equals(dbType)) {
				ds.setDriverClassName(props.getProperty("ORACLE_DB_DRIVER_CLASS"));
				ds.setUrl(props.getProperty("ORACLE_DB_URL"));
				ds.setUsername(props.getProperty("ORACLE_DB_USERNAME"));
				ds.setPassword(props.getProperty("ORACLE_DB_PASSWORD"));
				ds.setInitialSize(IncidentMonitorConstants.num_db_connections);
			} else if ("sqlite".equals(dbType)) {
				ds.setDriverClassName(props.getProperty("SQLITE_DB_DRIVER_CLASS"));
				ds.setUrl(props.getProperty("SQLITE_DB_URL"));
				ds.setInitialSize(IncidentMonitorConstants.num_db_connections);
			} else {
				return null;
			}
			return ds;
		} else
			return ds;
	}
}
