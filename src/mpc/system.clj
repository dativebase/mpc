(ns mpc.system
  (:require [mpc.server :as server]
            [com.stuartsierra.component :as component]))

(def system-components [:app])

(defrecord MPCSystem [config-options app]
  component/Lifecycle
  (start [this]
    (println "in start of MPCSystem")
    (println this)
    (println (type this))
    (component/start-system this system-components))
  (stop [this]
    (component/stop-system this system-components)))

(defn make-system [config-options]
  (let [_ (println "here") {:keys [port]} config-options]
    (println "got port" port)
    (map->MPCSystem
      {:config-options config-options
       :app (server/make-server port)})))
