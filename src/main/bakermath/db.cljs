(ns bakermath.db
  (:require [cljs.spec.alpha :as s]))

(s/def ::db (s/keys))

(def default-db
  {:root/recipe 0
   :recipe/id {0 {:recipe/id 0
                  :recipe/name "Bread"
                  :recipe/doughs [0 1]}}
   :dough/id {0 {:dough/id 0
                 :dough/name "Sour dough"}
              1 {:dough/id 1
                 :dough/name "Main dough"}}})
