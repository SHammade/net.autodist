CREATE TABLE Project (
  Name   varchar(255), 
  Package varchar(255),
  date timestamp DEFAULT SYSDATE,
  id      INTEGER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY 
);
CREATE TABLE Method (
  path           varchar(255) NOT NULL, 
  name           varchar(255) NOT NULL, 
  id              INTEGER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  projectid      integer(10) NOT NULL, 
  return_type    varchar(255) NOT NULL, 
  source		 text NOT NULL, 
  body           text NOT NULL,  
  FOREIGN KEY(Projectid) REFERENCES Project(id)
);
CREATE TABLE Parameter (
  id        INTEGER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  type     varchar(255) NOT NULL, 
  name     varchar(255) NOT NULL, 
  methodid integer(10) NOT NULL, 
  FOREIGN KEY(Methodid) REFERENCES Method(id)
);
CREATE TABLE Thrown_Exception (
  Methodid  integer(10) NOT NULL, 
  id         INTEGER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  exception varchar(255) NOT NULL , 
  FOREIGN KEY(Methodid) REFERENCES Method(id)
);
CREATE TABLE Annotation (
  methodid integer(10) NOT NULL, 
  id        INTEGER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  type     varchar(255) NOT NULL, 
  FOREIGN KEY(Methodid) REFERENCES Method(id));
CREATE TABLE Attribute (
  AnnotationsId integer(10) NOT NULL, 
  id             INTEGER  GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
  Type          varchar(255) NOT NULL, 
  Value         varchar(255), 
  FOREIGN KEY(AnnotationsId) REFERENCES Annotation(Id)
);

CREATE OR REPLACE VIEW SERVER_BY_METHOD AS
SELECT  ANNOTATION.ID AS AnnotationID, ANNOTATION.METHODID AS METHODID,
(SELECT VALUE FROM ATTRIBUTE WHERE LOWER(TYPE) = 'servername' AND ATTRIBUTE.ANNOTATIONSID = ANNOTATION.ID) AS server,
(SELECT VALUE FROM ATTRIBUTE WHERE LOWER(TYPE) = 'serverport' AND ATTRIBUTE.ANNOTATIONSID = ANNOTATION.ID) AS port
FROM ANNOTATION 
WHERE ANNOTATION.TYPE = 'RemoteCall';

CREATE OR replace VIEW THRIFT_SERVICE_NAMES AS
SELECT  SERVER_BY_METHOD.METHODID,
REPLACE(('Thriftservice'||'_'||SERVER_BY_METHOD.server||'_'||SERVER_BY_METHOD.port),'.','_') AS servicename,
REPLACE((METHOD.path||'_'||METHOD.name),'.','_') AS methodname
FROM SERVER_BY_METHOD INNER JOIN METHOD ON(SERVER_BY_METHOD.methodid = method.id);

