(ns mpc.user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
                (:require
                  [clojure.java.io :as io]
                  [clojure.java.javadoc :refer (javadoc)]
                  [clojure.pprint :refer (pprint)]
                  [clojure.reflect :refer (reflect)]
                  [clojure.repl :refer (apropos dir doc find-doc pst source)]
                  [clojure.set :as set]
                  [clojure.string :as str]
                  [clojure.test :as test]
                  [clojure.tools.namespace.repl :refer (refresh refresh-all)]
                  [com.stuartsierra.component :as component]
                  [mpc.system :refer :all]))

(def system nil)

(defn init []
  (println "in init")
  (alter-var-root #'system
                  (constantly (make-system {:port 9007}))))

(defn start []
  (println "in start")
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (println "in go")
  (init)
  (println "done init")
  (start)
  (println "done start")
  )

(defn reset []
  (stop)
  (refresh :after 'mpc.user/go))
