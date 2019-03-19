(ns leihs.my.server-side-js.engine
  (:require
    [me.raynes.conch :refer [programs]]
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [leihs.core.json :refer [to-json]]))

(programs node)

(def render-wait-time-max-ms 5000)

(defn ssr-cli []
  (-> "public/my/leihs-ssr.js" io/resource))

(defn ssr-cli-input-stream []
  (-> "public/my/leihs-ssr.js" io/resource io/input-stream))

; TMP FOR POC. But we might want to do it thusly in dev mode, so it can be watched?
; (programs cat)
; (def tmp-file "/tmp/leihs-ssr.js")
; (with-open [in (ssr-cli-input-stream)]
;   (io/copy in (io/file tmp-file)))
; this should just be the input stream, but just piping it did not work.
; (defn ssr-cli [] (cat tmp-file))

(defn render-react
  [name props]
  (try
    ; (node
    ;   "-" "render" name (to-json props)
    ;   {:in (ssr-cli-input-stream) :timeout render-wait-time-max-ms})

    ; DEBUG: see if source of script is printed
    (programs cat)
    (cat
      "-"
      {:in (ssr-cli-input-stream) :timeout render-wait-time-max-ms})

    (catch Exception e
      (throw (ex-info "Render Error!" {:status 500, :causes {:err e}})))))
