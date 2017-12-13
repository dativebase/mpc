(ns mpc.app
  "Entry point for the MPC"
  (:require [clojure.pprint :as pprint]
            [compojure.core :refer [defroutes]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-body]]
            [ring.util.response :refer [response]]
            ))

(defroutes app-routes
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
    wrap-log-request
    wrap-json-response
    wrap-json-body
    ))
