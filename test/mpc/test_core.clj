;; Common utilities to use across tests
(ns mpc.test-core
  (:require [clojure.java.jdbc :as jdbc]
            [mpc.db]
            [mpc.db.phonologies]))

(declare ^:dynamic *txn*)

(defn create-destroy-tables
  "Test :once-type fixture that creates all the needed tables in the db and
  then destroys them after the tests have run."
  [tests-fn]
  (jdbc/with-db-transaction [trans-conn mpc.db/connection-uri]
    (jdbc/db-set-rollback-only! trans-conn)
    (mpc.db/create-tables mpc.db/connection-uri)
    (tests-fn)
    (mpc.db/drop-tables mpc.db/connection-uri)))

(defn with-rollback
  "Test fixture for executing a test inside a database transaction
  that rolls back at the end so that database tests can remain isolated"
  [test-fn]
  (jdbc/with-db-transaction [trans-conn mpc.db/connection-uri]
    (jdbc/db-set-rollback-only! trans-conn)
    (binding [*txn* trans-conn] (test-fn))))
