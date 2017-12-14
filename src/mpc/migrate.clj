(ns mpc.migrate
  (:require [ragtime.jdbc :as jdbc]
            [mpc.db :refer [connection-uri]]))

(def config
  {:datastore  (jdbc/sql-database {:connection-uri connection-uri})
   :migrations (jdbc/load-resources "migrations")})
