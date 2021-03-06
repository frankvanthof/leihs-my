(ns leihs.my.language
  (:require
    [leihs.core.locale :refer [set-language-cookie]]
    [leihs.my.paths :refer [path]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [clojure.tools.logging :as log]
    [ring.util.response :refer [redirect]]))

(defn redirect-back-with-language-cookie
  [{tx :tx
    {lang-id :language_id} :form-params
    {referer :referer} :headers
    :as request}]
  (-> (redirect referer)
      (set-language-cookie (jdbc/get-by-id tx :languages lang-id))))

(def routes
  (cpj/routes
    (cpj/POST (path :language) [] #'redirect-back-with-language-cookie)))
