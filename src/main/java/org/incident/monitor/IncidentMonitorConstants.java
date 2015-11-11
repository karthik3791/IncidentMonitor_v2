package org.incident.monitor;

public class IncidentMonitorConstants {

	// Filter & Template
	public static int num_db_connections = 3;
	public static String database_type = "sqlite";
	public static String check_filter_query = "select * from email_filters where (display_from ='n/a' or instr(upper(?),upper(display_from))<> 0) and (subject ='n/a' OR instr(upper(?),upper(subject)) <> 0 )";
	public static String check_raw_filter_query = "select * from email_filters";
	public static String check_template_query = "select * from email_templates where (display_from ='n/a' or instr(upper(?),upper(display_from))<> 0) and (subject ='n/a' OR instr(upper(?),upper(subject)) <> 0 )";

	// NLP
	public static final String NLPDateEntityIdentifier = "DATE";
	public static final String NLPPrepositionIdentifier = "IN";
	public static final String NLPDeterminantIdentifier = "DT";
	public static final String NLPModalIdentifier = "MD";
	public static final String NLPOrganizationEntityIdentifier = "ORGANIZATION";
	public static final String NLPLocationEntityIdentifier = "LOCATION";
	public static final String NLPNounEntityIdentifier = "NN.*";
	public static final String NLPVerbEntityIdentifier = "VB.*";
	public static final String NLPAdjectiveEntityIdentifier = "JJ.*";
	public static final String NLPNumberIdentifier = "NUMBER";
	public static final String NLPUnknownEntityIdentifier = "O";
	public static final String NLPMiscEntityIdentifier = "MISC";
	public static final String NLPPersonEntityIdentifier = "PERSON";
	public static final String NLPUnwantedIdentifiers = NLPDeterminantIdentifier + "|" + NLPModalIdentifier
			+ "|,|;|:|-LRB-|-RRB-";
	public static final String NLPNEROtherIdentifiers = NLPUnknownEntityIdentifier + "|" + NLPMiscEntityIdentifier + "|"
			+ NLPPersonEntityIdentifier;

	public static final String NLPSubjectIdentifier = "nsubj.*";
	public static final String NLPAdjectiveMod = "amod";
	public static final String NLPNounMod = "nmod";
	public static final String NLPNounModOf = "nmod:of";
	public static final String NLPCompoundMod = "compound";
	public static final String NLPCCompId = "ccomp.*";
	public static final String NounFilters = "FW|RE|FWD|ALERT|UPDATE";

	public static final String MarchConstant = "MARCH";
	public static final String NLPXCompId = "xcomp.*";
	public static final String NLPDObj = "dobj.*";
	public static final String beforeNounComponents = NLPAdjectiveMod + "|" + NLPNounMod + "|" + NLPCompoundMod + "|"
			+ NLPNounModOf;

	// Persist
	public static String check_persisted_incidents_query = "select * from incidents_view where country = upper(?) and EVENT_DATE between date(?) and date(?)";
}
