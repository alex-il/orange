
/* Drop Triggers */

DROP TRIGGER TRI_ESB_CONNECTION_CONN_ID;
DROP TRIGGER TRI_ESB_CON_MAPPING_CONMAP_ID;
DROP TRIGGER TRI_ESB_DEFAULT_CONNECTION_DEF_CONN_ID;
DROP TRIGGER TRI_ESB_ENVIRONMENT_ENV_ID;
DROP TRIGGER TRI_ESB_PROVIDER_PROVIDER_ID;
DROP TRIGGER TRI_ESB_SERVICE_SERVICE_ID;
DROP TRIGGER TRI_ESB_SOURCE_SOURCE_ID;
DROP TRIGGER TRI_ESB_TYPE_TYPE_ID;



/* Drop Tables */

DROP TABLE ESB_CON_MAPPING;
DROP TABLE ESB_DEPLOYMENT;
DROP TABLE ESB_DEFAULT_CONNECTION;
DROP TABLE ESB_CONNECTION;
DROP TABLE ESB_ENVIRONMENT;
DROP TABLE ESB_PROVIDER;
DROP TABLE ESB_SERVICE;
DROP TABLE ESB_SOURCE;
DROP TABLE ESB_TYPE;



/* Drop Sequences */

DROP SEQUENCE SEQ_ESB_CONNECTION_CONN_ID;
DROP SEQUENCE SEQ_ESB_CON_MAPPING_CONMAP_ID;
DROP SEQUENCE SEQ_ESB_DEFAULT_CONNECTION_DEF_CONN_ID;
DROP SEQUENCE SEQ_ESB_ENVIRONMENT_ENV_ID;
DROP SEQUENCE SEQ_ESB_PROVIDER_PROVIDER_ID;
DROP SEQUENCE SEQ_ESB_SERVICE_SERVICE_ID;
DROP SEQUENCE SEQ_ESB_SOURCE_SOURCE_ID;
DROP SEQUENCE SEQ_ESB_TYPE_TYPE_ID;




/* Create Sequences */

CREATE SEQUENCE SEQ_ESB_CONNECTION_CONN_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_CON_MAPPING_CONMAP_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_DEFAULT_CONNECTION_DEF_CONN_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_ENVIRONMENT_ENV_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_PROVIDER_PROVIDER_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_SERVICE_SERVICE_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_SOURCE_SOURCE_ID INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_ESB_TYPE_TYPE_ID INCREMENT BY 1 START WITH 1;



/* Create Tables */

CREATE TABLE ESB_CONNECTION
(
	CONN_ID NUMBER NOT NULL UNIQUE,
	CONN_NAME VARCHAR2(200) NOT NULL UNIQUE,
	CONN_DESC VARCHAR2(500),
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (CONN_ID)
);


CREATE TABLE ESB_CON_MAPPING
(
	CONMAP_ID NUMBER NOT NULL UNIQUE,
	CONMAP_DESC VARCHAR2(500),
	CONN_HOST VARCHAR2(60),
	CONN_PORT VARCHAR2(8),
	CONN_SCHEMA VARCHAR2(60),
	CONN_USER VARCHAR2(55),
	CONN_PWD VARCHAR2(25),
	CONMAP_JDBC_MIN_CONCURRENCY VARCHAR2(10),
	CONMAP_JDBC_MAX_CONCURRENCY VARCHAR2(10),
	CONMAP_JDBC_CLASS VARCHAR2(10),
	CONN_ID NUMBER NOT NULL,
	TYPE_ID NUMBER NOT NULL,
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (CONMAP_ID)
);


CREATE TABLE ESB_DEFAULT_CONNECTION
(
	DEF_CONN_ID NUMBER NOT NULL,
	DEF_CONN_NAME VARCHAR2(200) NOT NULL UNIQUE,
	DEFAULT_CONN_DESC VARCHAR2(500),
	PROVIDER_ID NUMBER NOT NULL,
	ENV_ID NUMBER NOT NULL,
	CONN_ID NUMBER NOT NULL,
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (DEF_CONN_ID)
);


CREATE TABLE ESB_DEPLOYMENT
(
	SERVICE_ID NUMBER NOT NULL,
	DEPLOYMENT_DESC VARCHAR2(500),
	DEPLOYMENT_CLIENTS VARCHAR2(60),
	DEPLOYMENT_ENABLED VARCHAR2(10),
	DEPLOYMENT_STATUS VARCHAR2(30),
	DEF_CONN_ID NUMBER NOT NULL,
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60)
);


CREATE TABLE ESB_ENVIRONMENT
(
	ENV_ID NUMBER NOT NULL UNIQUE,
	ENV_NAME VARCHAR2(60) NOT NULL UNIQUE,
	ENV_DESC VARCHAR2(500),
	ENV_CLIENTS VARCHAR2(60),
	JMS_PROVIDER_URL VARCHAR2(60),
	JMS_Q_CONN_FACTORY VARCHAR2(40),
	JMS_TRACER_PROVIDER_URL VARCHAR2(100),
	JMS_TRACER_Q_CONN_FACTORY VARCHAR2(40),
	JMS_INBOUND_PREFIX VARCHAR2(30) DEFAULT '''Inbound: ''',
	SSG_HOST VARCHAR2(30),
	SSG_PORT VARCHAR2(8),
	SSG_USER VARCHAR2(30),
	SSG_PWD VARCHAR2(30),
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (ENV_ID)
);


CREATE TABLE ESB_PROVIDER
(
	PROVIDER_ID NUMBER NOT NULL UNIQUE,
	PROVIDER_NAME ,
	PROVIDER_DESC VARCHAR2(500),
	UPDATE_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (PROVIDER_ID)
);


