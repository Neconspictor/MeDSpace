/***********************************
 * Database and read-user creation 
 **********************************/

DROP DATABASE IF EXISTS medspace_test; 
CREATE DATABASE IF NOT EXISTS medspace_test;

DROP USER IF EXISTS medspace_client_test@'%';
CREATE USER IF NOT EXISTS medspace_client_test@'%' IDENTIFIED BY 'k4N!rT';
GRANT SELECT ON medspace_test.* TO medspace_client_test@'%';

/*************************************
 *	Database Initialization
 ************************************/

USE medspace_test;
Drop TABLE IF EXISTS PATIENT;
DROP TABLE IF EXISTS DOCTOR;
DROP TABLE IF EXISTS HOSPITAL;
DROP TABLE IF EXISTS RCP;
DROP TABLE IF EXISTS DECISION;
DROP TABLE IF EXISTS RCP_CAUSE;

DROP TABLE IF EXISTS RECEPTOR;
DROP TABLE IF EXISTS CLINICAL_DATA;
DROP TABLE IF EXISTS THERAPY;
DROP TABLE IF EXISTS TREATMENT;
DROP TABLE IF EXISTS DOCTOR_TREAT_PATIENT;
DROP TABLE IF EXISTS BREASTCANCER_HAS_RECEPTOR;
DROP TABLE IF EXISTS DOCTOR_PARTICIPATE_RCP;
DROP TABLE IF EXISTS CLASSIFICATION;

DROP TABLE IF EXISTS VANNUYSINDEX;
DROP TABLE IF EXISTS TNM;
DROP TABLE IF EXISTS TUMOR;
DROP TABLE IF EXISTS BREASTCANCER;
DROP TABLE IF EXISTS ADENOCARCINOMA;


CREATE TABLE IF NOT EXISTS PATIENT (
	id integer NOT NULL UNIQUE,
    firstname varchar(32),
    lastname varchar(32),
    maidenname varchar(32),
    birthday date FORMAT 'DD-MM-YYYY',
    sex char,
	PRIMARY KEY(id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS DOCTOR (
	id integer NOT NULL UNIQUE,
    firstname varchar(32),
    lastname varchar(32),
    hospital integer,
	PRIMARY KEY(id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS HOSPITAL (
	id integer NOT NULL UNIQUE,
    place varchar(64),
    name varchar(64),
	PRIMARY KEY(id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS RCP (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    place varchar(64),
    rcpdate date,
    patient integer
	
	#CONSTRAINT fk_sex FOREIGN KEY (sex) REFERENCES Sex(Name) ON UPDATE CASCADE ON DELETE RESTRICT,
	#CONSTRAINT fk_maritalStatus FOREIGN KEY (maritalStatus) REFERENCES MaritalStatus(Name) ON UPDATE CASCADE ON DELETE RESTRICT,
	#CONSTRAINT fk_language FOREIGN KEY (language) REFERENCES Language(Name) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS DECISION (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    description varchar(512),
    type varchar(32),
    rcp integer
	
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS RCP_CAUSE (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    description varchar(200),
    rcp integer
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS RECEPTOR (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    description varchar(32)
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS CLINICAL_DATA (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    tumor integer,
    breastsize char,
    history varchar(200)
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS THERAPY (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    rcp integer,
    place varchar(64),
    comment varchar(200)
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS TREATMENT (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    rcp integer,
    therapy integer,
    duration integer,
    sequence integer,
    description varchar(100),
    type varchar(32)
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS DOCTOR_TREAT_PATIENT (
	doctor integer,
    patient integer
    
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS BREASTCANCER_HAS_RECEPTOR (
	tumor integer,
    receptor integer
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS DOCTOR_PARTICIPATE_RCP (
	doctor integer,
    rcp integer
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS CLASSIFICATION (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    tumor integer,
    patient integer,
    name varchar(32)
	
) ENGINE=INNODB;


CREATE TABLE IF NOT EXISTS VANNUYSINDEX (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    tumor integer,
    patient integer,
    surgeryreport varchar(200),
    grading integer,
    resectionmargin integer,
    tumorsize integer
	
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS TNM (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    tumor integer,
    patient integer,
    radicalism varchar(2),
    metastases integer,
    tumorsize integer,
    affected_lymphnodes integer
	
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS TUMOR (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    patient integer,
    name varchar(32)
	
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS BREASTCANCER (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    patient integer,
    quadrant integer,
    breastside char,
    microcalcification char
	
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS ADENOCARCINOMA (
	id integer NOT NULL UNIQUE PRIMARY KEY,
    patient integer,
    metastasestype varchar(32),
    SBR_grading integer,
    resection_margin integer,
    number_of_removed_lymphnodes integer
	
) ENGINE=INNODB;


LOAD DATA LOCAL INFILE 'C:/ProgramData/MySQL/MySQL Server 5.7/Uploads/patient_small.csv'
INTO TABLE medspace_test.patient
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, firstname, lastname, maidenname, @birthday_variable, sex) -- read one of the field to variable
SET birthday = STR_TO_DATE(@birthday_variable, '%e.%c.%Y'); -- format this date-time variable