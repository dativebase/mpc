(ns mpc.db.candidate-rankers-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [mpc.db.candidate-rankers :as candidate-rankers]
            [mpc.test-core :refer [*txn* with-rollback create-destroy-tables]]
            [mpc.utils :as utils]
            [mpc.db.test-db-helpers :as h]))

;; Create all tables before tests and destroy them afterwards.
(use-fixtures :once create-destroy-tables)

;; Perform all db mutations in a transaction that is rolled back after each
;; test.
(use-fixtures :each with-rollback)

(deftest insert-get-candidate-ranker-test
  (testing "Insert into the candidate ranker table and get by id"
    (let [candidate-ranker-id (utils/uuid)
          candidate-ranker-map {:id candidate-ranker-id :corpus h/corpus-string}
          rows-affected-count
          (candidate-rankers/insert-candidate-ranker *txn* candidate-ranker-map)
          candidate-ranker-row
          (candidate-rankers/get-candidate-ranker *txn*
                                                  {:id candidate-ranker-id})
          created-at (:created_at candidate-ranker-row)
          modified-at (:modified_at candidate-ranker-row)]
      (is (= 1 rows-affected-count))
      (is (= (:created_at candidate-ranker-row)
             (:modified_at candidate-ranker-row)))
      (is (instance? java.util.Date (:created_at candidate-ranker-row)))
      (is (= "not attempted" (:estimate_status candidate-ranker-row)))
      (is (= candidate-ranker-id (:id candidate-ranker-row)))
      (is (= h/corpus (:corpus candidate-ranker-row))))))


(deftest insert-get-all-candidate-ranker-test
  (testing "Insert multiple candidate rankers and get them all"
    (let [candidate-ranker-1-id (utils/uuid)
          candidate-ranker-1-map
          {:id candidate-ranker-1-id :corpus h/corpus-1-string}
          rows-affected-count-1
          (candidate-rankers/insert-candidate-ranker *txn* candidate-ranker-1-map)
          candidate-ranker-2-id (utils/uuid)
          candidate-ranker-2-map
          {:id candidate-ranker-2-id :corpus h/corpus-2-string}
          rows-affected-count-2
          (candidate-rankers/insert-candidate-ranker
            *txn* candidate-ranker-2-map)
          candidate-ranker-row-set (candidate-rankers/get-candidate-rankers
                                     *txn*)]
      (is (= 1 rows-affected-count-1))
      (is (= 1 rows-affected-count-2))
      (is (= 2 (count candidate-ranker-row-set)))
      (let [candidate-ranker-1-row
            (first (filter #(= candidate-ranker-1-id (:id %)) candidate-ranker-row-set))]
        (is (= h/corpus-1 (:corpus candidate-ranker-1-row))))
      (let [candidate-ranker-2-row
            (first (filter #(= candidate-ranker-2-id (:id %)) candidate-ranker-row-set))]
        (is (= h/corpus-2 (:corpus candidate-ranker-2-row)))))))

(deftest update-candidate-ranker-test
  (testing "Update a row in the candidate ranker table"
    (let [initial-count (:count (candidate-rankers/count-candidate-rankers *txn*))
          ;; Create the candidate ranker
          candidate-ranker-id (utils/uuid)
          initial-corpus h/corpus-1
          initial-corpus-string h/corpus-1-string
          candidate-ranker-map {:id candidate-ranker-id :corpus initial-corpus-string}
          _ (candidate-rankers/insert-candidate-ranker *txn* candidate-ranker-map)
          post-insert-candidate-ranker-row (candidate-rankers/get-candidate-ranker *txn* {:id candidate-ranker-id})
          post-insert-created-at (:created_at post-insert-candidate-ranker-row)
          post-insert-modified-at (:modified_at post-insert-candidate-ranker-row)
          post-insert-count (:count (candidate-rankers/count-candidate-rankers *txn*))
          ;; Update the candidate ranker
          new-corpus h/corpus-2
          new-corpus-string h/corpus-2-string
          _ (candidate-rankers/update-candidate-ranker
              *txn* {:id candidate-ranker-id
                     :corpus new-corpus-string
                     :estimate-status "not attempted"})
          post-update-count (:count (candidate-rankers/count-candidate-rankers *txn*))
          post-update-candidate-ranker-row
          (candidate-rankers/get-candidate-ranker *txn* {:id candidate-ranker-id})
          post-update-created-at (:created_at post-update-candidate-ranker-row)
          post-update-modified-at (:modified_at post-update-candidate-ranker-row)]
      (is (= initial-corpus (:corpus post-insert-candidate-ranker-row)))
      (is (= new-corpus (:corpus post-update-candidate-ranker-row)))
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 1 post-update-count))
      (is (= post-insert-created-at post-update-created-at))
      (is (= post-insert-created-at post-insert-modified-at))
      (is (not= post-update-created-at post-update-modified-at))
      (is (utils/earlier-than? post-insert-modified-at post-update-modified-at))
      ;; Update the candidate ranker in a prohibited way and expect an exception
      (is (thrown? java.sql.BatchUpdateException
                  (candidate-rankers/update-candidate-ranker
                    *txn* {:id candidate-ranker-id
                           :corpus new-corpus-string
                           :estimate-status "monkey pants"}))))))

(deftest delete-candidate-ranker-test
  (testing "Delete a row in the candidate ranker table"
    (let [initial-count (:count (candidate-rankers/count-candidate-rankers *txn*))
          ;; Create the candidate ranker
          candidate-ranker-id (utils/uuid)
          candidate-ranker-map {:id candidate-ranker-id :corpus h/corpus-string}
          _ (candidate-rankers/insert-candidate-ranker *txn* candidate-ranker-map)
          post-insert-count (:count (candidate-rankers/count-candidate-rankers *txn*))
          ;; Delete the candidate ranker
          _ (candidate-rankers/delete-candidate-ranker *txn* {:id candidate-ranker-id})
          post-delete-count (:count (candidate-rankers/count-candidate-rankers *txn*))
          post-delete-candidate-ranker-row
          (candidate-rankers/get-candidate-ranker *txn* {:id candidate-ranker-id})]
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 0 post-delete-count))
      (is (nil? post-delete-candidate-ranker-row)))))
