(ns doh.db-test
  (:require [cljs.test :refer-macros [deftest is]]
            [doh.db :as db]
            [doh.test-util :refer [valid-db?]]))

(deftest empty-db-is-valid
  (is (valid-db? db/empty-db)))
