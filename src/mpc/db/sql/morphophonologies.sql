-- MPC Morphophonologies

-- ============================================================================
-- Morphophonology Table Creation Machinery
-- ============================================================================

-- :name table-exists? :? :1
-- :doc Return a boolean indicating whether the morphophonology table exists.
SELECT EXISTS (
   SELECT 1
   FROM   information_schema.tables
   WHERE  table_schema = 'public'
   AND    table_name = 'morphophonology'
)

-- :name create-morphophonology-table :! :1
-- :doc Create the morphophonology table.
CREATE TABLE morphophonology (
    id              UUID,
    script          TEXT,
    morphology_id   UUID REFERENCES morphology (id),
    phonology_id    UUID REFERENCES phonology (id),
    generate_status task_status DEFAULT 'not attempted',
    compile_status  task_status DEFAULT 'not attempted',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)

-- :name drop-morphophonology-table :! :1
-- :doc Drop the morphophonology table, if it exists.
DROP TABLE IF EXISTS morphophonology

-- ============================================================================
-- Morphophonology CRUD
-- ============================================================================

-- :name insert-morphophonology :! :n
-- :doc Insert a single morphophonology and return the number of rows affected.
INSERT INTO morphophonology (
    id,
    script,
    morphology_id,
    phonology_id
) VALUES (
    :id,
    :script,
    :morphology-id,
    :phonology-id
)

-- :name get-morphophonology :? :1
-- :doc Get the morphophonology with the given id.
SELECT * FROM morphophonology
WHERE id = :id

-- :name get-morphophonologies :? :*
-- :doc Get all the morphophonologies.
SELECT * FROM morphophonology

-- :name count-morphophonologies :? :1
-- :doc Count the morphophonologies.
SELECT COUNT(id) FROM morphophonology

-- :name update-morphophonology :! :n
-- :doc Update the morphophonology with the given id.
--      Note that we must cast timeofday() to a timestamp here in order for the
--      tests, which work within a single transaction, to pass. See
--      https://www.postgresql.org/message-id/20020418144134.A16277%40zf.jcu.cz
--      https://stackoverflow.com/questions/1035980/update-timestamp-when-row-is-updated-in-postgresql
UPDATE morphophonology
SET script = :script,
    phonology_id = :phonology-id,
    morphology_id = :morphology-id,
    modified_at = timeofday()::timestamp
WHERE id = :id

-- :name delete-morphophonology :! :n
-- :doc Delete the morphophonology with the given id.
DELETE FROM morphophonology
WHERE id = :id
