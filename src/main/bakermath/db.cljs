(ns bakermath.db
  (:require [cljs.spec.alpha :as s]))

(s/def :ingredient/name string?)

(s/def :dough-ingredient/quantity pos?)

(s/def :editor/visible boolean?)
(s/def :editor/mode #{:new :edit})
(s/def :editor/dough-ref nat-int?)

(s/def :dough/name string?)
(s/def :dough/ingredients
  (s/coll-of ::dough-ingredient :kind vector?))

(s/def :recipe/name string?)
(s/def :recipe/doughs
  (s/coll-of ::dough :kind vector?))

(s/def ::dough-ingredient
  (s/keys :req [:ingredient/name
                :dough-ingredient/quantity]))

(s/def ::dough
  (s/keys :req [:dough/name]
          :opt [:dough/ingredients]))

(s/def ::recipe
  (s/keys :req [:recipe/name]
          :opt [:recipe/doughs]))

(s/def ::dough-ingredient-editor
  (s/keys :req [:editor/visible
                :editor/mode
                :editor/dough-ref]
          :opt [:ingredient/name
                :dough-ingredient/quantity]))

(s/def ::root
  (s/keys :opt-un [::dough-ingredient-editor]))

(s/def ::db
  (s/merge ::root ::recipe))

(def default-db
  {:recipe/name "Bread"
   :recipe/doughs
   [{:dough/name "Sour dough"}
    {:dough/name "Main dough"}]})
