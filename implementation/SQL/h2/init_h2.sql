DROP SCHEMA IF EXISTS medspace;

CREATE SCHEMA IF NOT EXISTS medspace;

DROP USER IF EXISTS medspace_client;
CREATE USER IF NOT EXISTS medspace_client PASSWORD 'k4N!rT';

DROP ROLE IF EXISTS READONLY;
CREATE ROLE IF NOT EXISTS READONLY;
GRANT READONLY TO medspace_client;

USE medspace;
Drop TABLE IF EXISTS Patient;
DROP TABLE IF EXISTS Language;
DROP TABLE IF EXISTS MaritalStatus;
DROP TABLE IF EXISTS Sex;

CREATE TABLE IF NOT EXISTS Language (
	name varchar(20) NOT NULL UNIQUE,
	PRIMARY KEY(name)
) ENGINE=INNODB;
GRANT SELECT ON medspace.Language TO READONLY;

CREATE TABLE IF NOT EXISTS MaritalStatus (
	name varchar(20) NOT NULL UNIQUE,
	PRIMARY KEY(name)
) ENGINE=INNODB;
GRANT SELECT ON medspace.MaritalStatus TO READONLY;

CREATE TABLE IF NOT EXISTS Sex (
	name varchar(20) NOT NULL UNIQUE,
	PRIMARY KEY(name)
) ENGINE=INNODB;
GRANT SELECT ON medspace.Sex TO READONLY;

CREATE TABLE IF NOT EXISTS Patient (
	id varchar(20) NOT NULL UNIQUE,
	sex varchar(20) NOT NULL,
	dateOfBirth Date,
	maritalStatus varchar(20) NOT NULL,
	language varchar(20) NOT NULL,
	
	PRIMARY KEY(id),
	CONSTRAINT fk_sex FOREIGN KEY (sex) REFERENCES Sex(Name) ON UPDATE CASCADE ON DELETE RESTRICT,
	CONSTRAINT fk_maritalStatus FOREIGN KEY (maritalStatus) REFERENCES MaritalStatus(Name) ON UPDATE CASCADE ON DELETE RESTRICT,
	CONSTRAINT fk_language FOREIGN KEY (language) REFERENCES Language(Name) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=INNODB;
GRANT SELECT ON medspace.Patient TO READONLY;


INSERT INTO Language VALUES 
('Unknown'),
('English'),
('Spanish'),
('French'),
('German');

INSERT INTO MaritalStatus VALUES 
('Unknown'),
('Single'),
('Married'),
('Divorced'),
('Separated');

INSERT INTO Sex VALUES 
('Unknown'),
('Female'),
('Male');

INSERT INTO Patient VALUES 
('FB2ABB23-C9D0', 'Male', '1947-12-28', 'Unknown', 'English'),
('64182B95-EB72', 'Male', '1952-01-18', 'Married', 'Spanish'),
('DB22A4D9-7E4D', 'Female', '1970-07-25', 'Married', 'German'),
('6E70D84D-C75F', 'Male', '1979-01-04', 'Single', 'French'),
('C8556CC0-32FC', 'Female', '1921-04-11', 'Divorced', 'English');