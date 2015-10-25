CREATE TABLE INCIDENT_FILTER_TEMPLATE
( TEMPLATE_ID INT,
  DISPLAY_FROM VARCHAR(255) NOT NULL,
  SUBJECT VARCHAR(255) NOT NULL DEFAULT 'n/a',
  INSERT_TS DATETIME DEFAULT CURRENT_TIMESTAMP,
  FILTER_FLAG VARCHAR(1) NOT NULL DEFAULT 'Y',
  TEMPLATE_FLAG VARCHAR(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (DISPLAY_FROM, SUBJECT)  
 );
 
 CREATE TABLE INCIDENT_TEMPLATE_DETAILS
(
  TEMPLATE_ID INT,
  EMAIL_COMPONENT VARCHAR(10),
  INFORMATION_TYPE VARCHAR(30),
  INFORMATION_REGEX VARCHAR(255),
  PRIMARY KEY(TEMPLATE_ID,EMAIL_COMPONENT,INFORMATION_TYPE)  
 );
 
 
 INSERT INTO INCIDENT_FILTER_TEMPLATE(DISPLAY_FROM) VALUES
 ('Van Der Meij, Emmeline');
 
  INSERT INTO INCIDENT_FILTER_TEMPLATE(DISPLAY_FROM,SUBJECT) VALUES
 ('Alvarez, Eduardo - HR','Out of Office');
 
  INSERT INTO INCIDENT_FILTER_TEMPLATE(TEMPLATE_ID,DISPLAY_FROM,FILTER_FLAG,TEMPLATE_FLAG) VALUES
 (1,'Ganeshkarthik','N','Y');
 
 INSERT INTO INCIDENT_TEMPLATE_DETAILS VALUES
 (1,'BODY','EVENT_NAME','Incident Name : (.*)')
  INSERT INTO INCIDENT_TEMPLATE_DETAILS VALUES
 (1,'BODY','EVENT_DATE','Incident Date : (.*)')
  INSERT INTO INCIDENT_TEMPLATE_DETAILS VALUES
 (1,'BODY','EVENT_LOCATION','Incident Location : (.*)')
 
 CREATE VIEW EMAIL_FILTERS 
 AS
   SELECT DISPLAY_FROM,SUBJECT 
   FROM INCIDENT_FILTER_TEMPLATE 
   WHERE FILTER_FLAG ='Y' AND TEMPLATE_FLAG ='N';
   
CREATE VIEW EMAIL_TEMPLATES
AS 
   SELECT template.display_from,
          template.subject,
		  eventdate.information_regex as date_regex,
		  eventdate.email_component as date_component,
		  eventloc.information_regex as location_regex,
		  eventloc.email_component as location_component,
		  eventname.information_regex as name_regex,
		  eventname.email_component as name_component
   FROM    INCIDENT_FILTER_TEMPLATE template
   INNER JOIN  INCIDENT_TEMPLATE_DETAILS eventdate ON template.template_id = eventdate.template_id AND eventdate.information_type ='EVENT_DATE'
   INNER JOIN  INCIDENT_TEMPLATE_DETAILS eventloc ON template.template_id = eventloc.template_id AND eventloc.information_type ='EVENT_LOCATION'
   INNER JOIN  INCIDENT_TEMPLATE_DETAILS eventname ON template.template_id = eventname.template_id AND eventname.information_type ='EVENT_NAME'
   WHERE template.template_flag ='Y' and template.template_id IS NOT NULL
