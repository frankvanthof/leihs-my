(ns leihs.my.back.html
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require [leihs.core.json :refer [to-json]]
            [leihs.core.http-cache-buster2 :as cache-buster]
             [leihs.my.back.shared :refer [head]]
            [leihs.my.utils.release-info :as release-info]
            [leihs.my.back.shared :refer [head]]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.core.url.core :as url]
            [leihs.my.authorization :as auth]
            [leihs.my.back.ssr :as ssr]
            [leihs.my.server-side-js.engine :as js-engine]
            [leihs.core.anti-csrf.back :refer [anti-csrf-token]]
            [leihs.core.user.permissions :refer
             [borrow-access? managed-inventory-pools]]
            [leihs.core.user.permissions.procure :as procure]
            [leihs.my.paths :refer [path]]
            [clojure.java.jdbc :as jdbc]
            [hiccup.page :refer [include-js html5]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as logging]
            [logbug.catcher :as catcher]
            [logbug.debug :as debug :refer [I>]]
            [logbug.ring :refer [wrap-handler-with-logging]]
            [logbug.thrown :as thrown]))

(defn route-user [request]
  (let [user-id (-> request :route-params :user-id)
        tx (:tx request)]
    (-> (sql/select :*)
        (sql/from :users)
        (sql/where [:= :id user-id])
        sql/format
        (->> (jdbc/query tx))
        first)))

(defn user-attribute [request]
  (let [user (if (auth/me? request)
               (:authenticated-entity request)
               (route-user request))]
    (-> user to-json url/encode)))

(defn body-attributes
  [request]
  {:data-user (user-attribute request),
   :data-leihs-my-version (url/encode (to-json release-info/leihs-my-version)),
   :data-leihs-version (url/encode (to-json release-info/leihs-version))})

(defn not-found-handler
  [request]
  {:status 404,
   :headers {"Content-Type" "text/html"},
   :body (html5 (head)
                [:body (body-attributes request)
                 [:div.container-fluid
                  [:h1.text-danger "Error 404 - Not Found"]]])})



(defn html-handler
  [request]
  {:headers {"Content-Type" "text/html"},
   :body (html5 (head)
                [:body (body-attributes request)
                 [:div (ssr/render-navbar request)
                  [:div#app.container-fluid
                   [:div.alert.alert-warning [:h1 "Leihs My"]
                    [:p "This application requires Javascript."]]]]
                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                           "/my/leihs-shared-bundle.js"))
                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                           "/my/js/app.js"))])})


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
