(ns mpc.db.morphological-parsers-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [cheshire.core :refer [parse-string generate-string]]
            [mpc.db.candidate-rankers :as candidate-rankers]
            [mpc.db.morphologies :as morphologies]
            [mpc.db.morphophonologies :as morphophonologies]
            [mpc.db.morphological-parsers :as morphological-parsers]
            [mpc.db.phonologies :as phonologies]
            [mpc.test-core :refer [*txn* with-rollback create-destroy-tables]]
            [mpc.utils :as utils]
            [mpc.db.test-db-helpers :as h]))

;; Create all tables before tests and destroy them afterwards.
(use-fixtures :once create-destroy-tables)

;; Perform all db mutations in a transaction that is rolled back after each
;; test.
(use-fixtures :each with-rollback)

(deftest insert-get-morphological-parser-test
  (testing "Insert into the morphological parser table and get by id"
    (let [morphological-parser-id (utils/uuid)
          morphophonology-id (h/create-test-morphophonology)
          candidate-ranker-id (h/create-test-candidate-ranker)
          morphological-parser-map {:id morphological-parser-id
                                    :morphophonology-id morphophonology-id
                                    :candidate-ranker-id candidate-ranker-id}
          rows-affected-count
          (morphological-parsers/insert-morphological-parser *txn* morphological-parser-map)
          morphological-parser-row
          (morphological-parsers/get-morphological-parser *txn* {:id morphological-parser-id})
          created-at (:created_at morphological-parser-row)
          modified-at (:modified_at morphological-parser-row)]
      (is (= 1 rows-affected-count))
      (is (= (:created_at morphological-parser-row) (:modified_at morphological-parser-row)))
      (is (instance? java.util.Date (:created_at morphological-parser-row)))
      (is (= morphological-parser-id (:id morphological-parser-row)))
      (is (= morphophonology-id (:morphophonology_id morphological-parser-row)))
      (is (= candidate-ranker-id (:candidate_ranker_id morphological-parser-row))))))

(deftest insert-get-all-morphological-parser-test
  (testing "Insert multiple morphological parsers and get them all"
    (let [;; Create morphological parser #1
          morphological-parser-1-id (utils/uuid)
          morphophonology-1-id (h/create-test-morphophonology)
          candidate-ranker-1-id (h/create-test-candidate-ranker)
          morphological-parser-map {:id morphological-parser-1-id
                                    :morphophonology-id morphophonology-1-id
                                    :candidate-ranker-id candidate-ranker-1-id}
          rows-affected-count-1
          (morphological-parsers/insert-morphological-parser
            *txn* morphological-parser-map)
          ;; Create morphological parser #2
          morphological-parser-2-id (utils/uuid)
          morphophonology-2-id (h/create-test-morphophonology)
          candidate-ranker-2-id (h/create-test-candidate-ranker)
          morphological-parser-map {:id morphological-parser-2-id
                                    :morphophonology-id morphophonology-2-id
                                    :candidate-ranker-id candidate-ranker-2-id}
          rows-affected-count-2
          (morphological-parsers/insert-morphological-parser
            *txn* morphological-parser-map)
          ;; Get all the morphological parsers
          morphological-parser-row-set
          (morphological-parsers/get-morphological-parsers *txn*)]
      (is (= 1 rows-affected-count-1))
      (is (= 1 rows-affected-count-2))
      (is (= 2 (count morphological-parser-row-set)))
      (let [morphological-parser-1-row
            (first (filter
                     #(= morphological-parser-1-id (:id %))
                     morphological-parser-row-set))]
        (is (= morphophonology-1-id (:morphophonology_id morphological-parser-1-row)))
        (is (= candidate-ranker-1-id (:candidate_ranker_id morphological-parser-1-row))))
      (let [morphological-parser-2-row
            (first (filter
                     #(= morphological-parser-2-id (:id %))
                     morphological-parser-row-set))]
        (is (= morphophonology-2-id (:morphophonology_id morphological-parser-2-row)))
        (is (= candidate-ranker-2-id (:candidate_ranker_id morphological-parser-2-row)))))))

