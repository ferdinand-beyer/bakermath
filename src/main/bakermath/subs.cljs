(ns bakermath.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :root/recipe
 (fn [db _]
   (:root/recipe db)))

(rf/reg-sub
 :recipe/id
 (fn [db _]
   (:recipe/id db)))

(rf/reg-sub
 :dough/id
 (fn [db _]
   (:dough/id db)))

(rf/reg-sub
 ::recipe
 :<- [:root/recipe]
 :<- [:recipe/id]
 (fn [[recipe-id recipes] _]
   (get recipes recipe-id)))

(rf/reg-sub
 ::doughs
 :<- [::recipe]
 :<- [:dough/id]
 (fn [[recipe doughs] _]
   (mapv doughs (:recipe/doughs recipe))))
