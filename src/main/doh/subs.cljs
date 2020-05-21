(ns doh.subs
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

;; TODO: Refactor!
(rf/reg-sub
 ::table
 :<- [::doughs]
 (fn [doughs _]
   {:columns (concat [{:label "Ingredients"}]
                     (map (fn [dough]
                            {:label (:dough/name dough)})
                          doughs)
                     [{:label "Total"}])
    :data
    (let [cells (vec (repeat (count doughs) nil))]
      (->> doughs
           (mapcat (fn [i dough]
                     (->> dough :dough/ingredients
                          (map #(assoc % :dough/index i))))
                   (range))
           (group-by :ingredient/name)
           (map (fn [[name ingredients]]
                  (let [quantities (map (juxt :dough/index :dough-ingredient/quantity)
                                        ingredients)
                        total (reduce + (map second quantities))
                        cells (reduce #(apply assoc %1 %2) cells quantities)]
                    (concat [name] cells [total]))))
           (sort-by first)))}))
