(ns mpc.utils
  (:require [clojure.string :as str]))

(defn parse-connection-uri [connection-uri]
  "Parse a database connection URI like
  `'jdbc:postgresql://localhost:5432/my_db_name?user=my_user&password=my_password'`
  to a map like
  `{:host \"localhost\"
    :port \"5432\"
    :db \"my_db_name\"
    :user \"my_user\"
    :db \"my_password\"}`
  "
  (let [[_ _ host etc] (str/split connection-uri #":")
        host (subs host 2)
        [port etc] (str/split etc #"/")
        [db etc] (str/split etc #"\?")
        ret (apply hash-map (flatten
                              (map (fn [x]
                                     (let [[k v] (str/split x #"=")]
                                       [(keyword k) v]))
                                   (str/split etc #"&"))))]
    (merge ret {:host host :port port :db db})))

(defn kw-replace [kw match replacement]
  (-> kw
      str
      (subs 1)
      (str/replace match replacement)
      keyword))

(defn db-col->record-attr [db-col]
  (kw-replace db-col "_" "-"))

(defn record-attr->db-col [record-attr]
  (kw-replace record-attr "-" "_"))

(defn db-cols->record-attrs [db-map]
  (reduce (fn [val [k v]]
            (assoc val (db-col->record-attr k) v))
          {}
          db-map))

(defn record-attrs->db-cols [record]
  (reduce (fn [val [k v]]
            (assoc val (record-attr->db-col k) v))
          {}
          record))

(defn substring? [substring string]
  (.contains string substring))

(defn uuid [] (java.util.UUID/randomUUID))

(defn str->uuid [string]
  (java.util.UUID/fromString string))


(defn earlier-than? [d1 d2] (= -1 (compare d1 d2)))

(defn later-than? [d1 d2] (= 0 (compare d1 d2)))
