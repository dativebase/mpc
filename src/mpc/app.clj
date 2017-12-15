(ns mpc.app
  "Entry point for the MPC"
  (:require [clojure.pprint :as pprint]
            [compojure.core :as cmpjr]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-body]]
            [ring.util.response :refer [response]]
            [mpc.db]
            [mpc.entities :as e]
            ))

(declare get-phonologies)
(declare create-phonology)
(declare get-phonology)
(declare delete-phonology)

(defn get-phonologies [_]
  {:status 200
   :body {:count (mpc.db/count-phonologies)
          :results (mpc.db/get-phonologies)}})

(defn get-morphologies [_]
  {:status 200
   :body {:count (mpc.db/count-morphologies)
          :results (mpc.db/get-morphologies)}})

(defn get-morphophonologies [_]
  {:status 200
   :body {:count (mpc.db/count-morphophonologies)
          :results (mpc.db/get-morphophonologies)}})

(defn get-candidate-rankers [_]
  {:status 200
   :body {:count (mpc.db/count-candidate-rankers)
          :results (mpc.db/get-candidate-rankers)}})

(defn get-morphological-parsers [_]
  {:status 200
   :body {:count (mpc.db/count-morphological-parsers)
          :results (mpc.db/get-morphological-parsers)}})

(defn create-phonology [{phonology-data :body}]
  (let [new-phonology (mpc.db/create-phonology
                        (e/make-phonology phonology-data))]
     {:status 201
      :headers {"Location" (str "/phonologies/" (:id new-phonology))}}))

(cmpjr/defroutes app-routes

  (cmpjr/context "/phonologies" []
           (cmpjr/GET "/" [] get-phonologies)
           (cmpjr/POST "/" [] create-phonology)
           ;(cmpjr/GET "/:id" [id] get-phonology)
           ;(cmpjr/DELETE "/:id" [id] delete-phonology)
           )

  (cmpjr/context "/morphologies" []
           (cmpjr/GET "/" [] get-morphologies)
           ;(cmpjr/POST "/" [] create-morphology)
           ;(cmpjr/GET "/:id" [id] get-morphology)
           ;(cmpjr/DELETE "/:id" [id] delete-morphology)
           )

  (cmpjr/context "/morphophonologies" []
           (cmpjr/GET "/" [] get-morphophonologies)
           ;(cmpjr/POST "/" [] create-morphophonology)
           ;(cmpjr/GET "/:id" [id] get-morphophonology)
           ;(cmpjr/DELETE "/:id" [id] delete-morphophonology)
           )

  (cmpjr/context "/candidaterankers" []
           (cmpjr/GET "/" [] get-candidate-rankers)
           ;(cmpjr/POST "/" [] create-candidate-ranker)
           ;(cmpjr/GET "/:id" [id] get-candidate-ranker)
           ;(cmpjr/DELETE "/:id" [id] delete-candidate-ranker)
           )

  (cmpjr/context "/morphologicalparsers" []
           (cmpjr/GET "/" [] get-morphological-parsers)
           ;(cmpjr/POST "/" [] create-morphological-parser)
           ;(cmpjr/GET "/:id" [id] get-morphological-parser)
           ;(cmpjr/DELETE "/:id" [id] delete-morphological-parser)
           )

  (route/not-found
    (response {:message "Page not found"})))


; Behold, our middleware! Note that it's common to prefix our middleware name
; with "wrap-", since it surrounds any routes an other middleware "inside"
(defn wrap-log-request [handler]
  (fn [request] ; return handler function
    (pprint/pprint request) ; perform logging
    (handler request))) ; pass the request through to the inner handler

; We can attach our middleware directly to the main application handler. All
; requests/responses will be "filtered" through our logging handler.
(def app
  (-> app-routes
    ;wrap-log-request
    wrap-json-response
    (wrap-json-body {:keywords? true})
    ))
