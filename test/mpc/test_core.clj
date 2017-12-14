;; Common utilities to use across tests
(ns mpc.test-core
  (:require [korma.db :as db]))

(defn with-rollback
  "Test fixture for executing a test inside a database transaction
  that rolls back at the end so that database tests can remain isolated"
  [test-fn]
  (db/transaction
    (test-fn)
    (db/rollback)))
