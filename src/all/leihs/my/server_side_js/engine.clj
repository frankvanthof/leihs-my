(ns leihs.my.server-side-js.engine
  (:require
    [me.raynes.conch :refer [programs]]
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [leihs.core.json :refer [to-json]]))

(programs node)

(def render-wait-time-max-ms 5000)

(def tmp-file "/tmp/leihs-ui-server-side.js")

(defn server-side-js-input-stream []
  (-> "public/my/leihs-ui-server-side.js"
      io/resource io/input-stream))

(with-open [in (server-side-js-input-stream)]
  (io/copy in (io/file tmp-file)))

;(slurp (server-side-js-input-stream))

(defn js-code
  [name props]
  (str
    "window = global = this
   l = require('" tmp-file "')
   l.renderComponentToString('"
    name
    "', "
    (to-json props)
    ")"))

(defn render-react
  [name props]
  (try
    (node "-p" (js-code name props) {:timeout render-wait-time-max-ms})
    (catch Exception e
      (throw (ex-info "Render Error!" {:status 500, :causes {:err e}})))))
