(ns doh.test-util
  (:require [cljs.spec.alpha :as s]
            [cljs.test :as t]))

(defn assert-spec
  "Validates a value against a spec in a test."
  [msg form spec val]
  `(let [ed# (s/explain-data ~spec ~val)]
     (if (nil? ed#)
       (t/do-report {:type :pass, :message ~msg})
       (t/do-report
        {:type :fail
         :message (with-out-str
                    (when-let [m# ~msg] (println m#))
                    (println "Value failed spec:" ~spec)
                    (s/explain-out ed#))
         :expected '~form
         :actual ~val}))
     ed#))

(defmethod t/assert-expr 'valid?
  [_ msg [_ spec val :as form]]
  (assert-spec msg form spec val))

(defmethod t/assert-expr 'valid-db?
  [_ msg [_ val :as form]]
  (assert-spec msg form :doh.db/db val))
