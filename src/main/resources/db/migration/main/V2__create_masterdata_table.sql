CREATE TABLE master_data(
     id          character varying(256)  PRIMARY KEY,
     tenant_id    character varying(64)  NOT NULL,
     module_name character varying(64)  NOT NULL,
     master_name character varying(64)  NOT NULL,
     master_data JSONB  NOT NULL
);

CREATE TABLE master_data_schemas(
     master_name character varying(64)  PRIMARY KEY,
     master_data_schema JSONB  NOT NULL
);

CREATE TABLE master_config(
     id          character varying(256)  PRIMARY KEY,
     module_name character varying(64)  NOT NULL,
     master_name character varying(64)  NOT NULL,
     is_state_Level BOOLEAN,
     unique_keys JSONB
);