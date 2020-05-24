(ns doh.subs
  (:require [re-frame.core :as rf]))

(defn avatar-color [name]
  (str "hsl(" (-> name hash (mod 360)) ", 70%, 60%"))

(rf/reg-sub
 ::recipe
 (fn [db _] db))

(rf/reg-sub
 ::ingredients
 (fn [db _]
   (:ingredients db)))

(rf/reg-sub
 ::part-editor
 (fn [db _]
   (:part-editor db)))

(rf/reg-sub
 ::ingredient-options
 :<- [::ingredients]
 (fn [ingredients _]
   (->> (for [[k v] ingredients]
          (assoc v :ingredient/id k))
        (sort-by :ingredient/name)
        vec)))

(rf/reg-sub
 ::ingredient
 :<- [::ingredients]
 (fn [ingredients [_ id]]
   (let [ingredient (get ingredients id)
         color (avatar-color (:ingredient/name ingredient))]
     (assoc ingredient :avatar/color color))))

(rf/reg-sub
 ::mixtures
 :<- [::recipe]
 (fn [recipe _]
   (->> (:recipe/mixtures recipe)
        (mapv #(assoc %2 :mixture/index %1) (range)))))

(rf/reg-sub
 ::mixture-names
 :<- [::mixtures]
 (fn [mixtures _]
   (mapv #(select-keys % [:mixture/index, :mixture/name])
         mixtures)))

(rf/reg-sub
 ::parts
 :<- [::mixtures]
 (fn [mixtures [_ index]]
   (get-in mixtures [index :mixture/parts])))

(rf/reg-sub
 ::flour-weight
 :<- [::mixtures]
 :<- [::ingredients]
 (fn [[mixtures ingredients] _]
   (transduce (comp (mapcat :mixture/parts)
                    (map #(merge % (get ingredients (:part/ingredient-id %))))
                    (filter :ingredient/flour-proportion)
                    (map (juxt :ingredient/flour-proportion
                               :part/quantity))
                    (map #(apply * %)))
              + mixtures)))

(rf/reg-sub
 ::ingredient-weights
 :<- [::mixtures]
 :<- [::ingredients]
 :<- [::flour-weight]
 (fn [[mixtures ingredients flour-weight] _]
   (->> mixtures
        (mapcat (fn [mixture]
                  (let [index (:mixture/index mixture)]
                    (map-indexed (fn [i part]
                                   (merge part {:mixture/index index
                                                :part/index i}))
                                 (:mixture/parts mixture)))))
        (group-by :part/ingredient-id)
        (map (fn [[id parts]]
               (let [weights (into {} (map (juxt :mixture/index :part/quantity)) parts)
                     total (reduce + (vals weights))
                     percentage (if (pos? flour-weight)
                                  (* 100 (/ total flour-weight))
                                  0)]
                 {:id id
                  :name (get-in ingredients [id :ingredient/name])
                  :flour-proportion (get-in ingredients [id :ingredient/flour-proportion])
                  :parts (into {} (map (juxt :mixture/index identity) parts))
                  :weights weights
                  :total total
                  :percentage percentage})))
        (sort-by :name)
        vec)))
