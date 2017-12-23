(ns mpc.server
  (:require [compojure.core :as compojure]
            [com.stuartsierra.component :as component]
            [mpc.app :as app]
            [ring.adapter.jetty :as jetty]))

(compojure/defroutes app
                     app/app)

(defrecord Server [port server]
  component/Lifecycle
  (start [this]
    (println ";; Starting MPC server")
    (let [server (jetty/run-jetty app {:port port :join? false})]
      (assoc this :server server)))
  (stop [this]
    (println ";; Stopping MPC server")
    (.stop server)
    this))

(defn make-server
  [port]
  (map->Server {:port port}))
