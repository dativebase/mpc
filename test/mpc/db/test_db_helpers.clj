(ns mpc.db.test-db-helpers
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [cheshire.core :refer [parse-string generate-string]]
            [mpc.db.candidate-rankers :as candidate-rankers]
            [mpc.db.morphologies :as morphologies]
            [mpc.db.morphophonologies :as morphophonologies]
            [mpc.db.phonologies :as phonologies]
            [mpc.test-core :refer [*txn* with-rollback create-destroy-tables]]
            [mpc.utils :as utils]))

;; Phonology test constants
(def script "define phonology a -> b || c _ d ;")
(def script-1 script)
(def script-2 "define phonology x -> y || z _ w ;")

;; Morphology test constants
(def script-format "lexc")
(def corpus '("un-break-able" "in-perfect"))
(def corpus-string (generate-string corpus))
(def corpus-1 corpus)
(def corpus-1-string corpus-string)
(def corpus-2 '("con-sider-able" "un-re-mark-able"))
(def corpus-2-string (generate-string corpus-2))

(defn create-test-phonology []
  (let [phonology-id (utils/uuid)
        phonology-map {:id phonology-id :script script}]
    (phonologies/insert-phonology *txn* phonology-map)
    phonology-id))

(defn create-test-morphology []
  (let [morphology-id (utils/uuid)
        morphology-map {:id morphology-id
                        :script-format script-format
                        :corpus corpus-string}]
    (morphologies/insert-morphology *txn* morphology-map)
    morphology-id))

(defn create-test-morphophonology []
  (let [phonology-id (create-test-phonology)
        morphology-id (create-test-morphology)
        morphophonology-id (utils/uuid)
        morphophonology-map {:id morphophonology-id
                             :script ""
                             :morphology-id morphology-id
                             :phonology-id phonology-id}]
    (morphophonologies/insert-morphophonology *txn* morphophonology-map)
    morphophonology-id))

(defn create-test-candidate-ranker []
  (let [candidate-ranker-id (utils/uuid)
        candidate-ranker-map {:id candidate-ranker-id :corpus corpus-string}]
    (candidate-rankers/insert-candidate-ranker *txn* candidate-ranker-map)
    candidate-ranker-id))
