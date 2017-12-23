(ns mpc.entities
  (:require [mpc.utils :as utils]))

;; A `Phonology` is a compiled foma FST as well as the (user-supplied) foma
;; script used to generate it.
(defrecord Phonology [id
                      ;; The `script` is a user-supplied string representing a
                      ;; regex foma script.
                      script
                      ;; The `compiled-script` is `script` compiled to a binary
                      ;; using foma.
                      compiled-script])

(defn new-phonology []
  (->Phonology (utils/uuid) "" ""))

;; A `Morpphology` is a compiled foma FST as well as the word corpus, foma
;; script, and other parameters needed to generate it.
(defrecord Morphology [id
                       ;; The `corpus` is a vector of linguistic forms, which
                       ;; should contain morphologically analyzed words that we
                       ;; can extract syntactic category string patterns from,
                       ;; in order to build the `script`.
                       corpus
                       ;; The `script-format` is the format to use (i.e., "lexc"
                       ;; or "regex") when generating `script` from
                       ;; `corpus`; it should default to "lexc".
                       script-format
                       ;; The `script` is a foma script in the `script-format`
                       ;; language; it is generated from `corpus`.
                       script
                       ;; The `compiled-script` is `script` compiled to a binary
                       ;; using foma.
                       compiled-script])

(defn new-morphology []
  (->Morphology [] "lexc" "" ""))

;; A `Morphophonology` is a foma script, its compiled foma FST, and the
;; morphology and phonology whose scripts are concatenated in order to build
;; the morphophonology's script.
(defrecord Morphophonology [id
                            ^Morphology morphology
                            ^Phonology phonology
                            ;; The `script` is a foma script that is essentially
                            ;; a concatenation of the scripts of the
                            ;; `phonology` and the `morphology`.
                            script
                            ;; The `compiled-script` is `script` compiled to a
                            ;; binary using foma.
                            compiled-script])

(defn new-morphophonology []
  (->Morphophonology (new-morphology) (new-phonology) "" ""))

;; A `CandidateRanker` is a data structure that can be used to rank a set of
;; candidate parses according to their probabilities.
(defrecord CandidateRanker
  [id
   ;; The `corpus` is a vector of linguistics forms which should contain
   ;; morphologically analyzed words from which we can build a morpheme
   ;; language model.
   corpus
   ;; The `arpa` is the language model in ARPA format, which is a textual
   ;; representation of the N-grams extracted from `corpus`. It can be used to
   ;; estimate a `language-model` using MITLM.
   arpa
   ;; The `language-model` is a binary that can be used (by MITLM) to assign
   ;; probabilities to sequences of morphemes, i.e., parses.
   language-model])

(defn new-candidate-ranker []
  (->CandidateRanker [] "" ""))

;; A `MorphologicalParser` is a data structure that encodes how to parse words
;; into their morphemes and how to generate surface representations of morpheme
;; sequences.
(defrecord MorphologicalParser [id
                                ^Morphophonology morphophonology
                                ^CandidateRanker candidate-ranker])

(defn new-morphological-parser []
  (->MorphologicalParser (new-morphophonology) (new-candidate-ranker)))

(def entities
  {:phonology map->Phonology
   :morphology map->Morphology
   :morphophonology map->Morphophonology
   :candidate-ranker map->CandidateRanker
   :morphological-parser map->MorphologicalParser})

(defn make-entity [entity-kw entity-map]
  (let [map->constructor (entity-kw entities)
        id (:id entity-map)]
    (if id
      (map->constructor entity-map)
      (map->constructor (assoc entity-map :id (utils/uuid))))))

(def make-phonology (partial make-entity :phonology))
(def make-morphology (partial make-entity :morphology))
(def make-morphophonology (partial make-entity :morphophonology))
(def make-candidate-ranker (partial make-entity :candidate-ranker))
(def make-morphological-parser (partial make-entity :morphological-parser))
