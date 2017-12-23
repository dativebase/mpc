(ns mpc.db.morphologies
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "mpc/db/sql/morphologies.sql")

(hugsql/def-sqlvec-fns "mpc/db/sql/morphologies.sql")
