-- MPC Morphologies

-- ============================================================================
-- Morphology Table Creation Machinery
-- ============================================================================

-- :name table-exists? :? :1
-- :doc Return a boolean indicating whether the morphology table exists.
SELECT EXISTS (
   SELECT 1
   FROM   information_schema.tables
   WHERE  table_schema = 'public'
   AND    table_name = 'morphology'
)

-- :name create-morphology-table :! :1
-- :doc Create the morphology table.
CREATE TABLE morphology (
    id              UUID,
    script          TEXT,
    corpus          JSON,
    script_format   script_format DEFAULT 'lexc',
    generate_status task_status DEFAULT 'not attempted',
    compile_status  task_status DEFAULT 'not attempted',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)

-- :name drop-morphology-table :! :1
-- :doc Drop the morphology table, if it exists.
DROP TABLE IF EXISTS morphology

-- ============================================================================
-- Morphology CRUD
-- ============================================================================

-- :name insert-morphology :! :n
-- :doc Insert a single morphology and return the number of rows affected.
INSERT INTO morphology (id, corpus, script_format)
VALUES (:id, :corpus::JSON, :script-format::script_format)

-- :name get-morphology :? :1
-- :doc Get the morphology with the given id.
SELECT * FROM morphology
WHERE id = :id

-- :name get-morphologies :? :*
-- :doc Get all the morphologies.
SELECT * FROM morphology

-- :name count-morphologies :? :1
-- :doc Count the morphologies.
SELECT COUNT(id) FROM morphology

-- :name update-morphology :! :n
-- :doc Update the morphology with the given id.
--      Note that we must cast timeofday() to a timestamp here in order for the
--      tests, which work within a single transaction, to pass. See
--      https://www.postgresql.org/message-id/20020418144134.A16277%40zf.jcu.cz
--      https://stackoverflow.com/questions/1035980/update-timestamp-when-row-is-updated-in-postgresql
UPDATE morphology
SET corpus = :corpus::JSON,
    script_format = :script-format::script_format,
    modified_at = timeofday()::timestamp
WHERE id = :id

-- :name delete-morphology :! :n
-- :doc Delete the morphology with the given id.
DELETE FROM morphology
WHERE id = :id
