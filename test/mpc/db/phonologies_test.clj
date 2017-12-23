(ns mpc.db.phonologies-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [mpc.db.phonologies :as phonologies]
            [mpc.test-core :refer [*txn* with-rollback create-destroy-tables]]
            [mpc.utils :as utils]
            [mpc.db.test-db-helpers :as h]))

;; Create all tables before tests and destroy them afterwards.
(use-fixtures :once create-destroy-tables)

;; Perform all db mutations in a transaction that is rolled back after each
;; test.
(use-fixtures :each with-rollback)

(deftest insert-get-phonology-test
  (testing "Insert into the phonology table and get by id"
    (let [phonology-id (utils/uuid)
          phonology-map {:id phonology-id :script h/script}
          rows-affected-count (phonologies/insert-phonology *txn* phonology-map)
          phonology-row (phonologies/get-phonology *txn* {:id phonology-id})
          created-at (:created_at phonology-row)
          modified-at (:modified_at phonology-row)]
      (is (= 1 rows-affected-count))
      (is (= (:created_at phonology-row) (:modified_at phonology-row)))
      (is (instance? java.util.Date (:created_at phonology-row)))
      (is (= "not attempted" (:compile_status phonology-row)))
      (is (= phonology-id (:id phonology-row)))
      (is (= h/script (:script phonology-row))))))

(deftest insert-get-all-phonology-test
  (testing "Insert multiple phonologies and get them all"
    (let [phonology-1-id (utils/uuid)
          phonology-1-map {:id phonology-1-id :script h/script-1}
          rows-affected-count-1 (phonologies/insert-phonology *txn* phonology-1-map)
          phonology-2-id (utils/uuid)
          phonology-2-map {:id phonology-2-id :script h/script-2}
          rows-affected-count-2 (phonologies/insert-phonology *txn* phonology-2-map)
          phonology-row-set (phonologies/get-phonologies *txn*)]
      (is (= 1 rows-affected-count-1))
      (is (= 1 rows-affected-count-2))
      (is (= 2 (count phonology-row-set)))
      (let [phonology-1-row
            (first (filter #(= phonology-1-id (:id %)) phonology-row-set))]
        (is (= h/script-1 (:script phonology-1-row))))
      (let [phonology-2-row
            (first (filter #(= phonology-2-id (:id %)) phonology-row-set))]
        (is (= h/script-2 (:script phonology-2-row)))))))

(deftest update-phonology-test
  (testing "Update a row in the phonology table"
    (let [initial-count (:count (phonologies/count-phonologies *txn*))
          ;; Create the phonology
          phonology-id (utils/uuid)
          initial-script "define phonology a -> b || c _ d ;"
          phonology-map {:id phonology-id :script initial-script}
          _ (phonologies/insert-phonology *txn* phonology-map)
          post-insert-phonology-row (phonologies/get-phonology *txn* {:id phonology-id})
          post-insert-created-at (:created_at post-insert-phonology-row)
          post-insert-modified-at (:modified_at post-insert-phonology-row)
          post-insert-count (:count (phonologies/count-phonologies *txn*))
          ;; Update the phonology
          new-script "define phonology jim -> bob || tammy _ fay ;"
          _ (phonologies/update-phonology
              *txn* {:id phonology-id :script new-script})
          post-update-count (:count (phonologies/count-phonologies *txn*))
          post-update-phonology-row
          (phonologies/get-phonology *txn* {:id phonology-id})
          post-update-created-at (:created_at post-update-phonology-row)
          post-update-modified-at (:modified_at post-update-phonology-row)]
      (is (= initial-script (:script post-insert-phonology-row)))
      (is (= new-script (:script post-update-phonology-row)))
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 1 post-update-count))
      (is (= post-insert-created-at post-update-created-at))
      (is (= post-insert-created-at post-insert-modified-at))
      (is (not= post-update-created-at post-update-modified-at))
      (is (utils/earlier-than? post-insert-modified-at post-update-modified-at)))))

(deftest delete-phonology-test
  (testing "Delete a row in the phonology table"
    (let [initial-count (:count (phonologies/count-phonologies *txn*))
          ;; Create the phonology
          phonology-id (utils/uuid)
          phonology-map {:id phonology-id :script h/script}
          _ (phonologies/insert-phonology *txn* phonology-map)
          post-insert-count (:count (phonologies/count-phonologies *txn*))
          ;; Delete the phonology
          _ (phonologies/delete-phonology *txn* {:id phonology-id})
          post-delete-count (:count (phonologies/count-phonologies *txn*))
          post-delete-phonology-row
          (phonologies/get-phonology *txn* {:id phonology-id})]
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 0 post-delete-count))
      (is (nil? post-delete-phonology-row)))))
