(ns mpc.db.morphophonologies
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "mpc/db/sql/morphophonologies.sql")

(hugsql/def-sqlvec-fns "mpc/db/sql/morphophonologies.sql")
