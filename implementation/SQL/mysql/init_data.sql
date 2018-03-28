SELECT * FROM medspace_test.adenocarcinoma;

truncate medspace_test.adenocarcinoma;

LOAD DATA LOCAL INFILE './data/adenocarcinoma_small.csv'
INTO TABLE medspace_test.adenocarcinoma
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, patient, metastasestype, SBR_grading,resection_margin, number_of_removed_lymphnodes) -- read one of the field to variable
; -- format this date-time variable



SELECT * FROM medspace_test.breastcancer;

truncate medspace_test.breastcancer;

LOAD DATA LOCAL INFILE './data/breastcancer_small.csv'
INTO TABLE medspace_test.breastcancer
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, patient, quadrant, breastside,microcalcification) -- read one of the field to variable
; -- format this date-time variable


SELECT * FROM medspace_test.breastcancer_has_receptor;

truncate medspace_test.breastcancer_has_receptor;

LOAD DATA LOCAL INFILE './data/breastcancerhasreceptor_small.csv'
INTO TABLE medspace_test.breastcancer_has_receptor
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(tumor, receptor) -- read one of the field to variable
; -- format this date-time variable


SELECT * FROM medspace_test.classification;

truncate medspace_test.classification;

LOAD DATA LOCAL INFILE './data/classification_small.csv'
INTO TABLE medspace_test.classification
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, tumor, patient, name) -- read one of the field to variable
; -- format this date-time variable



SELECT * FROM medspace_test.clinical_data;
truncate medspace_test.clinical_data;

LOAD DATA LOCAL INFILE './data/clinicaldata_small.csv'
INTO TABLE medspace_test.clinical_data
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, tumor, breastsize, history) -- read one of the field to variable
; -- format this date-time variable


SELECT * FROM medspace_test.decision;
truncate medspace_test.decision;

LOAD DATA LOCAL INFILE './data/decision_small.csv'
INTO TABLE medspace_test.decision
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, description, type, rcp) -- read one of the field to variable
; -- format this date-time variable



SELECT * FROM medspace_test.doctor;
truncate medspace_test.doctor;

LOAD DATA LOCAL INFILE './data/doctor_1of1.csv'
INTO TABLE medspace_test.doctor
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, firstname, lastname, hospital) -- read one of the field to variable
; -- format this date-time variable


SELECT * FROM medspace_test.doctor_participate_rcp;
truncate medspace_test.doctor_participate_rcp;

LOAD DATA LOCAL INFILE './data/doctorparticipatercp_small.csv'
INTO TABLE medspace_test.doctor_participate_rcp
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(doctor, rcp) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp


SELECT * FROM medspace_test.doctor_treat_patient;
truncate medspace_test.doctor_treat_patient;

LOAD DATA LOCAL INFILE './data/doctortreatpatient_small.csv'
INTO TABLE medspace_test.doctor_treat_patient
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(doctor, patient) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp



SELECT * FROM medspace_test.hospital;
truncate medspace_test.hospital;

LOAD DATA LOCAL INFILE './data/hospital_1of1.csv'
INTO TABLE medspace_test.hospital
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, place, name) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp



SELECT * FROM medspace_test.patient;
truncate medspace_test.patient;

LOAD DATA LOCAL INFILE './data/patient_small.csv'
INTO TABLE medspace_test.patient
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, firstname, lastname, maidenname, @birthday_variable, sex) -- read one of the field to variable
SET birthday = STR_TO_DATE(@birthday_variable, '%e.%c.%Y'); -- format this date-time variable




SELECT * FROM medspace_test.rcp;
truncate medspace_test.rcp;

LOAD DATA LOCAL INFILE './data/rcp_small.csv'
INTO TABLE medspace_test.rcp
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, place, @rcpdate_var, patient) -- read one of the field to variable
SET rcpdate = STR_TO_DATE(@rcpdate_var, '%e.%c.%Y'); -- format this date-time variable



SELECT * FROM medspace_test.rcp_cause;
truncate medspace_test.rcp_cause;

LOAD DATA LOCAL INFILE './data/rcpcause_small.csv'
INTO TABLE medspace_test.rcp_cause
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, description, rcp) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp



SELECT * FROM medspace_test.receptor;
truncate medspace_test.receptor;

LOAD DATA LOCAL INFILE './data/receptor_1of1.csv'
INTO TABLE medspace_test.receptor
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, description) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp





SELECT * FROM medspace_test.therapy;
truncate medspace_test.therapy;

LOAD DATA LOCAL INFILE './data/therapy_small.csv'
INTO TABLE medspace_test.therapy
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, rcp, place, comment) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp


SELECT * FROM medspace_test.tnm;
truncate medspace_test.tnm;

LOAD DATA LOCAL INFILE './data/tnm_small.csv'
INTO TABLE medspace_test.tnm
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, tumor, patient, radicalism, metastases, tumorsize, affected_lymphnodes) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp




SELECT * FROM medspace_test.treatment;
truncate medspace_test.treatment;

LOAD DATA LOCAL INFILE './data/treatment_small.csv'
INTO TABLE medspace_test.treatment
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, rcp, therapy, duration, sequence, description, type) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp




SELECT * FROM medspace_test.tumor;
truncate medspace_test.tumor;

LOAD DATA LOCAL INFILE './data/tumor_small.csv'
INTO TABLE medspace_test.tumor
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, patient, name) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp



SELECT * FROM medspace_test.vannuysindex;
truncate medspace_test.vannuysindex;

LOAD DATA LOCAL INFILE './data/vannuysindex_small.csv'
INTO TABLE medspace_test.vannuysindex
FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
(id, tumor, patient, surgeryreport, grading, resectionmargin, tumorsize) -- read one of the field to variable
; -- format this date-time variabledoctor_participate_rcp