(ns duct-service.boundary.users
  (:require [clojure.java.jdbc :as jdbc]
            [buddy.hashers :as hashers])
  (:import (duct.database.sql Boundary)))

(defprotocol Users
  (create-user [db email password])
  (login-user [db email password])
  )

(extend-protocol Users Boundary
  (create-user [{db :spec} email password]
    (let [pw-hash (hashers/derive password)
          results (jdbc/insert! db :users {:email email :password pw-hash})]
      (-> results ffirst val)))

  (login-user [{db :spec} email password]
    (if-let [user (-> (jdbc/query db ["SELECT * FROM users WHERE email=?" email]) first)]
      (if (hashers/check password (:password user))
        (dissoc user :password)))))

