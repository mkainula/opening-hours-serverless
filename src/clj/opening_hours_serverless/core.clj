(ns opening-hours-serverless.core
  (:gen-class)
  (:require [opening-hours-serverless.parser :refer :all]
            [cheshire.core :as cheshire]))

(defn parse-schedule-from-json-file [file]
  "Read file, parse JSON and print results from parse-schedule"
  (-> (slurp file)
      (cheshire/parse-string true)
      (parse-schedule)
      (println)))

(defn -main [& args]
  (try
    (parse-schedule-from-json-file (first args))
    (catch Exception e
      (println "Oh no, something went wrong: " e))))