(ns mpc.app-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is]]
            [ring.mock.request :refer [request]]
            [mpc.app :refer [app]]
            ))

(deftest test-app
  (testing "morphological parsers endpoint"
    (let [response (app (request :get "/morphologicalparsers"))
          content-type (get-in response [:headers "Content-Type"])]
      ;(is (= 200 (:status response)))
      (is (str/starts-with? content-type "application/json")))
    )

  (testing "phonologies endpoint"
    (let [response (app (request :get "/phonologies"))
          content-type (get-in response [:headers "Content-Type"])]
      ;(is (= 200 (:status response)))
      (is (str/starts-with? content-type "application/json")))
    )

  (testing "not-found route"
    (let [response (app (request :get "/bogus-route"))]
      (is (= (:status response) 404)))))
