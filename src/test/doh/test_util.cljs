(ns doh.test-util
  (:require-macros [doh.test-util]))

;; Used in the 'is' macro, declare to silence editors.
(declare valid-db?)

(def test-db
  {:ingredients
   {0 {:ingredient/name "Rye flour full grain"
       :ingredient/flour-proportion 1.0}
    1 {:ingredient/name "Rye flour type 1150"
       :ingredient/flour-proportion 1.0}
    2 {:ingredient/name "Water"}
    3 {:ingredient/name "Sourdough starter"
       :ingredient/flour-proportion 0.5}
    4 {:ingredient/name "Wheat flour full grain"
       :ingredient/flour-proportion 1.0}
    5 {:ingredient/name "Wheat flour type 1050"
       :ingredient/flour-proportion 1.0}
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

