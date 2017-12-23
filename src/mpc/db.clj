(ns mpc.db
  (:require [cheshire.core :refer [parse-string generate-string]]
            [environ.core :refer [env]]
            [hugsql.core :as hugsql]
            [postgre-types.json :refer [add-json-type]]
            [mpc.db.candidate-rankers]
            [mpc.db.morphologies]
            [mpc.db.morphological-parsers]
            [mpc.db.morphophonologies]
            [mpc.db.phonologies]
            ))

(add-json-type generate-string parse-string)

(def default-connection-uri
  "jdbc:postgresql://localhost:5432/mpc?user=mpc&password=mpc")

(def connection-uri
  (or (env :mpc-db-uri) default-connection-uri))

(hugsql/def-db-fns "mpc/db/sql/db.sql")

(hugsql/def-sqlvec-fns "mpc/db/sql/db.sql")

(defn create-tables [db]
  (drop-task-status-type db)
  (create-task-status-type db)
  (drop-script-format-type db)
  (create-script-format-type db)

  (mpc.db.phonologies/drop-phonology-table db)
  (mpc.db.morphologies/drop-morphology-table db)
  (mpc.db.morphophonologies/drop-morphophonology-table db)
  (mpc.db.candidate-rankers/drop-candidate-ranker-table db)
  (mpc.db.morphological-parsers/drop-morphological-parser-table db)

  (mpc.db.phonologies/create-phonology-table db)
  (mpc.db.morphologies/create-morphology-table db)
  (mpc.db.morphophonologies/create-morphophonology-table db)
  (mpc.db.candidate-rankers/create-candidate-ranker-table db)
  (mpc.db.morphological-parsers/create-morphological-parser-table db))

(defn drop-tables [db]
  (mpc.db.morphological-parsers/drop-morphological-parser-table db)
  (mpc.db.morphophonologies/drop-morphophonology-table db)
  (mpc.db.phonologies/drop-phonology-table db)
  (mpc.db.morphologies/drop-morphology-table db)
  (mpc.db.candidate-rankers/drop-candidate-ranker-table db)
  (drop-task-status-type db)
  (drop-script-format-type db))
