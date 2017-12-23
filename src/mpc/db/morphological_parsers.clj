(ns mpc.db.morphological-parsers
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "mpc/db/sql/morphological_parsers.sql")

(hugsql/def-sqlvec-fns "mpc/db/sql/morphological_parsers.sql")
