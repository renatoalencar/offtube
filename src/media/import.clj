(ns media.import
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as str]
            [cheshire.core :as json]
            [toucan.db :as db]

            [media.db]
            [media.models :refer :all]))

(def media-path
  (or (System/getenv "MEDIA_PATH") "./assets"))

(def config
  {:video-path (str media-path "/videos")
   :thumbnail-path (str media-path "/img")})

(defn shasum
  [filename]
  (-> (:out (sh "shasum" filename))
      (subs 0 40)))

(defn ext
  [filename]
  (subs filename (- (count filename) 4)))

(defn import
  ([filename uri]
   (let [hash (shasum uri)

         title (last (str/split (subs filename 0 (- (count filename) 3)) #"/"))

         duration (Float/parseFloat
                   (-> (:out (sh "ffprobe" "-i" uri "-print_format" "json" "-show_streams"))
                       (json/parse-string true)
                       (get-in [:streams 0 :duration])))]

     (println (sh "cp" uri
                  (str (:video-path config)
                       "/"
                       hash (ext filename))))

     (println (sh "ffmpeg" "-i" uri "-ss" "00:00:30.000" "-vframes" "1"
                  (str (:thumbnail-path config) "/" hash ".png")))

     (db/insert! Video
                 {:title title
                  :duration duration
                  :cover (str "/img/" hash ".png")
                  :video (str "/videos/" hash (ext filename))})))

  ([filename] (import filename filename)))

(defn youtube
  [uri]
  (let [{:keys [title _filename ext]} (-> (:out (sh "youtube-dl" "-f" "mp4"
                                                    "--print-json"
                                                    "-o" "/tmp/%(title)s.%(ext)s" uri))
                                          (json/parse-string true)
                                          (select-keys [:title :_filename :ext]))]
    (import (str title "." ext) _filename)))
    
(defn -main
  [& args]
  (doseq [filename args]
    (import filename)))
