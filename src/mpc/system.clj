(ns mpc.system
  (:require [mpc.server :as server]
            [com.stuartsierra.component :as component]))

(def system-components [:app])

(defrecord MPCSystem [config-options app]
  component/Lifecycle
  (start [this]
    (component/start-system this system-components))
  (stop [this]
    (component/stop-system this system-components)))

(defn make-system [config-options]
  (let [{:keys [port]} config-options]
    (map->MPCSystem
      {:config-options config-options
       :app (server/make-server port)})))