(deftest update-morphological-parser-test
  (testing "Update a row in the morphological-parser table"
    (let [initial-count
          (:count (morphological-parsers/count-morphological-parsers *txn*))
          ;; Create the morphological parser
          morphological-parser-id (utils/uuid)
          initial-morphophonology-id (h/create-test-morphophonology)
          candidate-ranker-id (h/create-test-candidate-ranker)
          morphological-parser-map {:id morphological-parser-id
                                    :morphophonology-id initial-morphophonology-id
                                    :candidate-ranker-id candidate-ranker-id}
          rows-affected-count
          (morphological-parsers/insert-morphological-parser
            *txn* morphological-parser-map)
          post-insert-morphological-parser-row
          (morphological-parsers/get-morphological-parser
            *txn* {:id morphological-parser-id})
          post-insert-created-at (:created_at post-insert-morphological-parser-row)
          post-insert-modified-at (:modified_at post-insert-morphological-parser-row)
          post-insert-count
          (:count (morphological-parsers/count-morphological-parsers *txn*))
          ;; Update the morphological parser
          new-morphophonology-id (h/create-test-morphophonology)
          morphological-parser-map {:id morphological-parser-id
                                    :morphophonology-id new-morphophonology-id
                                    :candidate-ranker-id candidate-ranker-id}
          rows-affected-count
          (morphological-parsers/update-morphological-parser
            *txn* morphological-parser-map)
          post-update-morphological-parser-row
          (morphological-parsers/get-morphological-parser
            *txn* {:id morphological-parser-id})
          post-update-created-at
          (:created_at post-update-morphological-parser-row)
          post-update-modified-at
          (:modified_at post-update-morphological-parser-row)
          post-update-count
          (:count (morphological-parsers/count-morphological-parsers *txn*))]
      (is (= initial-morphophonology-id
             (:morphophonology_id post-insert-morphological-parser-row)))
      (is (= new-morphophonology-id
             (:morphophonology_id post-update-morphological-parser-row)))
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 1 post-update-count))
      (is (= post-insert-created-at post-update-created-at))
      (is (= post-insert-created-at post-insert-modified-at))
      (is (not= post-update-created-at post-update-modified-at))
      (is (utils/earlier-than? post-insert-modified-at post-update-modified-at)))))

(deftest delete-morphological-parser-test
  (testing "Delete a row in the morphological parser table"
    (let [initial-count
          (:count (morphological-parsers/count-morphological-parsers *txn*))
          ;; Create the morphological parser
          morphological-parser-id (utils/uuid)
          initial-morphophonology-id (h/create-test-morphophonology)
          candidate-ranker-id (h/create-test-candidate-ranker)
          morphological-parser-map {:id morphological-parser-id
                                    :morphophonology-id initial-morphophonology-id
                                    :candidate-ranker-id candidate-ranker-id}
          rows-affected-count
          (morphological-parsers/insert-morphological-parser
            *txn* morphological-parser-map)
          post-insert-morphological-parser-row
          (morphological-parsers/get-morphological-parser
            *txn* {:id morphological-parser-id})
          post-insert-count
          (:count (morphological-parsers/count-morphological-parsers *txn*))
          ;; Delete the morphological parser
          _ (morphological-parsers/delete-morphological-parser
              *txn* {:id morphological-parser-id})
          post-delete-count
          (:count (morphological-parsers/count-morphological-parsers *txn*))
          post-delete-morphological-parser-row
          (morphological-parsers/get-morphological-parser
            *txn* {:id morphological-parser-id})]
      (is (= 0 initial-count))
      (is (= 1 post-insert-count))
      (is (= 0 post-delete-count))
      (is (nil? post-delete-morphological-parser-row)))))
