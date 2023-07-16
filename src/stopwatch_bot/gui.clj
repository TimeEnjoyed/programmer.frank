(ns stopwatch-bot.gui
  (:require [stopwatch-bot.date :as date])
  (:import [java.awt.event ActionListener]
           [java.awt Color Font BorderLayout RenderingHints Graphics2D]
           [javax.swing Timer JFrame JLabel JPanel SwingConstants]))

(def start-time (atom 0))
(def initial-time (atom 0))
(def time-format (atom "mm:ss"))

; custom counter label
(def label (proxy [JLabel] []
             ; override the paintComponent method to customize label rendering
             (paintComponent [g]
               (let [g2 (cast Graphics2D g)]
                 (.setRenderingHint g2 RenderingHints/KEY_TEXT_ANTIALIASING
                                    RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
                 (proxy-super paintComponent g2)))))

; calculate the elapsed time in seconds
(defn get-elapsed-time []
  (quot (- @initial-time (- (System/currentTimeMillis) @start-time))
        1000))

(def timer (Timer. 100
                   (proxy [ActionListener] []
                     ; called 10 times per second to update the counter label
                     (actionPerformed [e]
                       (let [elapsed (get-elapsed-time)]
                         (when (<= elapsed 0)
                           (.stop timer))
                         (.setText label (date/format-time (max 0 elapsed) @time-format)))))))

; start the stopwatch with the specified duration in seconds
(defn start-stopwatch [seconds]
  (reset! start-time (System/currentTimeMillis))
  (reset! initial-time (+ (* 1000 seconds) 999))
  (.start timer))

; create and show the JFrame GUI with the counter label
(defn create-and-show-gui [font-name background-color-hex foreground-color-hex format]
  (reset! time-format format)
  (let [frame (JFrame. "Stopwatch")
        panel (JPanel. (BorderLayout.))
        background-color (Color/decode (str "#" background-color-hex))
        foreground-color (Color/decode (str "#" foreground-color-hex))]
    (.setBackground panel background-color)
    (.setFont label (Font. font-name Font/BOLD 48))
    (.setForeground label foreground-color)
    (.setHorizontalAlignment label SwingConstants/CENTER)
    (.setText label (date/format-time 0 @time-format))

    ; add the label to the panel
    (.add panel label BorderLayout/CENTER)

    ; add the panel to the frame
    (.add (.getContentPane frame) panel)

    (.setSize frame 400 200)
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setVisible frame true)))
