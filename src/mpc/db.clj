(ns mpc.db
  (:require [cheshire.core :refer [parse-string generate-string]]
            [environ.core :refer [env]]
            [korma.db :refer [defdb postgres]]
            [korma.core :as korma]
            [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mpc.entities]
            [mpc.utils :as utils]))

(def default-connection-uri
  "jdbc:postgresql://localhost:5432/mpc?user=mpc&password=mpc")

(def connection-uri
  (or (env :mpc-db-uri) default-connection-uri))

(defdb db (postgres (utils/parse-connection-uri connection-uri)))

(declare get-phonology)
(declare get-morphology)
(declare get-morphophonology)
(declare get-candidate-ranker)

;; =============================================================================
;; Phonology
;; =============================================================================

(korma/defentity phonology

  (korma/transform (fn [entity]
             (-> entity
                 utils/db-cols->record-attrs
                 mpc.entities/make-phonology)))
  (korma/prepare (fn [entity]
             (-> entity
                 utils/record-attrs->db-cols))))

;; =============================================================================
;; Morphology
;; =============================================================================

(korma/defentity morphology

  (korma/transform (fn [entity]
                     (-> entity
                         utils/db-cols->record-attrs
                         (assoc :corpus (parse-string (:corpus entity)))
                         mpc.entities/make-morphology)))

  (korma/prepare (fn [entity]
                   (-> entity
                       (assoc :corpus (generate-string (:corpus entity)))
                       utils/record-attrs->db-cols))))

;; =============================================================================
;; Morphophonology
;; =============================================================================

(korma/defentity morphophonology

  (korma/belongs-to morphology)
  (korma/belongs-to phonology)

  (korma/prepare
    (fn [morphophonology-rec]
      (let [morphophonology-map
            (utils/record-attrs->db-cols morphophonology-rec)]
        {:id (:id morphophonology-map)
         :script (:script morphophonology-map)
         :compiled_script (:compiled_script morphophonology-map)
         :phonology_id (get-in morphophonology-map [:phonology :id])
         :morphology_id (get-in morphophonology-map [:morphology :id])})))

  (korma/transform
    (fn [morphophonology-map]
      (let [morphophonology-map (utils/db-cols->record-attrs
                                  morphophonology-map)]
        (mpc.entities/make-morphophonology
          {:id (:id morphophonology-map)
           :script (:script morphophonology-map)
           :compiled-script (:compiled-script morphophonology-map)
           :morphology (get-morphology (:morphology-id morphophonology-map))
           :phonology (get-phonology (:phonology-id morphophonology-map))})))))

;; =============================================================================
;; Candidate Ranker
;; =============================================================================

(korma/defentity candidate-ranker

  (korma/table :candidate_ranker)

  (korma/transform (fn [entity]
                     (-> entity
                         utils/db-cols->record-attrs
                         (assoc :corpus (parse-string (:corpus entity)))
                         mpc.entities/make-candidate-ranker)))

  (korma/prepare (fn [entity]
                   (-> entity
                       (assoc :corpus (generate-string (:corpus entity)))
                       utils/record-attrs->db-cols))))

;; =============================================================================
;; Morphological Parser
;; =============================================================================

(korma/defentity morphological-parser

  (korma/table :morphological_parser)
  (korma/belongs-to morphophonology)
  (korma/belongs-to candidate-ranker)

  (korma/prepare
    (fn [morphological-parser-rec]
      (let [morphological-parser-map
            (utils/record-attrs->db-cols morphological-parser-rec)]
        {:id (:id morphological-parser-map)
         :morphophonology_id (get-in morphological-parser-map
                                     [:morphophonology :id])
         :candidate_ranker_id (get-in morphological-parser-map
                                      [:candidate_ranker :id])})))

  (korma/transform
    (fn [morphological-parser-map]
      (let [morphological-parser-map (utils/db-cols->record-attrs
                                       morphological-parser-map)]
        (mpc.entities/make-morphophonology
          {:id (:id morphological-parser-map)
           :morphophonology (get-morphophonology
                              (:morphophonology-id morphological-parser-map))
           :candidate-ranker (get-candidate-ranker
                               (:candidate-ranker-id
                                 morphological-parser-map))})))))

;; =============================================================================
;; Count functions
;; =============================================================================

(defn count-entities [entity-kw]
  (let [table-name (subs (str entity-kw) 1)]
    (:count (first (korma/exec-raw
                     [(format "SELECT COUNT(id) FROM %s" table-name)]
                     :results)))))

(def count-phonologies (partial count-entities :phonology))
(def count-morphologies (partial count-entities :morphology))
(def count-morphophonologies (partial count-entities :morphophonology))
(def count-candidate-rankers (partial count-entities :candidate_ranker))
(def count-morphological-parsers (partial count-entities :morphological_parser))

;; =============================================================================
;; Get one functions
;; =============================================================================

(defn get-entity [entity entity-id]
  (first (korma/select entity
                       (korma/where {:id entity-id}))))

(def get-phonology (partial get-entity phonology))
(def get-morphology (partial get-entity morphology))
(def get-morphophonology (partial get-entity morphophonology))
(def get-candidate-ranker (partial get-entity candidate-ranker))
(def get-morphological-parser (partial get-entity morphological-parser))

;; =============================================================================
;; Get (fetch) all functions
;; =============================================================================

(defn get-entities [entity] (korma/select entity))

(def get-phonologies (partial get-entities phonology))
(def get-morphologies (partial get-entities morphology))
(def get-morphophonologies (partial get-entities morphophonology))
(def get-candidate-rankers (partial get-entities candidate-ranker))
(def get-morphological-parsers (partial get-entities morphological-parser))

;; =============================================================================
;; Create functions
;; =============================================================================

(defn create-entity [entity entity-record]
  (korma/insert entity
    (korma/values entity-record)))

(def create-phonology (partial create-entity phonology))
(def create-morphology (partial create-entity morphology))
(def create-morphophonology (partial create-entity morphophonology))
(def create-candidate-ranker (partial create-entity candidate-ranker))
(def create-morphological-parser (partial create-entity morphological-parser))
