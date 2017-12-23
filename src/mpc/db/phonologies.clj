(ns mpc.db.phonologies
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "mpc/db/sql/phonologies.sql")

(hugsql/def-sqlvec-fns "mpc/db/sql/phonologies.sql")
