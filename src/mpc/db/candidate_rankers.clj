(ns mpc.db.candidate-rankers
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "mpc/db/sql/candidate_rankers.sql")

(hugsql/def-sqlvec-fns "mpc/db/sql/candidate_rankers.sql")
