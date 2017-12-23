-- MPC Morphological Parsers

-- ============================================================================
-- Morphological Parser Table Creation Machinery
-- ============================================================================

-- :name table-exists? :? :1
-- :doc Return a boolean indicating whether the morphological_parser table
--      exists.
SELECT EXISTS (
   SELECT 1
   FROM   information_schema.tables
   WHERE  table_schema = 'public'
   AND    table_name = 'morphological_parser'
)

-- :name create-morphological-parser-table :! :1
-- :doc Create the morphological_parser table.
CREATE TABLE morphological_parser (
    id                  UUID,
    morphophonology_id  UUID REFERENCES morphophonology (id),
    candidate_ranker_id UUID REFERENCES candidate_ranker (id),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)

-- :name drop-morphological-parser-table :! :1
-- :doc Drop the morphological_parser table, if it exists.
DROP TABLE IF EXISTS morphological_parser

-- ============================================================================
-- Morphological Parser CRUD
-- ============================================================================

-- :name insert-morphological-parser :! :n
-- :doc Insert a single morphological parser and return the number of rows
--      affected.
INSERT INTO morphological_parser (
    id,
    morphophonology_id,
    candidate_ranker_id
) VALUES (
    :id,
    :morphophonology-id,
    :candidate-ranker-id
)

-- :name get-morphological-parser :? :1
-- :doc Get the morphological parser with the given id.
SELECT * FROM morphological_parser
WHERE id = :id

-- :name get-morphological-parsers :? :*
-- :doc Get all the morphological parsers.
SELECT * FROM morphological_parser

-- :name count-morphological-parsers :? :1
-- :doc Count the morphological parsers.
SELECT COUNT(id) FROM morphological_parser

-- :name update-morphological-parser :! :n
-- :doc Update the morphological parser with the given id.
--      Note that we must cast timeofday() to a timestamp here in order for the
--      tests, which work within a single transaction, to pass. See
--      https://www.postgresql.org/message-id/20020418144134.A16277%40zf.jcu.cz
--      https://stackoverflow.com/questions/1035980/update-timestamp-when-row-is-updated-in-postgresql
UPDATE morphological_parser
SET candidate_ranker_id = :candidate-ranker-id,
    morphophonology_id = :morphophonology-id,
    modified_at = timeofday()::timestamp
WHERE id = :id

-- :name delete-morphological-parser :! :n
-- :doc Delete the morphological parser with the given id.
DELETE FROM morphological_parser
WHERE id = :id
