(ns stopwatch-bot.core
  (:require [stopwatch-bot.gui :as gui]
            [stopwatch-bot.date :as date]
            [manifold.stream :as s]
            [aleph.tcp :as tcp]
            [byte-streams :as bs]
            [aero.core :refer [read-config]]
            [clojure.string :as str]))

; read configuration
(def config (read-config "config.edn"))

; allowed users
(def allowed-users (atom []))

; parse raw Twitch chat message into user, type, and content
(defn parse-message [message]
  (let [parts (str/split message #" ")]
    (if (= (first parts) "PING")
      {:type (first parts) :content (->> (drop 1 parts) (str/join " ") str/trim)}
      (if (> (count parts) 3)
        {:user (subs (-> (parts 0) (str/split #"!") first) 1)
         :type (parts 1)
         :content (subs (->> (drop 3 parts) (str/join " ") str/trim) 1)}
        {:type "unknown" :content ""}))))

; send a formatted message through the connection
(defn send-message [message conn]
  (if (.startsWith message "PASS")
    (println "Sending token")
    (println (str "Sending: " message)))
  (s/put! conn (.getBytes (str message "\r\n"))))

; format a Twitch chat message to send to a channel
(defn format-message [message channel]
  (str "PRIVMSG #" channel " :" message "\r\n"))

; test if the user is allowed to run commands
(defn user-allowed? [user]
  (let [user (-> user str/lower-case str/trim)]
    (contains? (set @allowed-users) user)))

; process incoming messages
(defn process-message [message conn channel]
  (println "Received: " (str/trim message))
  (let [{:keys [user type content]} (parse-message message)]
    (cond
      (= type "PING")
      (send-message (str "PONG " content) conn)
      (= type "PRIVMSG")
      (when (re-matches #"!sw .+" content)
        (if (user-allowed? user)
          (let [[_ time] (str/split content #" ")
                seconds (date/parse-time time)]
            (gui/start-stopwatch seconds)
            (send-message (format-message (str "stopwatch started with " (date/format-time seconds "HH:mm:ss")) channel) conn))
          (send-message (format-message (str "you are not allowed to use this command, @" user) channel) conn))))))

; start the Twitch chat bot
(defn start-bot [channel token username]
  (println "Starting bot...")
  (let [conn @(tcp/client {:host "irc.chat.twitch.tv" :port 6667})]
    (println "Connection established...")
    (->> (s/->source conn)
         (s/map bs/to-byte-array)
         (s/map #(String. % "UTF-8"))
         (s/consume #(process-message % conn channel)))
    (send-message (str "PASS oauth:" token) conn)
    (send-message (str "NICK " username) conn)
    (send-message (str "JOIN #" channel) conn)
    (send-message (format-message (str "stopwatch-bot ready") channel) conn)
    conn))

; parse the allowed users string and set it as allowed-users
(defn add-users [users-string]
  (let [user-list (str/split users-string #",")
        processed-list (map #(-> % str/lower-case str/trim) user-list)]
    (reset! allowed-users (vec (doall processed-list)))))

; main function
(defn -main [& _]
  (let [{:keys [channel token username font allowed background-color foreground-color time-format]} config
        conn (start-bot channel token username)]
    (add-users allowed)
    (gui/create-and-show-gui font background-color foreground-color time-format)
    (while (let [user-input (read-line)]
             (when user-input
               (send-message (format-message (str user-input) channel) conn))))))
