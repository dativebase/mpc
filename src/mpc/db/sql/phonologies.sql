-- MPC Phonologies

-- ============================================================================
-- Phonology Table Creation Machinery
-- ============================================================================

-- :name table-exists? :? :1
-- :doc Return a boolean indicating whether the phonology table exists.
SELECT EXISTS (
   SELECT 1
   FROM   information_schema.tables
   WHERE  table_schema = 'public'
   AND    table_name = 'phonology'
)

-- :name create-phonology-table :! :1
-- :doc Create the phonology table.
CREATE TABLE phonology (
    id             UUID,
    script         TEXT,
    compile_status task_status DEFAULT 'not attempted',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)

-- :name drop-phonology-table :! :1
-- :doc Drop the phonology table, if it exists.
DROP TABLE IF EXISTS phonology

-- ============================================================================
-- Phonology CRUD
-- ============================================================================

-- :name insert-phonology :! :n
-- :doc Insert a single phonology and return the number of phonologies in the
--      table.
INSERT INTO phonology (id, script)
VALUES (:id, :script)

-- :name get-phonology :? :1
-- :doc Get the phonology with the given id.
SELECT * FROM phonology
WHERE id = :id

-- :name get-phonologies :? :*
-- :doc Get all the phonologies.
SELECT * FROM phonology

-- :name count-phonologies :? :1
-- :doc Count the phonologies.
SELECT COUNT(id) FROM phonology

-- :name update-phonology :! :n
-- :doc Update the phonology with the given id.
--      Note that we must cast timeofday() to a timestamp here in order for the
--      tests, which work within a single transaction, to pass. See
--      https://www.postgresql.org/message-id/20020418144134.A16277%40zf.jcu.cz
--      https://stackoverflow.com/questions/1035980/update-timestamp-when-row-is-updated-in-postgresql
UPDATE phonology
SET script = :script,
    modified_at = timeofday()::timestamp
WHERE id = :id

-- :name delete-phonology :! :n
-- :doc Delete the phonology with the given id.
DELETE FROM phonology
WHERE id = :id
