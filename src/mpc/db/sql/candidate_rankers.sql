-- MPC Candidate Rankers

-- ============================================================================
-- Candidate Ranker Table Creation Machinery
-- ============================================================================

-- :name table-exists? :? :1
-- :doc Return a boolean indicating whether the candidate ranker table exists.
SELECT EXISTS (
   SELECT 1
   FROM   information_schema.tables
   WHERE  table_schema = 'public'
   AND    table_name = 'candidate_ranker'
)

-- :name create-candidate-ranker-table :! :1
-- :doc Create the candidate ranker table.
CREATE TABLE candidate_ranker (
    id              UUID,
    corpus          JSON,
    estimate_status task_status DEFAULT 'not attempted',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)

-- :name drop-candidate-ranker-table :! :1
-- :doc Drop the candidate ranker table, if it exists.
DROP TABLE IF EXISTS candidate_ranker

-- ============================================================================
-- Candidate Ranker CRUD
-- ============================================================================

-- :name insert-candidate-ranker :! :n
-- :doc Insert a single candidate ranker and return the number of candidate rankers in the
--      table.
INSERT INTO candidate_ranker (id, corpus)
VALUES (:id, :corpus::JSON)

-- :name get-candidate-ranker :? :1
-- :doc Get the candidate ranker with the given id.
SELECT * FROM candidate_ranker
WHERE id = :id

-- :name get-candidate-rankers :? :*
-- :doc Get all the candidate rankers.
SELECT * FROM candidate_ranker

-- :name count-candidate-rankers :? :1
-- :doc Count the candidate rankers.
SELECT COUNT(id) FROM candidate_ranker

-- :name update-candidate-ranker :! :n
-- :doc Update the candidate ranker with the given id.
--      Note that we must cast timeofday() to a timestamp here in order for the
--      tests, which work within a single transaction, to pass. See
--      https://www.postgresql.org/message-id/20020418144134.A16277%40zf.jcu.cz
--      https://stackoverflow.com/questions/1035980/update-timestamp-when-row-is-updated-in-postgresql
UPDATE candidate_ranker
SET corpus = :corpus::JSON,
    estimate_status = :estimate-status::task_status,
    modified_at = timeofday()::timestamp
WHERE id = :id

-- :name delete-candidate-ranker :! :n
-- :doc Delete the candidate ranker with the given id.
DELETE FROM candidate_ranker
WHERE id = :id
