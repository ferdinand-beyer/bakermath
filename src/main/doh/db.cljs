(ns doh.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]))

;;;; Spec

(def not-blank? (complement str/blank?))

(s/def ::id nat-int?)
(s/def ::name (and string? not-blank?))

(s/def :ingredient/id ::id)
(s/def :ingredient/name ::name)
(s/def :ingredient/flour? boolean?)

(s/def ::ingredient
  (s/keys :req [:ingredient/id :ingredient/name]
          :opt [:ingredient/flour?]))

(s/def :part/quantity (s/and number? pos?))

(s/def :mixture/id ::id)
(s/def :mixture/name ::name)
(s/def :mixture/parts (s/map-of :ingredient/id :part/quantity))

(s/def ::mixture
  (s/keys :req [:mixture/id :mixture/name]
          :opt [:mixture/parts]))

(s/def :recipe/id ::id)
(s/def :recipe/name ::name)
(s/def :recipe/mixtures (s/coll-of :mixture/id :kind vector?))

(s/def ::recipe
  (s/keys :req [:recipe/id :recipe/name]
          :opt [:recipe/mixtures]))

(s/def :db/last-id ::id)
(s/def :db/ingredients (s/map-of :ingredient/id ::ingredient))
(s/def :db/mixtures (s/map-of :mixture/id ::mixture))
(s/def :db/recipes (s/map-of :recipe/id ::recipe))

(s/def ::data
  (s/keys :req [:db/last-id :db/ingredients :db/mixtures :db/recipes]))

(s/def :field/orig any?)
(s/def :field/input any?)
(s/def :field/value any?)
(s/def :field/error string?)

(s/def ::field (s/keys :opt [:field/orig :field/input :field/value :field/error]))

(s/def :editor/visible? boolean?)
(s/def :editor/mixture-id :mixture/id)
(s/def :editor/ingredient-id :ingredient/id)
(s/def :editor/name ::field)
(s/def :editor/quantity ::field)

(s/def :view/mixture-editor
  (s/keys :req [:editor/visible?]
          :opt [:editor/mixture-id
                :editor/name]))

(s/def :view/part-editor
  (s/keys :req [:editor/visible?
                :editor/mixture-id]
          :opt [:editor/ingredient-id
                :editor/name
                :editor/quantity]))

(s/def :view/quantity-editor
  (s/keys :req [:editor/visible?
                :editor/mixture-id
                :editor/ingredient-id
                :editor/quantity]))

(s/def :view/recipe :recipe/id)
(s/def :view/tab #{:recipe :table})

(s/def ::view
  (s/keys :opt [:view/recipe
                :view/tab
                :view/mixture-editor
                :view/part-editor
                :view/quantity-editor]))

(s/def ::db
  (s/merge ::data ::view))

;;; Populating

(def empty-db #:db{:last-id 0
                   :ingredients {}
                   :mixtures {}
                   :recipes {}})

(defn add
  "Adds a new entity to the database, returning the updated
   database and the ID of the inserted entity."
  [db coll-key id-key attrs]
  (let [id (inc (:db/last-id db))
        entity (assoc attrs id-key id)
        db (-> db
               (assoc :db/last-id id)
               (update-in [coll-key] assoc id entity))]
    [db id]))

(defn add-ingredient [db name flour?]
  (add db :db/ingredients :ingredient/id
       (cond-> {:ingredient/name name}
         flour? (assoc :ingredient/flour? true))))

(defn add-mixture [db name parts]
  (add db :db/mixtures :mixture/id
       {:mixture/name name
        :mixture/parts parts}))

(defn add-recipe [db name mixtures]
  (add db :db/recipes :recipe/id
       {:recipe/name name
        :recipe/mixtures mixtures}))

;;;; Query

(defn find-mixture-by-name
  [db name]
  (->> db :db/mixtures vals
       (filter #(= name (:mixture/name %)))
       first))

(defn find-ingredient-by-name
  [db name]
  (->> db :db/ingredients vals
       (filter #(= name (:ingredient/name %)))
       first))

(defn find-mixtures-by-ingredient-id
  [db ingredient-id]
  (->> db :db/mixtures vals
       (filter #(contains? (:mixture/parts %) ingredient-id))))

;;;; Default database

(def default-db
  (let [db empty-db
        [db water] (add-ingredient db "Water" false)
        [db salt] (add-ingredient db "Salt" false)
        [db starter] (add-ingredient db "Starter" false)
        [db rye-fg] (add-ingredient db "Rye flour full grain" true)
        [db rye-1150] (add-ingredient db "Rye flour type 1150" true)
        [db wheat-fg] (add-ingredient db "Wheat flour full grain" true)
        [db wheat-1050] (add-ingredient db "Wheat flour type 1050" true)
        [db sourdough] (add-mixture db "Sourdough"
                                    {rye-fg 100
                                     rye-1150 50
                                     water 150
                                     starter 15})
        [db maindough] (add-mixture db "Main dough"
                                    {rye-1150 200
                                     wheat-1050 100
                                     wheat-fg 50
                                     water 175
                                     salt 9})
        [db _] (add-recipe db "My Bread" [sourdough maindough])]
    db))
