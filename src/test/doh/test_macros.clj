(ns doh.events-test
  (:require [cljs.spec.alpha :as s]
            [cljs.test :as t]))

(defmethod t/assert-expr 's/valid?
  [_ msg [_ spec val]]
  `(let [ed# (s/explain-data ~spec ~val)]
     (if (nil? ed#)
       (t/do-report {:type :pass, :message ~msg})
       (t/do-report
        {:type :fail
         :message (with-out-str (s/explain-out ed#))
         :expected '(s/valid? ~spec ~val)
         :actual ed#}))
     ed#))
