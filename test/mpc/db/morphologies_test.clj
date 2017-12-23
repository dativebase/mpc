(ns mpc.db.morphologies-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [cheshire.core :refer [parse-string generate-string]]
            [mpc.db.morphologies :as morphologies]
            [mpc.test-core :refer [*txn* with-rollback create-destroy-tables]]
            [mpc.utils :as utils]
            [mpc.db.test-db-helpers :as h]))

;; Create all tables before tests and destroy them afterwards.
(use-fixtures :once create-destroy-tables)

;; Perform all db mutations in a transaction that is rolled back after each
;; test.
(use-fixtures :each with-rollback)

(deftest insert-get-morphology-test
  (testing "Insert into the morphology table and get by id"
    (let [morphology-id (utils/uuid)
          morphology-map {:id morphology-id
                          :script-format h/script-format
                          :corpus h/corpus-string}
          rows-affected-count
          (morphologies/insert-morphology *txn* morphology-map)
          morphology-row (morphologies/get-morphology *txn* {:id morphology-id})
          created-at (:created_at morphology-row)
          modified-at (:modified_at morphology-row)]
      (is (= 1 rows-affected-count))
      (is (= (:created_at morphology-row) (:modified_at morphology-row)))
      (is (instance? java.util.Date (:created_at morphology-row)))
      (is (= "not attempted" (:compile_status morphology-row)))
      (is (= "not attempted" (:generate_status morphology-row)))
      (is (= morphology-id (:id morphology-row)))
      (is (= h/corpus (:corpus morphology-row)))
      (is (= h/corpus (:corpus morphology-row))))))

(deftest insert-get-all-morphology-test
  (testing "Insert multiple morphologies and get them all"
    (let [morphology-1-id (utils/uuid)
          morphology-1-map {:id morphology-1-id
                            :script-format h/script-format
                            :corpus h/corpus-1-string}
          rows-affected-count-1
          (morphologies/insert-morphology *txn* morphology-1-map)
          morphology-2-id (utils/uuid)
          morphology-2-map {:id morphology-2-id
                            :script-format h/script-format
                            :corpus h/corpus-2-string}
          rows-affected-count-2
          (morphologies/insert-morphology *txn* morphology-2-map)
          morphology-row-set (morphologies/get-morphologies *txn*)]
      (is (= 1 rows-affected-count-1))
      (is (= 1 rows-affected-count-2))
      (is (= 2 (count morphology-row-set)))
      (let [morphology-1-row
            (first (filter #(= morphology-1-id (:id %)) morphology-row-set))]
        (is (= h/corpus-1 (:corpus morphology-1-row))))
      (let [morphology-2-row
            (first (filter #(= morphology-2-id (:id %)) morphology-row-set))]
        (is (= h/corpus-2 (:corpus morphology-2-row)))))))

(deftest update-morphology-test
  (testing "Update a row in the morphology table"
    (let [initial-count (:count (morphologies/count-morphologies *txn*))
          ;; Create the morphology
          morphology-id (utils/uuid)
          initial-corpus-string h/corpus-1-string
          initial-corpus h/corpus-1
          morphology-map {:id morphology-id
                          :corpus initial-corpus-string
                          :script-format h/script-format}
          _ (morphologies/insert-morphology *txn* morphology-map)
          post-insert-morphology-row
          (morphologies/get-morphology *txn* {:id morphology-id})
          post-insert-created-at (:created_at post-insert-morphology-row)
          post-insert-modified-at (:modified_at post-insert-morphology-row)
          post-insert-count (:count (morphologies/count-morphologies *txn*))
          ;; Update the morphology
          new-corpus-string h/corpus-2-string
          new-corpus h/corpus-2
          _ (morphologies/update-morphology
              *txn* {:id morphology-id
                     :corpus new-corpus-string
                     :script-format "regex"})
          post-update-count (:count (morphologies/count-morphologies *txn*))
          post-update-morphology-row
          (morphologies/get-morphology *txn* {:id morphology-id})
          post-update-created-at (:created_at post-update-morphology-row)
          post-update-modified-at (:modified_at post-update-morphology-row)]
      (is (= initial-corpus (:corpus post-insert-morphology-row)))
      (is (= h/script-format (:script_format post-insert-morphology-row)))
      (is (= new-corpus (:corpus post-update-morphology-row)))
      (is (= "regex" (:script_format post-update-morphology-row)))
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 1 post-update-count))
      (is (= post-insert-created-at post-update-created-at))
      (is (= post-insert-created-at post-insert-modified-at))
      (is (not= post-update-created-at post-update-modified-at))
      (is (utils/earlier-than? post-insert-modified-at post-update-modified-at)))))

(deftest delete-morphology-test
  (testing "Delete a row in the morphology table"
    (let [initial-count (:count (morphologies/count-morphologies *txn*))
          ;; Create the morphology
          morphology-id (utils/uuid)
          morphology-map {:id morphology-id
                          :corpus h/corpus-string
                          :script-format h/script-format}
          _ (morphologies/insert-morphology *txn* morphology-map)
          post-insert-count (:count (morphologies/count-morphologies *txn*))
          ;; Delete the morphology
          _ (morphologies/delete-morphology *txn* {:id morphology-id})
          post-delete-count (:count (morphologies/count-morphologies *txn*))
          post-delete-morphology-row
          (morphologies/get-morphology *txn* {:id morphology-id})]
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 0 post-delete-count))
      (is (nil? post-delete-morphology-row)))))
