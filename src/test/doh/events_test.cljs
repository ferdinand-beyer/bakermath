(ns doh.events-test
  (:require [cljs.test :refer-macros [deftest is]]
            [doh.events :as e]
            [doh.test-util :as util :refer [valid-db?]]))

(deftest init-db-conforms-spec
  (let [db (e/init-db nil nil)]
    (is (valid-db? db))))
