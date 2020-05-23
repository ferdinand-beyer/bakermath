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
   (for [[k v] ingredients]
     (assoc v :ingredient/id k))))

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
   (:recipe/mixtures recipe)))

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

;; TODO: Refactor!
(rf/reg-sub
 ::table
 :<- [::mixtures]
 :<- [::ingredients]
 :<- [::flour-weight]
 (fn [[mixtures ingredients flour-weight] _]
   {:columns (concat [{:label "Ingredients"}]
                     (for [mixture mixtures]
                       {:label (:mixture/name mixture)})
                     [{:label "Total"}
                      {:label "Percentage"}])
    :data
    (let [cells (vec (repeat (count mixtures) nil))]
      (->> mixtures
           (mapcat (fn [i mixture]
                     (map #(assoc % :mixture/index i) (:mixture/parts mixture)))
                   (range))
           (group-by :part/ingredient-id)
           (map (fn [[id parts]]
                  (let [name (get-in ingredients [id :ingredient/name])
                        quantities (map (juxt :mixture/index :part/quantity) parts)
                        total (reduce + (map second quantities))
                        percentage (if (pos? flour-weight)
                                     (* 100 (/ total flour-weight))
                                     0)
                        cells (reduce (fn [cells [index qty]]
                                        (update cells index + qty))
                                      cells quantities)]
                    (concat [name] cells [total (str (.toFixed percentage 2) "%")]))))
           (sort-by first)))}))
