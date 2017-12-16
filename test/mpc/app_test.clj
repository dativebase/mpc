(ns mpc.app-test
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [clojure.test :refer [use-fixtures deftest testing is]]
            [cheshire.core :refer [parse-string generate-string]]
            [ring.mock.request :as mock]
            [mpc.app :refer [app]]
            [mpc.db :as db]
            [mpc.test-core :refer [with-rollback]]
            [mpc.utils :refer [str->uuid substring?]]))

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
(def morphology-corpus '("in-perfect" "un-break-able"))
(def morphology-script-format "lexc")

(deftest creating-phonology
  (testing "POST /phonologies"
    (let [phonology-count (db/count-phonologies)
          response (app (-> (mock/request :post "/phonologies")
                            (mock/body (generate-string {:script phonology-script}))
                            (mock/content-type "application/json")
                            (mock/header "Accept" "application/json")))
          resource-location (get-in response [:headers "Location"])]
      (is (= (:status response) 201))
      (is (substring? "/phonologies/" resource-location))
      (is (= (inc phonology-count) (db/count-phonologies)))
      (let [response (app (-> (mock/request :get resource-location)
                              (mock/header "Accept" "application/json")))
            phonology-map (parse-string (:body response))
            phonology-id (get phonology-map "id")]
        (is (map? phonology-map))
        (is (= phonology-script (get phonology-map "script")))
        (is (instance? java.util.UUID (str->uuid phonology-id)))
        (is (substring? phonology-id resource-location))))))

(deftest creating-morphology
  (testing "POST /morphologies"
    (let [morphology-count (db/count-morphologies)
          response (app (-> (mock/request :post "/morphologies")
                            (mock/body (generate-string
                                         {:corpus morphology-corpus
                                          :script-format
                                          morphology-script-format}))
                            (mock/content-type "application/json")
                            (mock/header "Accept" "application/json")))
          resource-location (get-in response [:headers "Location"])]
      (is (= (:status response) 201))
      (is (substring? "/morphologies/" resource-location))
      (is (= (inc morphology-count) (db/count-morphologies)))
      (let [response (app (-> (mock/request :get resource-location)
                              (mock/header "Accept" "application/json")))
            morphology-map (parse-string (:body response))
            morphology-id (get morphology-map "id")]
        (is (map? morphology-map))
        (is (= morphology-corpus (get morphology-map "corpus")))
        (is (= morphology-script-format (get morphology-map "script-format")))
        (is (instance? java.util.UUID (str->uuid morphology-id)))
        (is (substring? morphology-id resource-location))))))

(deftest creating-morphophonology
  (testing "POST /morphophonologies"
    (let [morphophonology-count (db/count-morphophonologies)
          ;; Create and get a phonology and its id
          phonology-create-response
          (app (-> (mock/request :post "/phonologies")
                   (mock/body (generate-string {:script phonology-script}))
                   (mock/content-type "application/json")
                   (mock/header "Accept" "application/json")))
          phonology-location (get-in phonology-create-response [:headers
                                                                "Location"])
          phonology-get-response
          (app (-> (mock/request :get phonology-location)
                   (mock/header "Accept" "application/json")))
          phonology-map (parse-string (:body phonology-get-response))
          phonology-id (get phonology-map "id")

          ;; Create and get a morphology and its id
          morphology-create-response
          (app (-> (mock/request :post "/morphologies")
                   (mock/body (generate-string
                                {:corpus morphology-corpus
                                 :script-format
                                 morphology-script-format}))
                   (mock/content-type "application/json")
                   (mock/header "Accept" "application/json")))
          morphology-location (get-in morphology-create-response [:headers
                                                                  "Location"])
          morphology-get-response
          (app (-> (mock/request :get morphology-location)
                   (mock/header "Accept" "application/json")))
          morphology-map (parse-string (:body morphology-get-response))
          morphology-id (get morphology-map "id")
          response
          (app (-> (mock/request :post "/morphophonologies")
                   (mock/body (generate-string
                                {:phonology-id phonology-id
                                 :morphology-id morphology-id}))
                   (mock/content-type "application/json")
                   (mock/header "Accept" "application/json")))
          resource-location (get-in response [:headers "Location"])]
      (is (= (:status response) 201))
      (is (substring? "/morphophonologies/" resource-location))
      (is (= (inc morphophonology-count) (db/count-morphophonologies)))
      (let [response (app (-> (mock/request :get resource-location)
                              (mock/header "Accept" "application/json")))
            morphophonology-map (parse-string (:body response))
            morphophonology-id (get morphophonology-map "id")]
        (is (map? morphophonology-map))
        ;(is (= morphophonology-corpus (get morphophonology-map "corpus")))
        ;(is (= morphophonology-script-format (get morphophonology-map "script-format")))
        (is (instance? java.util.UUID (str->uuid morphophonology-id)))
        (is (substring? morphophonology-id resource-location))))))
