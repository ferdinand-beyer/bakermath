(ns doh.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::recipe
 (fn [db _] db))

(rf/reg-sub
 ::mixtures
 (fn [db _]
   (:recipe/mixtures db)))

(rf/reg-sub
 ::part-editor
 (fn [db _]
   (:part-editor db)))

(rf/reg-sub
 ::parts
 :<- [::mixtures]
 (fn [mixtures [_ index]]
   (get-in mixtures [index :mixture/parts])))

;; TODO: Refactor!
(rf/reg-sub
 ::table
 :<- [::mixtures]
 (fn [mixtures _]
   {:columns (concat [{:label "Ingredients"}]
                     (for [mixture mixtures]
                       {:label (:mixture/name mixture)})
                     [{:label "Total"}])
    :data
    (let [cells (vec (repeat (count mixtures) nil))]
      (->> mixtures
           (mapcat (fn [i mixture]
                     (map #(assoc % :mixture/index i) (:mixture/parts mixture)))
                   (range))
           (group-by :ingredient/name)
           (map (fn [[name parts]]
                  (let [quantities (map (juxt :mixture/index :part/quantity) parts)
                        total (reduce + (map second quantities))
                        cells (reduce #(apply assoc %1 %2) cells quantities)]
                    (concat [name] cells [total]))))
           (sort-by first)))}))
