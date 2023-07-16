(ns stopwatch-bot.date
  (:require [clj-time.coerce :as time-coerce]
            [clj-time.format :as time-format]))

(defn format-time [seconds format]
  (let [duration (time-coerce/from-long (* 1000 seconds))]
    (time-format/unparse (time-format/formatter format) duration)))

(defn parse-time [time-str]
  (if (re-matches #"^\d+$" time-str)
    (Long/parseLong time-str)
    (let [format (if (= (count (.split time-str ":")) 2)
                   "mm:ss"
                   "HH:mm:ss")
          datetime (time-format/parse (time-format/formatter format) time-str)]
      (quot (time-coerce/to-long datetime)
            1000))))
