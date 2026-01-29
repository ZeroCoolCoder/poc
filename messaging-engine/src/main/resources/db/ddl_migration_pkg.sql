create table MIG_DDL_SNAPSHOT (
                                  snapshot_id   number generated always as identity primary key,
                                  created_ts    timestamp default systimestamp not null,
                                  owner         varchar2(128) not null,
                                  object_type   varchar2(30) not null,      -- 'CONSTRAINT' or 'INDEX'
                                  object_subtype varchar2(10),              -- 'P','U','R' for constraints
                                  table_name    varchar2(128),
                                  object_name   varchar2(128) not null,
                                  ddl_sql       clob not null
);

create index MIG_DDL_SNAP_IX1 on MIG_DDL_SNAPSHOT(owner, snapshot_id, object_type);
