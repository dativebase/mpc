(ns mpc.db.phonologies-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [mpc.db :refer [connection-uri]]
            [mpc.db.phonologies :as phonologies]
            [mpc.test-core :refer [with-rollback create-destroy-tables]]
            [mpc.utils :as utils]))

(def db connection-uri)

(use-fixtures :each with-rollback)

(deftest create-phonology-table-test
  (testing "Create the phonology table"
    (let [db mpc.db/connection-uri]
      (is (false? (:exists (phonologies/table-exists? db))))
      (is (= 0 (phonologies/drop-task-status-type db)))
      (is (= 0 (phonologies/create-task-status-type db)))
      (is (= 0 (phonologies/create-phonology-table db)))
      (is (true? (:exists (phonologies/table-exists? db))))
      (is (= 0 (phonologies/drop-phonology-table db)))
      (is (= 0 (phonologies/drop-task-status-type db)))
      (is (false? (:exists (phonologies/table-exists? db)))))))
