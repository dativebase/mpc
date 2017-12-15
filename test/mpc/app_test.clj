(ns mpc.app-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [cheshire.core :refer [parse-string generate-string]]
            [ring.mock.request :as mock]
            [mpc.app :refer [app]]
            [mpc.db :as db]
            [mpc.test-core :refer [with-rollback]]
            [mpc.utils :refer [substring?]]
            ))

; WIll be rebound in test
(def ^{:dynamic true} *session-id* nil)

;; (defn with-session [t]
;;   (let [user (u/create {:name "Some admin"
;;                         :email "theadmin@example.com"
;;                         :password "sup3rs3cr3t"
;;                         :level :restful-clojure.auth/admin})
;;         session-id (auth/make-token! (:id user))]
;;     (with-bindings {#'*session-id* session-id}
;;       (t))
;;     (u/delete-user user)))
;; (use-fixtures :once with-session)

(use-fixtures :each with-rollback)

(deftest main-route

  (testing "phonologies endpoint"
    (let [response (app (mock/request :get "/phonologies"))
          content-type (get-in response [:headers "Content-Type"])]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8" content-type))))

  (testing "morphologies endpoint"
    (let [response (app (mock/request :get "/morphologies"))
          content-type (get-in response [:headers "Content-Type"])]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8" content-type))))

  (testing "morphophonologies endpoint"
    (let [response (app (mock/request :get "/morphophonologies"))
          content-type (get-in response [:headers "Content-Type"])]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8" content-type))))

  (testing "candidaterankers endpoint"
    (let [response (app (mock/request :get "/candidaterankers"))
          content-type (get-in response [:headers "Content-Type"])]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8" content-type))))

  (testing "morphological parsers endpoint"
    (let [response (app (mock/request :get "/morphologicalparsers"))
          content-type (get-in response [:headers "Content-Type"])]
      (is (= 200 (:status response)))
      (is (str/starts-with? content-type "application/json"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/bogus-route"))]
      (is (= (:status response) 404))))

)

(def phonology-script "define phonology a -> b | c _ d ;")

(deftest creating-phonology
  (testing "POST /phonology"
    (let [phonology-count (db/count-phonologies)
          response (app (-> (mock/request :post "/phonologies")
                            (mock/body (generate-string {:script phonology-script}))
                            (mock/content-type "application/json")
                            (mock/header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (substring? "/phonologies/" (get-in response [:headers "Location"])))
      (is (= (inc phonology-count) (db/count-phonologies))))))
