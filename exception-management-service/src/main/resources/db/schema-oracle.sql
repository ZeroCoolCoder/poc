-- Exception Management Service – Oracle DDL (Tables + Indexes)

CREATE TABLE exception_definition (
    exception_code          VARCHAR2(50)     NOT NULL,
    exception_type          VARCHAR2(20)     NOT NULL,
    technical_description   VARCHAR2(1000)   NOT NULL,
    business_description    VARCHAR2(1000)   NOT NULL,
    http_status_code        NUMBER(3,0)      NOT NULL,
    severity                VARCHAR2(20)     NOT NULL,
    is_active               NUMBER(1,0)      DEFAULT 1 NOT NULL,
    lst_mod_chg_cd          VARCHAR2(1)      NOT NULL,
    lst_mod_user            VARCHAR2(10)     NOT NULL,
    lst_mod_ts              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_exception_definition PRIMARY KEY (exception_code),
    CONSTRAINT ck_exc_type CHECK (exception_type IN ('TECHNICAL','BUSINESS')),
    CONSTRAINT ck_exc_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT ck_exc_is_active CHECK (is_active IN (0,1)),
    CONSTRAINT ck_exc_chg_cd CHECK (lst_mod_chg_cd IN ('C','U','D'))
);

CREATE INDEX ix_exc_type ON exception_definition (exception_type);
CREATE INDEX ix_exc_severity ON exception_definition (severity);
CREATE INDEX ix_exc_active ON exception_definition (is_active);
CREATE INDEX ix_exc_lst_mod_ts ON exception_definition (lst_mod_ts);

CREATE TABLE exception_definition_hist (
    hist_id                 NUMBER GENERATED ALWAYS AS IDENTITY NOT NULL,
    hist_ts                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    hist_action             VARCHAR2(10)     NOT NULL,
    hist_user               VARCHAR2(255),
    hist_reason             VARCHAR2(4000),
    exception_code          VARCHAR2(50)     NOT NULL,
    exception_type          VARCHAR2(20)     NOT NULL,
    technical_description   VARCHAR2(1000)   NOT NULL,
    business_description    VARCHAR2(1000)   NOT NULL,
    http_status_code        NUMBER(3,0)      NOT NULL,
    severity                VARCHAR2(20)     NOT NULL,
    is_active               NUMBER(1,0)      NOT NULL,
    lst_mod_chg_cd          VARCHAR2(1)      NOT NULL,
    lst_mod_user            VARCHAR2(10)     NOT NULL,
    lst_mod_ts              TIMESTAMP        NOT NULL,
    CONSTRAINT pk_exception_definition_hist PRIMARY KEY (hist_id),
    CONSTRAINT ck_exc_hist_action CHECK (hist_action IN ('C','U','D')),
    CONSTRAINT ck_exc_hist_type CHECK (exception_type IN ('TECHNICAL','BUSINESS')),
    CONSTRAINT ck_exc_hist_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT ck_exc_hist_is_active CHECK (is_active IN (0,1)),
    CONSTRAINT ck_exc_hist_chg_cd CHECK (lst_mod_chg_cd IN ('C','U','D'))
);

CREATE INDEX ix_exc_hist_code ON exception_definition_hist (exception_code);
CREATE INDEX ix_exc_hist_ts ON exception_definition_hist (hist_ts);
CREATE INDEX ix_exc_hist_action ON exception_definition_hist (hist_action);
CREATE INDEX ix_exc_hist_user ON exception_definition_hist (hist_user);
