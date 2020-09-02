(ns media.jobs
  (:require [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as mq]
            [media.import :as import]))

(defn import-handler
  [{:keys [filename location]}]
    (import/import filename location))

(defn download-handler
  [{:keys [uri]}]
  (import/youtube uri))

(def connection {:pool {}
                 :spec {:host "localhost"} })

(defmacro wcar*
  [& body]
  `(car/wcar connection ~@body))

(defn process [filename location]
  (wcar* (mq/enqueue "video-processing" {:filename filename
                                         :location location})))

(defn download [uri]
  (wcar* (mq/enqueue "youtube-download" {:uri uri})))

(defn start
  []
  (mq/worker connection "video-processing"
    {:handler (fn [{:keys [message attempt]}]
                (import-handler message)

                {:status :success})})

  (mq/worker connection "youtube-download"
    {:handler (fn [{:keys [message attempt]}]
                (download-handler message)

                {:status :success})}))
