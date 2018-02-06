(ns opening-hours-serverless.core
  (:gen-class)
  (:require [opening-hours-serverless.parser :refer :all]
            [cheshire.core :as cheshire]))

(defn parse-schedule-from-json-file [file]
  (-> (slurp file)
      (cheshire/parse-string true)
      (parse-schedule)
      (println)))

(defn -main [& args]
  (parse-schedule-from-json-file (first args)))