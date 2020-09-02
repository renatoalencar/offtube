(ns media.server
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [media.service :as service]
            [media.db :as db]
            [media.jobs :as jobs]))

(defonce runnable-service (server/create-server service/service))

(defn run-dev
  [& args]
  (println "\nCreating your [DEV] server...")

  (jobs/start)

  (-> service/service
      (merge {:env :dev
              ::server/join? false
              ::server/routes #(route/expand-routes (deref #'service/routes))
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
              ::server/secure-headers {:content-security-policy-settings {:object-src "'none'"}}})

      server/default-interceptors
      server/dev-interceptors
      server/create-server
      server/start))

(defn -main
  [& args]
  (println "\nCreating your server...")

  (jobs/start)
  (server/start runnable-service))
