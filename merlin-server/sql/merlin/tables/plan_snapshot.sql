-- Snapshot is a collection of the state of all the activities as they were at the time of the snapshot
-- as well as other of the plan metadata
create table plan_snapshot(
  snapshot_id integer
    generated always as identity
    primary key,

  plan_id integer
    references plan
    on delete set null,
  revision integer not null,

  snapshot_name text,
  taken_by text,
  taken_at timestamptz not null default now(),
  constraint snapshot_name_unique_per_plan
		unique (plan_id, snapshot_name)
);

comment on table plan_snapshot is e''
  'A record of the state of a plan at a given time.';
comment on column plan_snapshot.snapshot_id is e''
	'The identifier of the snapshot.';
comment on column plan_snapshot.plan_id is e''
	'The plan that this is a snapshot of.';
comment on column plan_snapshot.revision is e''
	'The revision of the plan at the time the snapshot was taken.';
comment on column plan_snapshot.snapshot_name is e''
	'A human-readable name for the snapshot.';
comment on column plan_snapshot.taken_by is e''
	'The user who took the snapshot.';
comment on column plan_snapshot.taken_at is e''
	'The time that the snapshot was taken.';
