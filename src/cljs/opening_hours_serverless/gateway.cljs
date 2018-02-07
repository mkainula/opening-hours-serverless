(ns opening-hours-serverless.gateway
  (:require [cljs-lambda.macros :refer-macros [defgateway]]
            [opening-hours-serverless.parser :refer [parse-schedule]]))

(defn parse-schedule-from-json [json]
  "Parse JSON into Clojure map and return output from parse-schedule"
  (-> (.parse js/JSON json)
      (js->clj :keywordize-keys true)
      (parse-schedule)))

(defgateway parse-opening-hours [event ctx]
  {:status  200
   :headers {:content-type "text/plain"}
   :body    (parse-schedule-from-json (:body event))})
