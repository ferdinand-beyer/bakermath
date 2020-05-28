(ns doh.db
  (:require [cljs.spec.alpha :as s]))

(s/def :ingredient/id nat-int?)
(s/def :ingredient/name string?)
(s/def :ingredient/flour? boolean?)

(s/def ::ingredient
  (s/keys :req [:ingredient/name]
          :opt [:ingredient/flour?]))

(s/def ::ingredients
  (s/map-of :ingredient/id ::ingredient))

(s/def :part/ingredient-id :ingredient/id)
;; TODO: rename to "weight"
(s/def :part/quantity (s/and number? pos?))

(s/def ::part
  (s/keys :req [:part/ingredient-id
                :part/quantity]))

(s/def :editor/visible? boolean?)
(s/def :editor/mode #{:new :edit})
(s/def :editor/mixture-index nat-int?)
(s/def :editor/part-index nat-int?)

(s/def :mixture/name string?)
(s/def :mixture/parts
  (s/coll-of ::part :kind vector?))

(s/def ::mixture
  (s/keys :req [:mixture/name]
          :opt [:mixture/parts]))

(s/def :recipe/name string?)
(s/def :recipe/mixtures
  (s/coll-of ::mixture :kind vector?))
(s/def :recipe/tab #{:recipe :table :ingredients})

(s/def ::recipe
  (s/keys :req [:recipe/name]
          :opt [:recipe/mixtures :recipe/tab]))

(s/def ::part-editor-default
  (s/keys :req [:editor/visible?
                :editor/mode
                :editor/mixture-index]
          :opt [:ingredient/name
                :part/ingredient-id
                :part/quantity]))

(defmulti part-editor-mode :editor/mode)

(defmethod part-editor-mode :new [_]
  ::part-editor-default)

(defmethod part-editor-mode :edit [_]
  (s/merge ::part-editor-default (s/keys :req [:editor/part-index])))

(s/def ::part-editor (s/multi-spec part-editor-mode :editor/mode))

(s/def ::root
  (s/keys :opt-un [::ingredients
                   ::part-editor]))

(s/def ::db
  (s/merge ::root ::recipe))

(def default-db
  {:ingredients
   {0 {:ingredient/name "Rye flour full grain"
       :ingredient/flour? true}
    1 {:ingredient/name "Rye flour type 1150"
       :ingredient/flour? true}
    2 {:ingredient/name "Water"}
    3 {:ingredient/name "Sourdough starter"}
    4 {:ingredient/name "Wheat flour full grain"
       :ingredient/flour? true}
    5 {:ingredient/name "Wheat flour type 1050"
       :ingredient/flour? true}
    6 {:ingredient/name "Salt"}}
   
   :recipe/name "My Bread"
   :recipe/mixtures
   [{:mixture/name "Sourdough"
     :mixture/parts
     [{:part/ingredient-id 0
       :part/quantity 100}
      {:part/ingredient-id 1
       :part/quantity 50}
      {:part/ingredient-id 2
       :part/quantity 150}
      {:part/ingredient-id 3
       :part/quantity 15}]}
    {:mixture/name "Main dough"
     :mixture/parts
     [{:part/ingredient-id 1
       :part/quantity 200}
      {:part/ingredient-id 4
       :part/quantity 50}
      {:part/ingredient-id 5
       :part/quantity 100}
      {:part/ingredient-id 2
       :part/quantity 175}
      {:part/ingredient-id 6
       :part/quantity 9}]}]})
