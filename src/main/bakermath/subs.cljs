(ns bakermath.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::recipe
 (fn [db _] db))

(rf/reg-sub
 ::doughs
 (fn [db _]
   (:recipe/doughs db)))

(rf/reg-sub
 ::dough-ingredient-editor
 (fn [db _]
   (:dough-ingredient-editor db)))

(rf/reg-sub
 ::dough-ingredients
 :<- [::doughs]
 (fn [doughs [_ index]]
   (get-in doughs [index :dough/ingredients])))
