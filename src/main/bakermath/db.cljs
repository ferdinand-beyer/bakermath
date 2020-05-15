(ns bakermath.db
  (:require [cljs.spec.alpha :as s]))

(s/def :ingredient/name string?)

(s/def :dough-ingredient/quantity pos?)

(s/def :editor/visible boolean?)
(s/def :editor/mode #{:new :edit})
(s/def :editor/dough-ref nat-int?)
(s/def :editor/ingredient-ref nat-int?)

(s/def :dough/name string?)
(s/def :dough/ingredients
  (s/coll-of ::dough-ingredient :kind vector?))

(s/def :recipe/name string?)
(s/def :recipe/doughs
  (s/coll-of ::dough :kind vector?))
(s/def :recipe/tab #{:recipe :table :ingredients})

(s/def ::dough-ingredient
  (s/keys :req [:ingredient/name
                :dough-ingredient/quantity]))

(s/def ::dough
  (s/keys :req [:dough/name]
          :opt [:dough/ingredients]))

(s/def ::recipe
  (s/keys :req [:recipe/name]
          :opt [:recipe/doughs :recipe/tab]))

(defmulti editor-mode :editor/mode)

(defmethod editor-mode :new [_]
  (s/keys :req [:editor/visible
                :editor/mode
                :editor/dough-ref]
          :opt [:ingredient/name
                :dough-ingredient/quantity]))

(defmethod editor-mode :edit [_]
  (s/keys :req [:editor/visible
                :editor/mode
                :editor/dough-ref
                :editor/ingredient-ref]
          :opt [:ingredient/name
                :dough-ingredient/quantity]))

(s/def ::dough-ingredient-editor (s/multi-spec editor-mode :editor/mode))

(s/def ::root
  (s/keys :opt-un [::dough-ingredient-editor]))

(s/def ::db
  (s/merge ::root ::recipe))

(def default-db
  {:recipe/name "My Bread"
   :recipe/doughs
   [{:dough/name "Sourdough"
     :dough/ingredients
     [{:ingredient/name "Rye flour full grain"
       :dough-ingredient/quantity 100}
      {:ingredient/name "Rye flour type 1150"
       :dough-ingredient/quantity 50}
      {:ingredient/name "Water"
       :dough-ingredient/quantity 150}
      {:ingredient/name "Sourdough starter"
       :dough-ingredient/quantity 15}]}
    {:dough/name "Main dough"
     :dough/ingredients
     [{:ingredient/name "Rye flour type 1150"
       :dough-ingredient/quantity 200}
      {:ingredient/name "Wheat flour full grain"
       :dough-ingredient/quantity 50}
      {:ingredient/name "Wheat flour type 1050"
       :dough-ingredient/quantity 100}
      {:ingredient/name "Water"
       :dough-ingredient/quantity 175}
      {:ingredient/name "Salt"
       :dough-ingredient/quantity 9}]}]})
