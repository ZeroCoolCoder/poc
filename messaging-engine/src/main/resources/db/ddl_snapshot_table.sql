create or replace package MIG_DDL_MGR authid current_user as
  function snapshot_and_drop(
      p_owner      in varchar2 default user,
      p_table_like in varchar2 default '%'
  ) return number;

  procedure restore(p_snapshot_id in number);
end MIG_DDL_MGR;
/
create or replace package body MIG_DDL_MGR as

  -- Get object DDL with stable transforms
  function get_ddl(p_obj_type varchar2, p_name varchar2, p_owner varchar2) return clob is
    h  number;
    dd clob;
begin
    -- Ensure metadata is predictable
    dbms_metadata.set_transform_param(dbms_metadata.session_transform, 'STORAGE', false);
    dbms_metadata.set_transform_param(dbms_metadata.session_transform, 'SEGMENT_ATTRIBUTES', false);
    dbms_metadata.set_transform_param(dbms_metadata.session_transform, 'SQLTERMINATOR', true);
    dbms_metadata.set_transform_param(dbms_metadata.session_transform, 'PRETTY', true);

    dd := dbms_metadata.get_ddl(p_obj_type, p_name, p_owner);
return dd;
exception
    when others then
      -- If GET_DDL fails for some object, bubble it up (better to fail early)
      raise;
end;

  function snapshot_and_drop(
      p_owner      in varchar2 default user,
      p_table_like in varchar2 default '%'
  ) return number is
    v_owner       varchar2(128) := upper(p_owner);
    v_snapshot_id number;
begin
    -- create snapshot header by inserting a dummy row then using its identity
insert into MIG_DDL_SNAPSHOT(owner, object_type, object_name, ddl_sql)
values (v_owner, 'SNAPSHOT', 'SNAPSHOT_HEADER', to_clob('/*header*/'));
select max(snapshot_id) into v_snapshot_id
from MIG_DDL_SNAPSHOT
where owner = v_owner and object_type = 'SNAPSHOT';

-- 1) Save FK constraint DDL (constraint_type='R')
for c in (
      select c.owner, c.table_name, c.constraint_name, c.constraint_type
      from   all_constraints c
      where  c.owner = v_owner
      and    c.constraint_type = 'R'
      and    c.table_name like upper(p_table_like) escape '\'
      and    c.status = 'ENABLED'
    ) loop
      insert into MIG_DDL_SNAPSHOT(owner, object_type, object_subtype, table_name, object_name, ddl_sql)
      values (c.owner, 'CONSTRAINT', c.constraint_type, c.table_name, c.constraint_name,
              get_ddl('REF_CONSTRAINT', c.constraint_name, c.owner));
end loop;

    -- 2) Save PK/UK constraint DDL (P/U)
for c in (
      select c.owner, c.table_name, c.constraint_name, c.constraint_type
      from   all_constraints c
      where  c.owner = v_owner
      and    c.constraint_type in ('P','U')
      and    c.table_name like upper(p_table_like) escape '\'
      and    c.status = 'ENABLED'
    ) loop
      insert into MIG_DDL_SNAPSHOT(owner, object_type, object_subtype, table_name, object_name, ddl_sql)
      values (c.owner, 'CONSTRAINT', c.constraint_type, c.table_name, c.constraint_name,
              get_ddl('CONSTRAINT', c.constraint_name, c.owner));
end loop;

    -- 3) Save NON-constraint indexes DDL
    -- Exclude indexes that are explicitly tied to constraints via ALL_CONSTRAINTS.index_name
for i in (
      select i.owner, i.table_name, i.index_name
      from   all_indexes i
      where  i.owner = v_owner
      and    i.table_name like upper(p_table_like) escape '\'
      and    i.generated = 'N'
      and    i.index_type <> 'LOB'
      and    i.index_name not in (
               select nvl(c.index_name,'-')
               from   all_constraints c
               where  c.owner = v_owner
               and    c.table_name like upper(p_table_like) escape '\'
               and    c.constraint_type in ('P','U','R')
             )
    ) loop
      insert into MIG_DDL_SNAPSHOT(owner, object_type, table_name, object_name, ddl_sql)
      values (i.owner, 'INDEX', i.table_name, i.index_name,
              get_ddl('INDEX', i.index_name, i.owner));
end loop;

    -- 4) Drop in safe order: FK -> PK/UK -> remaining indexes
for c in (
      select owner, table_name, constraint_name
      from   all_constraints
      where  owner = v_owner
      and    constraint_type = 'R'
      and    table_name like upper(p_table_like) escape '\'
      and    status = 'ENABLED'
      order  by table_name, constraint_name
    ) loop
      execute immediate 'alter table "'||c.owner||'"."'||c.table_name||'" drop constraint "'||c.constraint_name||'"';
end loop;

for c in (
      select owner, table_name, constraint_name
      from   all_constraints
      where  owner = v_owner
      and    constraint_type in ('P','U')
      and    table_name like upper(p_table_like) escape '\'
      and    status = 'ENABLED'
      order  by constraint_type, table_name, constraint_name
    ) loop
      execute immediate 'alter table "'||c.owner||'"."'||c.table_name||'" drop constraint "'||c.constraint_name||'"';
end loop;

for i in (
      select owner, index_name
      from   all_indexes
      where  owner = v_owner
      and    table_name like upper(p_table_like) escape '\'
      and    generated = 'N'
      and    index_type <> 'LOB'
      and    index_name not in (
               select nvl(c.index_name,'-')
               from   all_constraints c
               where  c.owner = v_owner
               and    c.table_name like upper(p_table_like) escape '\'
               and    c.constraint_type in ('P','U','R')
             )
      order by index_name
    ) loop
      execute immediate 'drop index "'||i.owner||'"."'||i.index_name||'"';
end loop;

commit;
return v_snapshot_id;
end snapshot_and_drop;

  procedure restore(p_snapshot_id in number) is
begin
    -- Restore PK/UK first
for r in (
      select ddl_sql
      from   MIG_DDL_SNAPSHOT
      where  snapshot_id = p_snapshot_id
      and    object_type = 'CONSTRAINT'
      and    object_subtype in ('P','U')
      order  by object_subtype, table_name, object_name
    ) loop
      execute immediate r.ddl_sql;
end loop;

    -- Restore non-constraint indexes next
for r in (
      select ddl_sql
      from   MIG_DDL_SNAPSHOT
      where  snapshot_id = p_snapshot_id
      and    object_type = 'INDEX'
      order  by table_name, object_name
    ) loop
      execute immediate r.ddl_sql;
end loop;

    -- Restore FKs last
for r in (
      select ddl_sql
      from   MIG_DDL_SNAPSHOT
      where  snapshot_id = p_snapshot_id
      and    object_type = 'CONSTRAINT'
      and    object_subtype = 'R'
      order  by table_name, object_name
    ) loop
      execute immediate r.ddl_sql;
end loop;

commit;
end restore;

end MIG_DDL_MGR;
/
