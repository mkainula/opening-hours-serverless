(defproject opening-hours-serverless "0.1.0"
  :dependencies [[org.clojure/clojure       "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [clj-time "0.13.0"]
                 [cheshire "5.8.0"]
                 [io.nervous/cljs-lambda    "0.3.5"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]]
  :plugins [[lein-npm                    "0.6.2"]
            [io.nervous/lein-cljs-lambda "0.6.6"]
            [lein-doo "0.1.7"]]
  :npm {:dependencies [[serverless-cljs-plugin "0.1.2"]]}
  :main ^:skip-aot opening-hours-serverless.core
  :uberjar-name "opening-hours.jar"
  :profiles {:uberjar {:aot :all}}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :cljs-lambda {:compiler
                {:inputs  ["src/clj" "src/cljs" "src/cljc"]
                 :options {:output-to     "target/opening-hours-serverless/opening_hours_serverless.js"
                           :output-dir    "target/opening-hours-serverless"
                           :target        :nodejs
                           :language-in   :ecmascript5
                           :optimizations :simple}}})
