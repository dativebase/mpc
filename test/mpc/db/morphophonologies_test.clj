(ns mpc.db.morphophonologies-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [cheshire.core :refer [parse-string generate-string]]
            [mpc.db.morphologies :as morphologies]
            [mpc.db.morphophonologies :as morphophonologies]
            [mpc.db.phonologies :as phonologies]
            [mpc.test-core :refer [*txn* with-rollback create-destroy-tables]]
            [mpc.utils :as utils]
            [mpc.db.test-db-helpers :as h]))

;; Create all tables before tests and destroy them afterwards.
(use-fixtures :once create-destroy-tables)

;; Perform all db mutations in a transaction that is rolled back after each
;; test.
(use-fixtures :each with-rollback)

(deftest insert-get-morphophonology-test
  (testing "Insert into the morphophonology table and get by id"
    (let [morphophonology-id (utils/uuid)
          phonology-id (h/create-test-phonology)
          morphology-id (h/create-test-morphology)
          morphophonology-map {:id morphophonology-id
                               :script ""
                               :morphology-id morphology-id
                               :phonology-id phonology-id}
          rows-affected-count
          (morphophonologies/insert-morphophonology *txn* morphophonology-map)
          morphophonology-row
          (morphophonologies/get-morphophonology *txn* {:id morphophonology-id})
          created-at (:created_at morphophonology-row)
          modified-at (:modified_at morphophonology-row)]
      (is (= 1 rows-affected-count))
      (is (= (:created_at morphophonology-row) (:modified_at morphophonology-row)))
      (is (instance? java.util.Date (:created_at morphophonology-row)))
      (is (= "not attempted" (:compile_status morphophonology-row)))
      (is (= "not attempted" (:generate_status morphophonology-row)))
      (is (= morphophonology-id (:id morphophonology-row)))
      (is (= phonology-id (:phonology_id morphophonology-row)))
      (is (= morphology-id (:morphology_id morphophonology-row))))))

(deftest insert-get-all-morphophonology-test
  (testing "Insert multiple morphophonologies and get them all"
    (let [;; Create morphophonology #1
          morphophonology-1-id (utils/uuid)
          phonology-1-id (h/create-test-phonology)
          morphology-1-id (h/create-test-morphology)
          morphophonology-map {:id morphophonology-1-id
                               :script "abc"
                               :morphology-id morphology-1-id
                               :phonology-id phonology-1-id}
          rows-affected-count-1
          (morphophonologies/insert-morphophonology *txn* morphophonology-map)
          ;; Create morphophonology #2
          morphophonology-2-id (utils/uuid)
          phonology-2-id (h/create-test-phonology)
          morphology-2-id (h/create-test-morphology)
          morphophonology-map {:id morphophonology-2-id
                               :script "xyz"
                               :morphology-id morphology-2-id
                               :phonology-id phonology-2-id}
          rows-affected-count-2
          (morphophonologies/insert-morphophonology *txn* morphophonology-map)
          ;; Get all the morphophonologies
          morphophonology-row-set (morphophonologies/get-morphophonologies *txn*)]
      (is (= 1 rows-affected-count-1))
      (is (= 1 rows-affected-count-2))
      (is (= 2 (count morphophonology-row-set)))
      (let [morphophonology-1-row
            (first (filter #(= morphophonology-1-id (:id %)) morphophonology-row-set))]
        (is (= "abc" (:script morphophonology-1-row)))
        (is (= phonology-1-id (:phonology_id morphophonology-1-row)))
        (is (= morphology-1-id (:morphology_id morphophonology-1-row))))
      (let [morphophonology-2-row
            (first (filter #(= morphophonology-2-id (:id %)) morphophonology-row-set))]
        (is (= "xyz" (:script morphophonology-2-row)))
        (is (= phonology-2-id (:phonology_id morphophonology-2-row)))
        (is (= morphology-2-id (:morphology_id morphophonology-2-row)))))))

(deftest update-morphophonology-test
  (testing "Update a row in the morphophonology table"
    (let [initial-count (:count (morphophonologies/count-morphophonologies *txn*))
          ;; Create the morphophonology
          morphophonology-id (utils/uuid)
          initial-phonology-id (h/create-test-phonology)
          morphology-id (h/create-test-morphology)
          morphophonology-map {:id morphophonology-id
                               :script ""
                               :morphology-id morphology-id
                               :phonology-id initial-phonology-id}
          rows-affected-count
          (morphophonologies/insert-morphophonology *txn* morphophonology-map)
          post-insert-morphophonology-row
          (morphophonologies/get-morphophonology *txn* {:id morphophonology-id})
          post-insert-created-at (:created_at post-insert-morphophonology-row)
          post-insert-modified-at (:modified_at post-insert-morphophonology-row)
          post-insert-count (:count (morphophonologies/count-morphophonologies *txn*))
          ;; Update the morphophonology
          new-phonology-id (h/create-test-phonology)
          morphophonology-map {:id morphophonology-id
                               :script ""
                               :morphology-id morphology-id
                               :phonology-id new-phonology-id}
          rows-affected-count
          (morphophonologies/update-morphophonology *txn* morphophonology-map)
          post-update-morphophonology-row
          (morphophonologies/get-morphophonology *txn* {:id morphophonology-id})
          post-update-created-at (:created_at post-update-morphophonology-row)
          post-update-modified-at (:modified_at post-update-morphophonology-row)
          post-update-count (:count (morphophonologies/count-morphophonologies *txn*))]
      (is (= initial-phonology-id (:phonology_id post-insert-morphophonology-row)))
      (is (= new-phonology-id (:phonology_id post-update-morphophonology-row)))
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 1 post-update-count))
      (is (= post-insert-created-at post-update-created-at))
      (is (= post-insert-created-at post-insert-modified-at))
      (is (not= post-update-created-at post-update-modified-at))
      (is (utils/earlier-than? post-insert-modified-at post-update-modified-at)))))

(deftest delete-morphophonology-test
  (testing "Delete a row in the morphophonology table"
    (let [initial-count (:count (morphophonologies/count-morphophonologies *txn*))
          ;; Create the morphophonology
          morphophonology-id (utils/uuid)
          initial-phonology-id (h/create-test-phonology)
          morphology-id (h/create-test-morphology)
          morphophonology-map {:id morphophonology-id
                               :script ""
                               :morphology-id morphology-id
                               :phonology-id initial-phonology-id}
          rows-affected-count
          (morphophonologies/insert-morphophonology *txn* morphophonology-map)
          post-insert-morphophonology-row
          (morphophonologies/get-morphophonology *txn* {:id morphophonology-id})
          post-insert-count (:count (morphophonologies/count-morphophonologies *txn*))
          ;; Delete the morphophonology
          _ (morphophonologies/delete-morphophonology *txn* {:id morphophonology-id})
          post-delete-count (:count (morphophonologies/count-morphophonologies *txn*))
          post-delete-morphophonology-row
          (morphophonologies/get-morphophonology *txn* {:id morphophonology-id})]
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 0 post-delete-count))
      (is (nil? post-delete-morphophonology-row)))))
