(ns stopwatch-bot.core
  (:require [stopwatch-bot.gui :as gui]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [aleph.tcp :as tcp]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]))

;; allowed users
(def allowed-users (atom []))

;; parse raw Twitch chat message into user, type, and content
(defn parse-message [message]
  (let [parts (str/split message #" ")]
    (if (= (first parts) "PING")
      {:type (first parts) :content (->> (drop 1 parts) (str/join " ") str/trim)}
      {:user (subs (-> (parts 0) (str/split #"!") first) 1)
       :type (parts 1)
       :content (subs (->> (drop 3 parts) (str/join " ") str/trim) 1)})))

;; send a formatted message through the connection
(defn send-message [message conn]
  (if (.startsWith message "PASS")
    (println "Sending token")
    (println (str "Sending: " message)))
  (s/put! conn (.getBytes (str message "\r\n"))))

;; format a Twitch chat message to send to a channel
(defn format-message [message channel]
  (str "PRIVMSG #" channel " :" message "\r\n"))

;; test if the user is allowed to run commands
(defn user-allowed? [user]
  (let [user (-> user str/lower-case str/trim)]
    (contains? (set @allowed-users) user)))

;; handle incoming messages based on their type and content
(defn handle-message [message conn channel]
  (let [{:keys [user type content]} (parse-message message)]
    ; check here for user, if needed
    (cond
      (= type "PING") ; check for PING first, as this is a server command and doesn't come from a user
      (send-message (str "PONG " content) conn)
      (= type "PRIVMSG")
      (when (re-matches #"!sw \d+" content)
        (if (user-allowed? user)
          (let [[_ time] (str/split content #" ")]
            (gui/start-stopwatch time)
            (send-message (format-message (str "stopwatch started with " time "s") channel) conn))
          (send-message (format-message (str "you are not allowed to use this command, @" user) channel) conn))))))

;; process incoming messages
(defn process-message [message conn channel]
  (try
    (print "Received: " message)
    (handle-message message conn channel)
    (catch Exception e
      (.printStackTrace e))))

;; start the Twitch chat bot
(defn start-bot [channel token username]
  (println "Starting bot...")
  (let [conn @(tcp/client {:host "irc.chat.twitch.tv" :port 6667})]
    (println "Connection established...")
    (send-message (str "PASS " token) conn)
    (send-message (str "NICK " username) conn)
    (send-message (str "JOIN #" channel) conn)
    (send-message (format-message (str "stopwatch-bot ready") channel) conn)
    (d/chain
     (s/consume (fn [data] (process-message data conn channel))
                (s/map (fn [buf] (String. buf "UTF-8")) (s/->source conn))))
    conn))

(defn add-users [users-string]
  (let [user-list (str/split users-string #",")
        processed-list (map #(-> % str/lower-case str/trim) user-list)]
    (reset! allowed-users (vec (doall processed-list)))))

;; define command line options
(def cli-options
  [["-c" "--channel CHANNEL" "Channel name"]
   ["-t" "--token TOKEN" "OAuth token"]
   ["-u" "--username USERNAME" "Username"]
   ["-f" "--font FONT" "Font name"]
   ["-a" "--allowed ALLOWED" "Allowed users"]
   ["-h" "--help"]])

;; main function
(defn -main
  [& args]
  (let [{:keys [options banner]} (parse-opts args cli-options)
        {:keys [channel token username font allowed]} options
        conn (start-bot channel token username)]
    (add-users allowed)
    (when (:help options)
      (println banner)
      (System/exit 0))
    (gui/create-and-show-gui font)
    (while (let [user-input (read-line)]
             (when user-input
               (send-message (format-message (str user-input) channel) conn))))))
