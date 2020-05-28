(ns doh.subs
  (:require [re-frame.core :as rf]))

(defn avatar-color [name]
  (str "hsl(" (-> name hash (mod 360)) ", 70%, 60%"))

;;;; Extractors

;; The active recipe.
(rf/reg-sub
 ::recipe
 (fn [db _] db))

;; All ingredients.
(rf/reg-sub
 ::ingredients
 (fn [db _]
   (:ingredients db)))

;; The global part (mixture ingredient) editor.
(rf/reg-sub
 ::part-editor
 (fn [db _]
   (:part-editor db)))

;;;; Ingredients

;; Options to select an ingredient.
(rf/reg-sub
 ::ingredient-options
 :<- [::ingredients]
 (fn [ingredients _]
   (->> (for [[k v] ingredients]
          (assoc v :ingredient/id k))
        (sort-by :ingredient/name)
        vec)))

;; Details for an ingredient.
(rf/reg-sub
 ::ingredient
 :<- [::ingredients]
 (fn [ingredients [_ id]]
   (let [ingredient (get ingredients id)
         color (avatar-color (:ingredient/name ingredient))]
     (assoc ingredient :avatar/color color))))

;;;; Mixtures

;; All mixtures of the current recipe.
(rf/reg-sub
 ::mixtures
 :<- [::recipe]
 (fn [recipe _]
   (->> (:recipe/mixtures recipe)
        (mapv #(assoc %2 :mixture/index %1) (range)))))

;; The names for active mixtures.
(rf/reg-sub
 ::mixture-names
 :<- [::mixtures]
 (fn [mixtures _]
   (mapv #(select-keys % [:mixture/index, :mixture/name])
         mixtures)))

;; Parts of a given mixture.
(rf/reg-sub
 ::parts
 :<- [::mixtures]
 (fn [mixtures [_ index]]
   (get-in mixtures [index :mixture/parts])))

;;;; Analyses

;; Ingredients used by the recipe.
(rf/reg-sub
 ::recipe-ingredient-ids
 :<- [::mixtures]
 (fn [mixtures _]
   (into #{}
         (comp (mapcat :mixture/parts)
               (map :part/ingredient-id))
         mixtures)))

(rf/reg-sub
 ::recipe-ingredients
 :<- [::recipe-ingredient-ids]
 :<- [::ingredients]
 (fn [[ids ingredients] _]
   (->> (map ingredients ids)
        (sort-by :ingredient/name)
        vec)))

;; Total flour weight.
(rf/reg-sub
 ::flour-weight
 :<- [::mixtures]
 :<- [::ingredients]
 (fn [[mixtures ingredients] _]
   (transduce (comp (mapcat :mixture/parts)
                    (map #(merge % (get ingredients (:part/ingredient-id %))))
                    (filter :ingredient/flour?)
                    (map :part/quantity))
              + mixtures)))

(rf/reg-sub
 ::ingredient-part-index
 (fn [[_ _ mixture-index] _]
   (rf/subscribe [::parts mixture-index]))
 (fn [parts [_ ingredient-id _]]
   (->> parts
        (map-indexed #(assoc %2 :part/index %1))
        (filter #(= ingredient-id (:part/ingredient-id %)))
        (map :part/index)
        first)))

;; Ingredient weight by mixture
(rf/reg-sub
 ::ingredient-weight
 (fn [[_ _ mixture-index] _]
   (rf/subscribe [::parts mixture-index]))
 (fn [parts [_ ingredient-id _]]
   (or (->> parts
            (filter #(= ingredient-id (:part/ingredient-id %)))
            (map :part/quantity)
            first)
       0)))

(rf/reg-sub
 ::ingredient-total
 :<- [::mixtures]
 (fn [mixtures [_ ingredient-id]]
   (transduce (comp (mapcat :mixture/parts)
                    (filter #(= ingredient-id (:part/ingredient-id %)))
                    (map :part/quantity))
              + mixtures)))

(rf/reg-sub
 ::ingredient-percentage
 (fn [[_ ingredient-id]]
   [(rf/subscribe [::ingredient-total ingredient-id])
    (rf/subscribe [::flour-weight])])
 (fn [[total flour-weight] _]
   (if (pos? flour-weight)
     (* 100 (/ total flour-weight))
     0)))

