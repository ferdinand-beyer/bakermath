(ns doh.subs
  (:require [re-frame.core :as rf]))

(defn avatar-color [name]
  (str "hsl(" (-> name hash (mod 360)) ", 70%, 60%"))

;;;; Extractors

;; All ingredients.
(rf/reg-sub
 ::ingredients
 (fn [db _]
   (:db/ingredients db)))

;; All mixtures
(rf/reg-sub
 ::mixtures
 (fn [db _]
   (:db/mixtures db)))

;; All recipes.
(rf/reg-sub
 ::recipes
 (fn [db _]
   (:db/recipes db)))

;; The active recipe.
(rf/reg-sub
 ::current-recipe-id
 (fn [db _]
   (:view/recipe db)))

;; Selected tab in the recipe view.
(rf/reg-sub
 ::recipe-tab
 (fn [db _]
   (or (:view/tab db) :recipe)))

;;;; Editors

(rf/reg-sub
 ::part-editor
 (fn [db _]
   (:view/part-editor db)))

(rf/reg-sub
 ::quantity-editor
 (fn [db _]
   (:view/quantity-editor db)))

;;;; Ingredients

;; Options to select an ingredient.
(rf/reg-sub
 ::ingredient-options
 :<- [::ingredients]
 (fn [ingredients _]
   (->> (vals ingredients)
        (sort-by :ingredient/name)
        vec)))

;; Details for an ingredient.
(rf/reg-sub
 ::ingredient
 :<- [::ingredients]
 (fn [ingredients [_ id]]
   (get ingredients id)))

(rf/reg-sub
 ::ingredient-avatar-color
 (fn [[_ id] _]
   (rf/subscribe [::ingredient id]))
 (fn [ingredient _]
   (avatar-color (:ingredient/name ingredient))))

;; Ingredients that are flour.
(rf/reg-sub
 ::flour-ingredient-ids
 :<- [::ingredients]
 (fn [ingredients _]
   (into #{}
         (comp (filter :ingredient/flour?)
               (map :ingredient/id))
         (vals ingredients))))

;;;; Mixtures

;; A mixture by ID.
(rf/reg-sub
 ::mixture
 :<- [::mixtures]
 (fn [mixtures [_ id]]
   (get mixtures id)))

;; Ingredient IDs in a mixture.
(rf/reg-sub
 ::mixture-ingredient-ids
 (fn [[_ id] _]
   (rf/subscribe [::mixture id]))
 (fn [mixture _]
   (keys (:mixture/parts mixture))))

;; Quantity of a mixture ingredient.
(rf/reg-sub
 ::part-quantity
 (fn [[_ mixture-id _] _]
   (rf/subscribe [::mixture mixture-id]))
 (fn [mixture [_ _ ingredient-id]]
   (get (:mixture/parts mixture) ingredient-id)))

;;;; Recipes

;; The current recipe.
(rf/reg-sub
 ::recipe
 :<- [::recipes]
 :<- [::current-recipe-id]
 (fn [[recipes id] _]
   (get recipes id)))

;; Mixture IDs of the current recipe.
(rf/reg-sub
 ::recipe-mixture-ids
 :<- [::recipe]
 (fn [recipe _]
   (:recipe/mixtures recipe)))

;; All mixtures of the current recipe.
(rf/reg-sub
 ::recipe-mixtures
 :<- [::recipe]
 :<- [::mixtures]
 (fn [[recipe mixtures] _]
   (->> (:recipe/mixtures recipe)
        (mapv #(get mixtures %)))))

;; Ingredients used by the recipe.
(rf/reg-sub
 ::recipe-ingredient-ids
 :<- [::recipe-mixtures]
 (fn [mixtures _]
   (into #{}
         (comp (mapcat :mixture/parts)
               (map key))
         mixtures)))

(rf/reg-sub
 ::recipe-ingredients
 :<- [::recipe-ingredient-ids]
 :<- [::ingredients]
 (fn [[ids ingredients] _]
   (->> (map ingredients ids)
        (sort-by :ingredient/name)
        vec)))

;;;; Analyses

(rf/reg-sub
 ::flour-total
 :<- [::recipe-mixtures]
 :<- [::flour-ingredient-ids]
 (fn [[mixtures flour-ids] _]
   (transduce (comp (mapcat :mixture/parts)
                    (filter #(contains? flour-ids (key %)))
                    (map val))
              + mixtures)))

(rf/reg-sub
 ::ingredient-total
 :<- [::recipe-mixtures]
 (fn [mixtures [_ ingredient-id]]
   (transduce (keep #(get-in % [:mixture/parts ingredient-id]))
              + mixtures)))

(rf/reg-sub
 ::ingredient-percentage
 (fn [[_ ingredient-id]]
   [(rf/subscribe [::ingredient-total ingredient-id])
    (rf/subscribe [::flour-total])])
 (fn [[ingredient flour] _]
   (if (pos? flour)
     (* 100 (/ ingredient flour))
     0)))
