(defproject mpc "0.1.0-SNAPSHOT"
  :description "MPC is the Morphological Parser Creator, an application that
               allows users to build morphological parsers for natural
               languages using finite state transducer (FST) technology (foma)
               and N-gram language models (MITLM). Part of the OLD (Online
               Linguistic Database) project"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.8.0"]
                 [compojure "1.6.0"]
                 [environ "1.0.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [hiccup "1.0.5"]
                 [inflections "0.13.0"]
                 [com.layerware/hugsql "0.4.8"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [org.postgresql/postgresql "9.4.1208"]
                 [postgre-types "0.0.4"]
                 [ragtime "0.7.2"]
                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-mock "0.3.2"]
                 [ring/ring-json "0.4.0"]]
  :main ^:skip-aot mpc.core
  :target-path "target/%s"
  :profiles {:dev {:source-paths ["dev"]
                   :env {:http-port 3000}}
                   :dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]]
             :prod {:env {:http-port 8000
                          :repl-port 8001}
                    :dependencies [[org.clojure/tools.nrepl "0.2.12"]]}
             :test {:ragtime {:database
                              "jdbc:postgresql://localhost:5432/mpc?user=mpc&password=mpc"}}
             :uberjar {:aot :all}}
  :plugins [[lein-environ "1.0.0"]
            ; The lein-ring plugin allows us to easily start a development web
            ; server with "lein ring server". It also allows us to package up
            ; our application as a standalone .jar or as a .war for deployment
            ; to a servlet container.
            [lein-ring "0.12.2"]
            [com.jakemccrary/lein-test-refresh "0.22.0"]
            [ragtime/ragtime.lein "0.3.9"]]
  :ring {:handler mpc.app/app
         :nrepl {:start? true
                 :port 9998}}
  :ragtime {:migrations ragtime.sql.files/resources/migrations
            :database (System/getenv "MPC_DB_URI")}
)
