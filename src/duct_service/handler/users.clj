(ns duct-service.handler.users
  (:require
    duct.database.sql
    [ataraxy.response :as response]
    [integrant.core :as ig]
    [buddy.sign.jwt :as jwt]
    [duct-service.boundary.users :as users]))

(defmethod ig/init-key ::create [_ {:keys [db]}]
  (fn [{[_ email password] :ataraxy/result}]
    (let [id (users/create-user db email password)]
      [::response/created (str "/users/" id)])))

(defn with-token [user jwt-secret]
  (->> (jwt/sign {:email (:email user)} jwt-secret)
       (assoc user :token)))

(defmethod ig/init-key ::login [_ {:keys [db jwt-secret]}]
  (fn [{[_ email password] :ataraxy/result}]
    (if-let [user (users/login-user db email password)]
      [::response/ok {:user (with-token user jwt-secret)}]
      {:status 403 :headers {} :body "Not authorized"})))
