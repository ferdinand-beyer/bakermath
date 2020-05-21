(ns doh.events-test
  (:require-macros [doh.test-macros])
  (:require [cljs.spec.alpha :as s]
            [cljs.test :refer-macros [deftest is]]
            [doh.db :as db]
            [doh.events :as e]))

(deftest init-db-conforms-spec
  (let [db (e/init-db nil nil)]
    (is (s/valid? ::db/db db))))
