(ns doh.part-weight-editor-test
  (:require [cljs.test :refer-macros [deftest is]]
            [doh.part-weight-editor :as ed]
            [doh.test-util :refer [test-db valid-db?]]))

(def part-ident {:mixture-index 1, :part-index 3})
(def part-db-path [:recipe/mixtures 1 :mixture/parts 3])

(defn- get-part [db]
  (ed/get-part db [::ed/part part-ident]))

(defn- get-part-weight [db]
  (get-in db (conj part-db-path :part/quantity)))

(deftest test-initial-state
  (is (not (ed/editing? (get-part test-db) nil))))

(deftest test-start-editing
  (let [db (ed/start-edit test-db [nil part-ident])]
    (is (valid-db? db))
    (is (ed/editing? (get-part db) nil))))

(deftest test-enter-weight
  (let [start-db (-> test-db (ed/start-edit [nil part-ident]))
        enter-weight #(ed/enter-weight start-db [nil part-ident %])]
    (let [db (enter-weight "123.45")]
      (is (valid-db? db))
      (is (= "123.45" (ed/get-entered-weight (get-part db) nil)))
      (is (= 123.45 (get-part-weight db))))
    (let [db (enter-weight "")]
      (is (valid-db? db))
      (is (= "" (ed/get-entered-weight (get-part db) nil)))
      (is (= (get-part-weight test-db) (get-part-weight db))))
    (let [db (enter-weight "0")]
      (is (= (get-part-weight test-db) (get-part-weight db))))))

(deftest test-cancel-edit
  (let [db (-> test-db (ed/cancel-edit [nil part-ident]))]
    (is (= db test-db)))
  (let [db (-> test-db (ed/start-edit [nil part-ident]) (ed/cancel-edit [nil part-ident]))]
    (is (= db test-db)))
  (let [db (-> test-db
               (ed/start-edit [nil part-ident])
               (ed/enter-weight [nil part-ident "123"])
               (ed/cancel-edit [nil part-ident]))]
    (is (= test-db db))))

(deftest test-save-edit
  (let [db (-> test-db
               (ed/start-edit [nil part-ident])
               (ed/enter-weight [nil part-ident "123"])
               (ed/save-edit [nil part-ident]))
        expected-db (assoc-in test-db (conj part-db-path :part/quantity) 123)]
    (is (= expected-db db))))
