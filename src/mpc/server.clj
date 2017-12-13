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
    (println this)
    (println (type this))
    (println "server")
    (println server)
    (println (type server))
    (let [server (jetty/run-jetty app {:port port :join? false})
          _ (println "server")
          _ (println server)
          _ (println (type server))
          ]
      (assoc this :server server)))
  (stop [this]
    (println ";; Stopping MPC server")
    (println ";; almost done stopping MPC server")
    (println "server")
    (println server)
    (println (type server))
    (.stop server)
    (println ";; Done stopping MPC server")
    this))

(defn make-server
  [port]
  (map->Server {:port port}))
