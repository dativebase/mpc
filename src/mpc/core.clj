(ns mpc.core
  (:gen-class)
  (:require [mpc.system :as system]
            [com.stuartsierra.component :as component]))

(defn -main [& args]
  (let [[port] (Integer. args)]
    (component/start
      (system/make-system {:port port}))))
