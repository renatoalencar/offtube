(ns media.views
  (:require [hiccup.core :as hiccup]
            [ring.util.response :as ring-resp]))

(defn layout
  [content]
  [:html
   [:head
    [:title "offtube"]

    [:link {:href "https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600;700;800&display=swap" :rel "stylesheet"}]
    [:link {:rel "stylesheet" :href "/style.css"}]

    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]]
   [:body
    [:header
     [:a {:href "/" :class "logo-link"} [:img {:src "/offtube.svg" :alt "offtube logo"}]]

     [:nav
      [:a {:href "/upload"} "Upload"]
      [:a {:href "/youtube"} "YouTube"]]]
    content]])

(defn html-page
  [content]
  (-> (layout content)
      hiccup/html
      ring-resp/response))

(defn format-duration
  [duration]
  (let [time (int duration)]
    (format "%02d:%02d"
            (quot time 60)
            (mod time 60))))


(def ASSET_PATH (or (System/getenv "ASSET_PATH") "http://localhost:8081/"))

(defn video-item
  [video]
  [:a.video-item {:href (str "/video?id=" (:id video))}
   [:img {:alt "Cover" :src (str ASSET_PATH (:cover video))}]
   [:span.duraton (format-duration (:duration video))]
   [:span.title (:title video)]])

(defn videos
  [{:keys [videos]}]
  (html-page [:main
              [:div (map video-item videos)]]))

(defn video
  [{:keys [video request suggestions]}]
  (html-page
   [:div.video-page
    [:div.video-container
     [:video {:controls true :autoplay true}
      [:source {:src (str ASSET_PATH (:video video)) :type "video/mp4"}]]
     [:div.title (:title video)]]

    [:div.suggested
     (map video-item suggestions)]]))

(defn video-upload-form
  []
  (html-page [:div.upload-form
              [:h2 "Upload video"]
              [:form {:action "/upload" :method "post" :enctype "multipart/form-data"}
               [:input {:type "file" :name "video"}]
               [:button {:type "submit"} "Enviar"]]]))

(defn youtube-form
  []
  (html-page [:div.youtube-form
              [:h2 "Download do YouTube"]
              [:form {:action "/youtube" :method "post"}
               [:input {:type "text" :name "uri" :placeholder "URL"}]
               [:button {:type "submit"} "Enviar"]]]))
