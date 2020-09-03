(ns media.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as ring-mw]
            [ring.util.response :as ring-resp]
            [toucan.db :as db]

            [media.models :refer :all]
            [media.jobs :as jobs]
            [media.views :as views]))


(defn videos [request]
  (views/videos {:videos (Video)}))

(defn video [request]
  (let [video (Video (-> request
                         (get-in [:params :id])
                         Integer/parseInt))

        suggestions (db/select Video {:limit 3 :order "random()"})]

    (views/video {:request request
                  :video video
                  :suggestions suggestions})))

(defn video-upload-form
  [request]
  (views/video-upload-form))

(defn video-upload
  [request]
  (let [{:keys [filename temfile]} (get-in request [:params "video"])]
    (jobs/process filename (.getAbsolutePath tempfile)))

  (ring-resp/redirect "/"))

(defn youtube-form
  [request]
  (views/youtube-form))

(defn youtube-download
  [request]
  (let [url (get-in request [:params "uri"])]
    (jobs/download url))

  (ring-resp/redirect "/"))

(def common-interceptors [(body-params/body-params) (ring-mw/multipart-params) http/html-body ])

(def routes #{["/"        :get  (conj common-interceptors `videos)]
              ["/video"   :get  (conj common-interceptors `video)]
              ["/upload"  :get  (conj common-interceptors `video-upload-form)]
              ["/upload"  :post (conj common-interceptors `video-upload)]

              ["/youtube" :get  (conj common-interceptors `youtube-form)]
              ["/youtube" :post (conj common-interceptors `youtube-download)]})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/host (or (System/getenv "HOST") "0.0.0.0")
              ::http/port 8080})
