(ns stopwatch-bot.gui
  (:require [clojure.string :as str])
  (:import [java.awt.event ActionListener]
           [java.awt Color Font BorderLayout RenderingHints Graphics2D]
           [javax.swing Timer JFrame JLabel JPanel SwingConstants]))

(def start-time (atom 0))
(def initial-time (atom 0))

(def label (proxy [JLabel] ["00:00:00"]
             (paintComponent [g]
               (let [g2 (cast Graphics2D g)] ; Cast to Graphics2D object
                 (.setRenderingHint g2 RenderingHints/KEY_TEXT_ANTIALIASING
                                    RenderingHints/VALUE_TEXT_ANTIALIAS_ON) ; Enable anti-aliasing
                 (proxy-super paintComponent g2)))))

(declare get-elapsed-time)

(defn format-time [time]
  (let [seconds (quot time 1000)
        minutes (quot seconds 60)
        hours (quot minutes 60)]
    (format "%02d:%02d:%02d" hours (mod minutes 60) (mod seconds 60))))

(defn get-elapsed-time []
  (- @initial-time (- (System/currentTimeMillis) @start-time)))

(defn parse-time [time-str]
  (let [parts (map #(Integer/parseInt %) (str/split time-str #":"))
        parts-count (count parts)]
    (cond
      (= parts-count 3) (+ (* (nth parts 0) 60 60)
                           (* (nth parts 1) 60)
                           (* (nth parts 2)))
      (= parts-count 2) (+ (* (nth parts 0) 60)
                           (* (nth parts 1)))
      (= parts-count 1) (* (nth parts 0)))))

(def timer (Timer. 100
                   (proxy [ActionListener] []
                     (actionPerformed [e]
                       (let [elapsed (get-elapsed-time)]
                         (when (<= elapsed 0)
                           (.stop timer))
                         (.setText label (format-time (max 0 elapsed))))))))

(defn start-stopwatch [time]
  (reset! start-time (System/currentTimeMillis))
  (reset! initial-time (+ (* 1000 (parse-time time)) 999))
  (.start timer))

(defn create-and-show-gui [font-name]
  (let [frame (JFrame. "Stopwatch")
        panel (JPanel. (BorderLayout.))]

    (.setBackground panel (Color. 255 0 255)) ; Set background color of the panel to magenta
    (.setFont label (Font. font-name Font/BOLD 48)) ; Set font of the label
    (.setHorizontalAlignment label SwingConstants/CENTER)

    ;; Place the label in the center of the panel
    (.add panel label BorderLayout/CENTER)

    (.add (.getContentPane frame) panel)
    (.setSize frame 400 200)
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setVisible frame true)))