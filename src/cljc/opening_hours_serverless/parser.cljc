(ns opening-hours-serverless.parser
  #?(:clj (:require [clj-time.core :as c]
                    [clj-time.format :as f]
                    [clojure.string :as str]))
  #?(:cljs (:require [cljs-time.core :as c]
                     [cljs-time.format :as f]
                     [clojure.string :as str])))

(defn sort-by-day [{:keys [monday tuesday wednesday thursday friday saturday sunday]}]
  (let [days {:monday monday
              :tuesday tuesday
              :wednesday wednesday
              :thursday thursday
              :friday friday
              :saturday saturday
              :sunday sunday}]
    (->> days
         (filter #(some? (second %)))
         (into {}))))

(defn parse-into-day-pairs [parsed-json]
  (loop [schedule parsed-json
         result []]
    (if schedule
      (let [current-day (first schedule)
            next-day (second schedule)]
        (recur (next schedule) (conj result {:current current-day
                                             :next next-day})))
      result)))

(defn format-date [seconds]
  (let [dt (-> (c/epoch)
               (c/plus (c/seconds seconds)))
        formatter (f/formatter "h a")]
    (.toUpperCase (f/unparse formatter dt))))

(defn pair->opening-hours-str [open closed]
  (str (format-date (:value open))
       " - "
       (format-date (:value closed))))

(defn format-schedule [open closed]
  (if (empty? open)
    ["Closed"]
    (map pair->opening-hours-str open closed)))

(defn schedule->str [schedule]
  (->> schedule
       (map #(str (str/capitalize (name (:day %))) ": " (clojure.string/join ", " (:schedule %))))
       (str/join \newline)))

(defn pairs->schedule [parsed]
  (map (fn [{:keys [current next]}]
         (let [current-values (second current)
               closed (->> current-values
                           (filter #(= (:type %) "close"))
                           (sort-by :value))
               open (->> current-values
                         (filter #(= (:type %) "open"))
                         (sort-by :value))
               matching-counts? (= (count open) (count closed))
               closed-next (if (> (count open) (count closed))
                             (->> (second next)
                                  (filter #(= (:type %) "close"))
                                  (sort-by :value)
                                  first
                                  list)
                             (rest closed))
               opening-hours (if matching-counts?
                               (format-schedule open closed)
                               (format-schedule open closed-next))]
           {:day (first current)
            :schedule opening-hours})) parsed))

(defn parse-schedule [schedule]
  (-> schedule
      (sort-by-day)
      (parse-into-day-pairs)
      (pairs->schedule)
      (schedule->str)))