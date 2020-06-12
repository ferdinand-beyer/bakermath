(ns doh.test-util
  (:require-macros [doh.test-util])
  (:require [doh.db :refer [default-db]]))

;; Used in the 'is' macro, declare to silence editors.
(declare valid-db?)

(def test-db
  "Database for tests."
  default-db)
