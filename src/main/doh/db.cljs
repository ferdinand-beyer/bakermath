(ns doh.db
  (:require [cljs.spec.alpha :as s]))

(s/def :ingredient/name string?)

(s/def :part/quantity pos?)

(s/def :editor/visible boolean?)
(s/def :editor/mode #{:new :edit})
(s/def :editor/mixture-index nat-int?)
(s/def :editor/part-index nat-int?)

(s/def :mixture/name string?)
(s/def :mixture/parts
  (s/coll-of ::part :kind vector?))

(s/def :recipe/name string?)
(s/def :recipe/mixtures
  (s/coll-of ::mixture :kind vector?))
(s/def :recipe/tab #{:recipe :table :ingredients})

(s/def ::part
  (s/keys :req [:ingredient/name
                :part/quantity]))

(s/def ::mixture
  (s/keys :req [:mixture/name]
          :opt [:mixture/parts]))

(s/def ::recipe
  (s/keys :req [:recipe/name]
          :opt [:recipe/mixtures :recipe/tab]))

(defmulti editor-mode :editor/mode)

(defmethod editor-mode :new [_]
  (s/keys :req [:editor/visible
                :editor/mode
                :editor/mixture-index]
          :opt [:ingredient/name
                :part/quantity]))

(defmethod editor-mode :edit [_]
  (s/keys :req [:editor/visible
                :editor/mode
                :editor/mixture-index
                :editor/part-index]
          :opt [:ingredient/name
                :part/quantity]))

(s/def ::part-editor (s/multi-spec editor-mode :editor/mode))

(s/def ::root
  (s/keys :opt-un [::part-editor]))

(s/def ::db
  (s/merge ::root ::recipe))

(def default-db
  {:recipe/name "My Bread"
   :recipe/mixtures
   [{:mixture/name "Sourdough"
     :mixture/parts
     [{:ingredient/name "Rye flour full grain"
       :part/quantity 100}
      {:ingredient/name "Rye flour type 1150"
       :part/quantity 50}
      {:ingredient/name "Water"
       :part/quantity 150}
      {:ingredient/name "Sourdough starter"
       :part/quantity 15}]}
    {:mixture/name "Main dough"
     :mixture/parts
     [{:ingredient/name "Rye flour type 1150"
       :part/quantity 200}
      {:ingredient/name "Wheat flour full grain"
       :part/quantity 50}
      {:ingredient/name "Wheat flour type 1050"
       :part/quantity 100}
      {:ingredient/name "Water"
       :part/quantity 175}
      {:ingredient/name "Salt"
       :part/quantity 9}]}]})
