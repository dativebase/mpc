(ns mpc.db-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [mpc.db :as db]
            [mpc.entities :as e]
            [mpc.test-core :refer [with-rollback]]))

;; =============================================================================
;; Fixtures
;; =============================================================================

; Run each test in an isolated db transaction and rollback
; afterwards
(use-fixtures :each with-rollback)

(def corpus '("in-perfect" "un-break-able"))
(def script-format "lexc")
(def phonology-script "define phonology ...")
(def morphology-script "define morphology ...")
(def morphophonology-script "define morphophonology ...")
(def compiled-script "/path/to/compiled/script")
(def arpa "some ARPA text file")
(def language-model "/path/to/estimated/lm")

(def phonology (e/make-phonology
                 {:script phonology-script
                  :compiled-script compiled-script}))

(def morphology (e/make-morphology
                  {:corpus corpus
                   :script-format script-format
                   :script morphology-script
                   :compiled-script compiled-script}))

(def morphophonology (e/make-morphophonology
                       {:morphology morphology
                        :phonology phonology
                        :script morphophonology-script
                        :compiled-script compiled-script}))

(def candidate-ranker (e/make-candidate-ranker
                       {:corpus corpus
                        :arpa arpa
                        :language-model language-model}))

(def morphological-parser (e/make-morphological-parser
                            {:morphophonology morphophonology
                             :candidate-ranker candidate-ranker}))

;; =============================================================================
;; Tests
;; =============================================================================

(deftest create-get-phonologies
  (testing "Create and read phonologies"
    (let [count-orig (db/count-phonologies)]
      (is (= 0 count-orig))
      (db/create-phonology phonology)
      (is (= (inc count-orig) (db/count-phonologies)))
      (let [created-phonology (first (db/get-phonologies))]
        (is (= (db/get-phonology (:id created-phonology)) created-phonology))
        (is (= phonology created-phonology))
        (is (= phonology-script (:script created-phonology)))
        (is (= compiled-script (:compiled-script created-phonology)))))))

(deftest create-get-morphologies
  (testing "Create and read morphologies"
    (let [count-orig (db/count-morphologies)]
      (is (= 0 count-orig))
      (db/create-morphology morphology)
      (is (= (inc count-orig) (db/count-morphologies)))
      (let [created-morphology (first (db/get-morphologies))]
        (is (= (db/get-morphology (:id created-morphology))
               created-morphology))
        (is (= morphology created-morphology))
        (is (= corpus (:corpus created-morphology)))
        (is (= morphology-script (:script created-morphology)))
        (is (= script-format (:script-format created-morphology)))
        (is (= compiled-script (:compiled-script created-morphology)))))))

(deftest create-get-morphophonologies
  (testing "Create and read morphophonologies"
    (let [count-orig (db/count-morphophonologies)]
      (is (= 0 count-orig))
      (db/create-phonology phonology)
      (db/create-morphology morphology)
      (db/create-morphophonology morphophonology)
      (is (= (inc count-orig) (db/count-morphophonologies)))
      (let [created-morphophonology (first (db/get-morphophonologies))]
        (is (= (db/get-morphophonology (:id created-morphophonology))
               created-morphophonology))
        (is (= morphophonology created-morphophonology))
        (is (= morphophonology-script (:script created-morphophonology)))
        (is (= phonology (:phonology created-morphophonology)))
        (is (= morphology (:morphology created-morphophonology)))
        (is (= compiled-script (:compiled-script created-morphophonology)))))))

(deftest create-get-candidate-rankers
  (testing "Create and read candidate rankers"
    (let [count-orig (db/count-candidate-rankers)]
      (is (= 0 count-orig))
      (db/create-candidate-ranker candidate-ranker)
      (is (= (inc count-orig) (db/count-candidate-rankers)))
      (let [created-candidate-ranker (first (db/get-candidate-rankers))]
        (is (= (db/get-candidate-ranker (:id created-candidate-ranker))
             created-candidate-ranker))
        (is (= corpus (:corpus created-candidate-ranker)))
        (is (= arpa (:arpa created-candidate-ranker)))
        (is (= language-model (:language-model created-candidate-ranker)))))))

(deftest create-get-morphological-parsers
  (testing "Create and read candidate rankers"
    (let [count-orig (db/count-morphological-parsers)]
      (is (= 0 count-orig))
      (db/create-phonology phonology)
      (db/create-morphology morphology)
      (db/create-morphophonology morphophonology)
      (db/create-candidate-ranker candidate-ranker)
      (db/create-morphological-parser morphological-parser)
      (is (= (inc count-orig) (db/count-morphological-parsers)))
      (let [created-morphological-parser (first (db/get-morphological-parsers))]
        (is (= (db/get-morphological-parser (:id created-morphological-parser))
             created-morphological-parser))
        (is (= morphophonology (:morphophonology created-morphological-parser)))
        (is (= candidate-ranker (:candidate-ranker created-morphological-parser)))))))
