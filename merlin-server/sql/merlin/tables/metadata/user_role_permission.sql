create table metadata.user_role_permission(
  role text not null
    primary key
    references metadata.user_roles
      on update cascade
      on delete cascade,
  action_permissions jsonb not null default '{}',
  function_permissions jsonb not null default '{}'
);

comment on table metadata.user_role_permission is e''
  'Permissions for a role that cannot be expressed in Hasura. Permissions take the form {KEY:PERMISSION}.'
  'A list of valid KEYs and PERMISSIONs can be found at https://github.com/NASA-AMMOS/aerie/discussions/983#discussioncomment-6257146';
comment on column metadata.user_role_permission.role is e''
  'The role these permissions apply to.';
comment on column metadata.user_role_permission.action_permissions is ''
  'The permissions the role has on Hasura Actions.';
comment on column metadata.user_role_permission.function_permissions is ''
  'The permissions the role has on Hasura Functions.';

-- Permissions For Default Roles:
-- 'aerie_admin' permissions aren't specified since 'aerie_admin' is always considered to have "NO_CHECK" permissions
insert into metadata.user_role_permission(role, action_permissions, function_permissions)
values
  ('aerie_admin', '{}', '{}'),
  ('user',
   '{
      "check_constraints": "PLAN_OWNER_COLLABORATOR",
      "create_expansion_rule": "NO_CHECK",
      "create_expansion_set": "NO_CHECK",
      "expand_all_activities": "NO_CHECK",
      "insert_ext_dataset": "PLAN_OWNER",
      "resource_samples": "NO_CHECK",
      "schedule":"PLAN_OWNER_COLLABORATOR",
      "sequence_seq_json_bulk": "NO_CHECK",
      "simulate":"PLAN_OWNER_COLLABORATOR"
    }',
   '{
      "apply_preset": "PLAN_OWNER_COLLABORATOR",
      "begin_merge": "PLAN_OWNER_TARGET",
      "branch_plan": "NO_CHECK",
      "cancel_merge": "PLAN_OWNER_TARGET",
      "commit_merge": "PLAN_OWNER_TARGET",
      "create_merge_rq": "PLAN_OWNER_SOURCE",
      "create_snapshot": "PLAN_OWNER_COLLABORATOR",
      "delete_activity_reanchor": "PLAN_OWNER_COLLABORATOR",
      "delete_activity_reanchor_bulk": "PLAN_OWNER_COLLABORATOR",
      "delete_activity_reanchor_plan": "PLAN_OWNER_COLLABORATOR",
      "delete_activity_reanchor_plan_bulk": "PLAN_OWNER_COLLABORATOR",
      "delete_activity_subtree": "PLAN_OWNER_COLLABORATOR",
      "delete_activity_subtree_bulk": "PLAN_OWNER_COLLABORATOR",
      "deny_merge": "PLAN_OWNER_TARGET",
      "get_conflicting_activities": "NO_CHECK",
      "get_non_conflicting_activities": "NO_CHECK",
      "get_plan_history": "NO_CHECK",
      "restore_activity_changelog": "PLAN_OWNER_COLLABORATOR",
      "restore_snapshot": "PLAN_OWNER_COLLABORATOR",
      "set_resolution": "PLAN_OWNER_TARGET",
      "set_resolution_bulk": "PLAN_OWNER_TARGET",
      "withdraw_merge_rq": "PLAN_OWNER_SOURCE"
    }' ),
  ('viewer',
   '{
      "sequence_seq_json_bulk": "NO_CHECK",
      "resource_samples": "NO_CHECK"
    }',
   '{
      "get_conflicting_activities": "NO_CHECK",
      "get_non_conflicting_activities": "NO_CHECK",
      "get_plan_history": "NO_CHECK"
    }');
