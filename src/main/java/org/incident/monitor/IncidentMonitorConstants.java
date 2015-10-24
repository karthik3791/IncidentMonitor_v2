package org.incident.monitor;

public class IncidentMonitorConstants {

	public static int num_db_connections = 3;
	public static String database_type = "sqlite";
	public static String check_filter_query = "select * from email_filters where (display_from ='n/a' or instr(upper(?),upper(display_from))<> 0) and (subject ='n/a' OR instr(upper(?),upper(subject)) <> 0 )";
	public static String check_raw_filter_query = "select * from email_filters";
	public static String check_template_query = "select * from email_templates where (display_from ='n/a' or instr(upper(?),upper(display_from))<> 0) and (subject ='n/a' OR instr(upper(?),upper(subject)) <> 0 )";

}
