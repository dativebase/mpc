(ns mpc.app
  "Entry point for the MPC"
  (:require [clojure.pprint :as pprint]
            [compojure.core :as cmpjr]
            [compojure.route :as route]
            [inflections.core :as infl]
            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-body]]
            [ring.util.response :refer [response]]
            [mpc.db]
            [mpc.entities :as e]
            [mpc.utils :as utils]))

;; ==============================================================================
;; Get all resources functions
;; ==============================================================================

(defn get-resources [resource-kw _]
  (let [resource-str (name resource-kw)
        resource-str-pl (infl/plural resource-str)
        rows-getter (->> resource-str-pl
                         (str "get-")
                         symbol
                         (ns-resolve 'mpc.db))
        rows-counter (->> resource-str-pl
                          (str "count-")
                          symbol
                          (ns-resolve 'mpc.db))]
  {:status 200
   :body {:count (rows-counter)
          :results (rows-getter)}}))

(def get-phonologies (partial get-resources :phonology))
(def get-morphologies (partial get-resources :morphology))
(def get-morphophonologies (partial get-resources :morphophonologies))
(def get-candidate-rankers (partial get-resources :candidate-ranker))
(def get-morphological-parsers (partial get-resources :morphological-parser))

;; ==============================================================================
;; Get single resource functions
;; ==============================================================================

(defn get-resource [resource-kw {{:keys [id]} :params}]
  (let [resource-str (name resource-kw)
        row-getter (->> resource-str
                        (str "get-")
                        symbol
                        (ns-resolve 'mpc.db))]
    (-> id
        utils/str->uuid
        row-getter
        response)))

(def get-phonology (partial get-resource :phonology))
(def get-morphology (partial get-resource :morphology))
(def get-morphophonology (partial get-resource :morphophonology))
(def get-candidate-ranker (partial get-resource :candidate-ranker))
(def get-morphological-parser (partial get-resource :morphological-parser))

;; ==============================================================================
;; Create resource functions
;; ==============================================================================

(defn create-resource [resource-kw {resource-data :body}]
  (let [resource-str (name resource-kw)
        resource-str-pl (infl/plural resource-str)
        record-constructor (->> resource-str
                                (str "make-")
                                symbol
                                (ns-resolve 'mpc.entities))
        row-creator (->> resource-str
                         (str "create-")
                         symbol
                         (ns-resolve 'mpc.db))
        new-resource (-> resource-data
                         record-constructor
                         row-creator)]
     {:status 201
      :headers {"Location"
                (format "/%s/%s" resource-str-pl (:id new-resource))}}))

(def create-phonology (partial create-resource :phonology))
(def create-morphology (partial create-resource :morphology))
(def create-morphophonology (partial create-resource :morphophonology))
(def create-candidate-ranker (partial create-resource :candidate-ranker))
(def create-morphological-parser (partial create-resource :morphological-parser))

;; ==============================================================================
;; Routes
;; ==============================================================================

(cmpjr/defroutes app-routes

  (cmpjr/context "/phonologies" []
           (cmpjr/GET "/" [] get-phonologies)
           (cmpjr/POST "/" [] create-phonology)
           (cmpjr/GET "/:id" [id] get-phonology)
           ;(cmpjr/DELETE "/:id" [id] delete-phonology)
           )

  (cmpjr/context "/morphologies" []
           (cmpjr/GET "/" [] get-morphologies)
           (cmpjr/POST "/" [] create-morphology)
           (cmpjr/GET "/:id" [id] get-morphology)
           ;(cmpjr/DELETE "/:id" [id] delete-morphology)
           )

  (cmpjr/context "/morphophonologies" []
           (cmpjr/GET "/" [] get-morphophonologies)
           (cmpjr/POST "/" [] create-morphophonology)
           (cmpjr/GET "/:id" [id] get-morphophonology)
           ;(cmpjr/DELETE "/:id" [id] delete-morphophonology)
           )

  (cmpjr/context "/candidaterankers" []
           (cmpjr/GET "/" [] get-candidate-rankers)
           (cmpjr/POST "/" [] create-candidate-ranker)
           (cmpjr/GET "/:id" [id] get-candidate-ranker)
           ;(cmpjr/DELETE "/:id" [id] delete-candidate-ranker)
           )

  (cmpjr/context "/morphologicalparsers" []
           (cmpjr/GET "/" [] get-morphological-parsers)
           (cmpjr/POST "/" [] create-morphological-parser)
           (cmpjr/GET "/:id" [id] get-morphological-parser)
           ;(cmpjr/DELETE "/:id" [id] delete-morphological-parser)
           )

  (route/not-found
    (response {:message "Page not found"})))

;; ==============================================================================
;; Custom middleware
;; ==============================================================================

(defn wrap-log-request [handler]
  (fn [request]
    (pprint/pprint request)
    (handler request)))

;; ==============================================================================
;; App
;; ==============================================================================

(def app
  (-> app-routes
    wrap-json-response
    (wrap-json-body {:keywords? true})))
