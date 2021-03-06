/***********************************
 * Database and read-user creation 
 **********************************/

DROP DATABASE IF EXISTS medspace; 
CREATE DATABASE IF NOT EXISTS medspace;

DROP USER IF EXISTS medspace_client@'%';
CREATE USER IF NOT EXISTS medspace_client@'%' IDENTIFIED BY 'k4N!rT';
GRANT SELECT ON medspace.* TO medspace_client@'%';

/*************************************
 *	Database Initialization
 ************************************/

USE medspace;
Drop TABLE IF EXISTS Patient;
DROP TABLE IF EXISTS Language;
DROP TABLE IF EXISTS MaritalStatus;
DROP TABLE IF EXISTS Sex;

CREATE TABLE IF NOT EXISTS Language (
	name varchar(20) NOT NULL UNIQUE,
	PRIMARY KEY(name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS MaritalStatus (
	name varchar(20) NOT NULL UNIQUE,
	PRIMARY KEY(name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS Sex (
	name varchar(20) NOT NULL UNIQUE,
	PRIMARY KEY(name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS Patient (
	id varchar(20) NOT NULL UNIQUE PRIMARY KEY,
	sex varchar(20) NOT NULL,
	dateOfBirth Date,
	maritalStatus varchar(20) NOT NULL,
	language varchar(20) NOT NULL,
	
	CONSTRAINT fk_sex FOREIGN KEY (sex) REFERENCES Sex(Name) ON UPDATE CASCADE ON DELETE RESTRICT,
	CONSTRAINT fk_maritalStatus FOREIGN KEY (maritalStatus) REFERENCES MaritalStatus(Name) ON UPDATE CASCADE ON DELETE RESTRICT,
	CONSTRAINT fk_language FOREIGN KEY (language) REFERENCES Language(Name) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=INNODB;


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