CREATE TABLE ESB_SERVICE
(
	SERVICE_ID NUMBER NOT NULL UNIQUE,
	SERVICE_NAME VARCHAR2(160) NOT NULL UNIQUE,
	SERVICE_DESC VARCHAR2(500),
	VERSION_NUMBER VARCHAR2(10),
	IS_ASYNC CHAR(1),
	IS_PARSED VARCHAR2(8),
	SERVICE_ARGUMENTS VARCHAR2(4000),
	ETA NUMBER,
	INVOCATIONS_PER_MINUTE NUMBER,
	PARAMETER_LIST                   VARCHAR2(4000),
	SERVICE_TARGET_ENC VARCHAR2(50),
	SERVICE_SOURCE_ENC VARCHAR2(50),
	METHOD VARCHAR2(30),
	URL_ENCODING VARCHAR2(30),
	SERVICE_NAME_CONCURRENCY_LIMIT NUMBER,
	TYPE_ID NUMBER NOT NULL,
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (SERVICE_ID)
);


CREATE TABLE ESB_SOURCE
(
	SOURCE_ID NUMBER NOT NULL UNIQUE,
	SOURCE_NAME VARCHAR2(100) NOT NULL UNIQUE,
	SOURCE_DESC VARCHAR2(330) NOT NULL UNIQUE,
	SOURCE_USER VARCHAR2(33),
	SOURCE_PASSWORD VARCHAR2(33),
	SOURCE_HOST VARCHAR2(33),
	SOURCE_PORT VARCHAR2(33),
	VERSION VARCHAR2(25),
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (SOURCE_ID)
);


CREATE TABLE ESB_TYPE
(
	TYPE_ID NUMBER NOT NULL UNIQUE,
	TYPE_NAME VARCHAR2(60) NOT NULL UNIQUE,
	TYPE_DESC VARCHAR2(350),
	CREATION_DATE DATE,
	UPDATE_DATE DATE,
	USER_ID VARCHAR2(60),
	PRIMARY KEY (TYPE_ID)
);



/* Create Foreign Keys */

ALTER TABLE ESB_CON_MAPPING
	ADD FOREIGN KEY (CONN_ID)
	REFERENCES ESB_CONNECTION (CONN_ID)
;


ALTER TABLE ESB_DEFAULT_CONNECTION
	ADD FOREIGN KEY (CONN_ID)
	REFERENCES ESB_CONNECTION (CONN_ID)
;


ALTER TABLE ESB_DEPLOYMENT
	ADD FOREIGN KEY (DEF_CONN_ID)
	REFERENCES ESB_DEFAULT_CONNECTION (DEF_CONN_ID)
;


ALTER TABLE ESB_DEFAULT_CONNECTION
	ADD FOREIGN KEY (ENV_ID)
	REFERENCES ESB_ENVIRONMENT (ENV_ID)
;


ALTER TABLE ESB_DEFAULT_CONNECTION
	ADD FOREIGN KEY (PROVIDER_ID)
	REFERENCES ESB_PROVIDER (PROVIDER_ID)
;


ALTER TABLE ESB_DEPLOYMENT
	ADD FOREIGN KEY (SERVICE_ID)
	REFERENCES ESB_SERVICE (SERVICE_ID)
;


ALTER TABLE ESB_CON_MAPPING
	ADD FOREIGN KEY (TYPE_ID)
	REFERENCES ESB_TYPE (TYPE_ID)
;


ALTER TABLE ESB_SERVICE
	ADD FOREIGN KEY (TYPE_ID)
	REFERENCES ESB_TYPE (TYPE_ID)
;



/* Create Triggers */

CREATE TRIGGER TRI_ESB_CONNECTION_CONN_ID BEFORE INSERT ON ESB_CONNECTION
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_CONNECTION_CONN_ID.NEXTVAL
	INTO :NEW.CONN_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_CON_MAPPING_CONMAP_ID BEFORE INSERT ON ESB_CON_MAPPING
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_CON_MAPPING_CONMAP_ID.NEXTVAL
	INTO :NEW.CONMAP_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_DEFAULT_CONNECTION_DEF_CONN_ID BEFORE INSERT ON ESB_DEFAULT_CONNECTION
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_DEFAULT_CONNECTION_DEF_CONN_ID.NEXTVAL
	INTO :NEW.DEF_CONN_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_ENVIRONMENT_ENV_ID BEFORE INSERT ON ESB_ENVIRONMENT
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_ENVIRONMENT_ENV_ID.NEXTVAL
	INTO :NEW.ENV_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_PROVIDER_PROVIDER_ID BEFORE INSERT ON ESB_PROVIDER
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_PROVIDER_PROVIDER_ID.NEXTVAL
	INTO :NEW.PROVIDER_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_SERVICE_SERVICE_ID BEFORE INSERT ON ESB_SERVICE
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_SERVICE_SERVICE_ID.NEXTVAL
	INTO :NEW.SERVICE_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_SOURCE_SOURCE_ID BEFORE INSERT ON ESB_SOURCE
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_SOURCE_SOURCE_ID.NEXTVAL
	INTO :NEW.SOURCE_ID
	FROM DUAL;
END;
CREATE TRIGGER TRI_ESB_TYPE_TYPE_ID BEFORE INSERT ON ESB_TYPE
FOR EACH ROW
BEGIN
	SELECT SEQ_ESB_TYPE_TYPE_ID.NEXTVAL
	INTO :NEW.TYPE_ID
	FROM DUAL;
END;



