(ns media.db
  (:require [toucan.db :as db]))

(def db-host (or (System/getenv "DB_HOST") "localhost"))

(def db-port (or (System/getenv "DB_PORT") "5432"))

(def db-name (or (System/getenv "DB_NAME") "media"))

(def db-user (or (System/getenv "DB_USER") "media"))

(def db-password (or (System/getenv "DB_PASSWORD") "media"))

(db/set-default-db-connection!
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname (format "//%s:%s/%s" db-host db-port db-name)
   :user db-user
   :password db-password})

