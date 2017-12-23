-- MPC Database

-- :name drop-task-status-type :! :1
-- :doc Drop the task_status enumerated type, if it exists.
DROP TYPE IF EXISTS task_status CASCADE

-- :name create-task-status-type :! :1
-- :doc Create the task_status enumerated type.
CREATE TYPE task_status
AS ENUM (
    'not attempted',
    'in progress',
    'failed',
    'succeeded'
)

-- :name drop-script-format-type :! :1
-- :doc Drop the script_format enumerated type, if it exists.
DROP TYPE IF EXISTS script_format CASCADE

-- :name create-script-format-type :! :1
-- :doc Create the script_format enumerated type.
CREATE TYPE script_format
AS ENUM (
    'lexc',
    'regex'
)
