(ns opening-hours-serverless.parser-test
  (:require [clojure.test :refer :all]
            [opening-hours-serverless.parser :refer :all]
            [cheshire.core :refer [generate-string]]))

(def single-day {:thursday [{:type "open", :value 36000}
                            {:type "close", :value 64800}]})

(def test-opening-hours {:friday [{:type "open", :value 64800}]
                         :saturday [{:type "close", :value 3600}
                                    {:type "open", :value 32400}
                                    {:type "close", :value 39600}
                                    {:type "open", :value 57600}
                                    {:type "close", :value 82800}]})

(def test-opening-hours-wrong-order {:friday [{:type "open", :value 64800}]
                                     :saturday [{:type "close", :value 39600}
                                                {:type "open", :value 32400}
                                                {:type "close", :value 3600}
                                                {:type "close", :value 82800}
                                                {:type "open", :value 57600}]})

(def full-schedule {:monday []
                    :tuesday [{:type "open", :value 36000} {:type "close", :value 64800}]
                    :wednesday []
                    :thursday [{:type "open", :value 36000} {:type "close", :value 64800}]
                    :friday [{:type "open", :value 36000}]
                    :saturday [{:type "close", :value 3600} {:type "open", :value 36000}]
                    :sunday [{:type "close", :value 3600} {:type "open", :value 43200} {:type "close", :value 75600}]})

(def full-schedule-wrong-order {:monday []
                                :wednesday []
                                :thursday [{:type "open", :value 36000} {:type "close", :value 64800}]
                                :tuesday [{:type "open", :value 36000} {:type "close", :value 64800}]
                                :saturday [{:type "close", :value 3600} {:type "open", :value 36000}]
                                :friday [{:type "open", :value 36000}]
                                :sunday [{:type "close", :value 3600} {:type "open", :value 43200} {:type "close", :value 75600}]})

(def full-schedule-wrong-order-with-unsorted-vals {:monday []
                                :wednesday []
                                :thursday [{:type "open", :value 36000} {:type "close", :value 64800}]
                                :tuesday [{:type "close", :value 64800} {:type "open", :value 36000}]
                                :saturday [{:type "open", :value 36000} {:type "close", :value 3600} ]
                                :friday [{:type "open", :value 36000}]
                                :sunday [ {:type "open", :value 43200} {:type "close", :value 75600} {:type "close", :value 3600}]})

(deftest sort-by-day-test
  (testing "Sorting by day works and ignores nil days"
    (is (= {:monday 1 :wednesday 2 :saturday 3}
          (sort-by-day {:monday 1 :tuesday nil :wednesday 2 :thursday nil :friday nil :saturday 3 :sunday nil})))
    (is (= {:monday 1 :wednesday 2 :saturday 3}
          (sort-by-day {:tuesday nil :wednesday 2 :monday 1 :thursday nil :saturday 3 :sunday nil :friday nil })))
    (is (= {:monday 1 :wednesday 2 :saturday 3}
          (sort-by-day {:wednesday 2 :saturday 3 :monday 1 })))
    (is (= test-opening-hours (sort-by-day test-opening-hours)))
    (is (= full-schedule (sort-by-day full-schedule)))
    (is (= full-schedule (sort-by-day full-schedule-wrong-order)))))

(deftest parse-into-pairs-test
         (testing "Parsing into pairs"
    (is (= [{:current [:a 1]
             :next    nil}]
           (parse-into-day-pairs {:a 1})))
    (is (= [{:current [:a 1]
             :next    [:b 2]}
            {:current [:b 2]
             :next    [:c 3]}
            {:current [:c 3]
             :next    nil}]
           (parse-into-day-pairs {:a 1 :b 2 :c 3})))
    (is (= [{:current [:thursday [{:type  "open" :value 36000}
                                 {:type  "close" :value 64800}]]
             :next    nil}]
           (parse-into-day-pairs single-day)))))

(deftest format-schedules-test
         (testing "formatting"
    (is (= ["Closed"] (format-schedule {} {})))
    (is (= ["12 AM - 1 AM"] (format-schedule [{:type "open" :value 1}] [{:type "close" :value 3600}])))
    (is (= ["12 AM - 1 AM" "2 AM - 4 AM"] (format-schedule [{:type "open" :value 1} {:type "open" :value 7200}] [{:type "close" :value 3600} {:type "close" :value 14400}])))))

(deftest pairs-to-schedule-test
  (testing "Parsing from pairs to schedule"
    (is (= '({:day      :thursday
              :schedule ("10 AM - 6 PM")})
           (pairs->schedule [{:current [:thursday
                                        [{:type  "open"
                                          :value 36000}
                                         {:type  "close"
                                          :value 64800}]]
                              :next    nil}])))
    (is (= '({:day      :friday
              :schedule  ("6 PM - 1 AM")}
              {:day      :saturday
               :schedule ("9 AM - 11 AM"
                           "4 PM - 11 PM")})
           (-> test-opening-hours
               parse-into-day-pairs
               pairs->schedule))))
 (testing "Opening hours are parsed correctly even if they are in the wrong order"
    (is (= (-> test-opening-hours
               parse-into-day-pairs
               pairs->schedule)
           (-> test-opening-hours-wrong-order
               parse-into-day-pairs
               pairs->schedule)))))

(deftest parse-schedule-from-json-test
  (testing "Opening hours are parsed from JSON input"
    (is (= "Monday: Closed"
           (parse-schedule {:monday [] :tuesday nil})))
    (is (= (str "Friday: 6 PM - 1 AM" \newline
                "Saturday: 9 AM - 11 AM, 4 PM - 11 PM")
           (parse-schedule test-opening-hours)))
    (is (= (str "Monday: Closed" \newline
                "Tuesday: 10 AM - 6 PM" \newline
                "Wednesday: Closed" \newline
                "Thursday: 10 AM - 6 PM" \newline
                "Friday: 10 AM - 1 AM" \newline
                "Saturday: 10 AM - 1 AM" \newline
                "Sunday: 12 PM - 9 PM")
           (parse-schedule full-schedule)))
    (is (= "Monday: Closed\nTuesday: 10 AM - 6 PM\nWednesday: Closed\nThursday: 10 AM - 6 PM\nFriday: 10 AM - 1 AM\nSaturday: 10 AM - 1 AM\nSunday: 12 PM - 9 PM"
           (parse-schedule full-schedule-wrong-order)))
    (is (= "Monday: Closed\nTuesday: 10 AM - 6 PM\nWednesday: Closed\nThursday: 10 AM - 6 PM\nFriday: 10 AM - 1 AM\nSaturday: 10 AM - 1 AM\nSunday: 12 PM - 9 PM"
           (parse-schedule full-schedule-wrong-order-with-unsorted-vals)))))
