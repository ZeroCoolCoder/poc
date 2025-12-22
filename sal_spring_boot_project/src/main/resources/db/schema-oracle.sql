-- SAL – Oracle DDL (Tables + Indexes)
CREATE TABLE sal_metadata (
    sal_uuid          VARCHAR2(36)     NOT NULL,
    version           NUMBER(10,0)     NOT NULL,
    sal_name          VARCHAR2(255)    NOT NULL,
    sal_description   VARCHAR2(1000),
    sal_type          VARCHAR2(50)     NOT NULL,
    sal_metadata      CLOB,
    size_in_bytes     NUMBER(19,0),
    status            VARCHAR2(30)     DEFAULT 'PENDING_UPLOAD' NOT NULL,
    is_latest         NUMBER(1,0)      DEFAULT 0 NOT NULL,
    is_compressed     NUMBER(1,0)      DEFAULT 0 NOT NULL,
    compression_type  VARCHAR2(30),
    owner_id          VARCHAR2(10)     NOT NULL,
    lst_mod_chg_cd    VARCHAR2(1)      NOT NULL,
    lst_mod_user      VARCHAR2(10)     NOT NULL,
    lst_mod_ts        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_sal_metadata PRIMARY KEY (sal_uuid, version),
    CONSTRAINT ck_sal_status CHECK (status IN ('PENDING_UPLOAD','AVAILABLE','FAILED','DELETED')),
    CONSTRAINT ck_sal_is_latest CHECK (is_latest IN (0,1)),
    CONSTRAINT ck_sal_is_compressed CHECK (is_compressed IN (0,1)),
    CONSTRAINT ck_sal_type CHECK (sal_type IN ('FILE_SYSTEM','S3','DATABASE','REST')),
    CONSTRAINT ck_sal_metadata_json CHECK (sal_metadata IS JSON),
    CONSTRAINT ck_sal_metadata_chg_cd CHECK (lst_mod_chg_cd IN ('C','U','D')),
    CONSTRAINT ck_sal_compression_type CHECK (compression_type IS NULL OR compression_type IN ('GZIP','ZSTD','NONE'))
);

CREATE UNIQUE INDEX ux_sal_one_latest
ON sal_metadata (sal_uuid, CASE WHEN is_latest = 1 THEN 1 ELSE NULL END);

CREATE INDEX ix_sal_uuid_latest ON sal_metadata (sal_uuid, is_latest);
CREATE INDEX ix_sal_name ON sal_metadata (sal_name);
CREATE INDEX ix_sal_owner ON sal_metadata (owner_id);
CREATE INDEX ix_sal_status ON sal_metadata (status);
CREATE INDEX ix_sal_last_modified_ts ON sal_metadata (lst_mod_ts);

CREATE TABLE sal_metadata_hist (
    hist_id           NUMBER GENERATED ALWAYS AS IDENTITY NOT NULL,
    hist_ts           TIMESTAMP DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    hist_action       VARCHAR2(10) NOT NULL,
    hist_user         VARCHAR2(255),
    hist_reason       VARCHAR2(4000),
    sal_uuid          VARCHAR2(36)     NOT NULL,
    version           NUMBER(10,0)     NOT NULL,
    sal_name          VARCHAR2(255)    NOT NULL,
    sal_description   VARCHAR2(1000),
    sal_type          VARCHAR2(50)     NOT NULL,
    sal_metadata      CLOB,
    size_in_bytes     NUMBER(19,0),
    status            VARCHAR2(30)     NOT NULL,
    is_latest         NUMBER(1,0)      NOT NULL,
    is_compressed     NUMBER(1,0)      NOT NULL,
    compression_type  VARCHAR2(30),
    owner_id          VARCHAR2(10)     NOT NULL,
    lst_mod_chg_cd    VARCHAR2(1)      NOT NULL,
    lst_mod_user      VARCHAR2(10)     NOT NULL,
    lst_mod_ts        TIMESTAMP        NOT NULL,
    CONSTRAINT pk_sal_metadata_hist PRIMARY KEY (hist_id),
    CONSTRAINT ck_sal_hist_action CHECK (hist_action IN ('C','U','D')),
    CONSTRAINT ck_sal_hist_status CHECK (status IN ('PENDING_UPLOAD','AVAILABLE','FAILED','DELETED')),
    CONSTRAINT ck_sal_hist_is_latest CHECK (is_latest IN (0,1)),
    CONSTRAINT ck_sal_hist_is_compressed CHECK (is_compressed IN (0,1)),
    CONSTRAINT ck_sal_hist_type CHECK (sal_type IN ('FILE_SYSTEM','S3','DATABASE','REST')),
    CONSTRAINT ck_sal_hist_metadata_json CHECK (sal_metadata IS JSON),
    CONSTRAINT ck_sal_hist_chg_cd CHECK (lst_mod_chg_cd IN ('C','U','D')),
    CONSTRAINT ck_sal_hist_compression_type CHECK (compression_type IS NULL OR compression_type IN ('GZIP','ZSTD','NONE'))
);

CREATE INDEX ix_sal_hist_key ON sal_metadata_hist (sal_uuid, version);
CREATE INDEX ix_sal_hist_ts ON sal_metadata_hist (hist_ts);
CREATE INDEX ix_sal_hist_action ON sal_metadata_hist (hist_action);
CREATE INDEX ix_sal_hist_user ON sal_metadata_hist (hist_user);
CREATE INDEX ix_sal_hist_owner ON sal_metadata_hist (owner_id);
