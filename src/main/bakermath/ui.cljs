(ns bakermath.ui
  (:require
   [bakermath.ui.material :as material]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]))

(defsc AppBar [this props]
  (material/app-bar
   {}
   (material/tool-bar
    {}
    (material/typography {:variant "h6"} "Baker's Math"))))

(def app-bar (comp/factory AppBar))

(defsc Ingredient [this {:ingredient/keys [name quantity]}]
  {:initial-state (fn [{:keys [name quantity]}]
                    #:ingredient{:name name
                                 :quantity quantity})}
  (material/list-item
   {:button true}
   (material/list-item-text {:primary name,
                             :secondary quantity})))

(def ingredient (comp/factory Ingredient {:keyfn :ingredient/name}))

(defsc IngredientList [this {:list/keys [name ingredients]}]
  {:initial-state
   (fn [{:keys [name]}]
     {:list/name name
      :list/ingredients
      (letfn [(ingredient [[name quantity]]
                (comp/get-initial-state Ingredient {:name name
                                                    :quantity quantity}))]
        (case name
          "Sourdough"
          (map ingredient
               [["Rye Full Grain" "100 g"]
                ["Rye 1150" "50 g"]
                ["Water" "150 g"]
                ["Starter" "15 g"]])
          "Main dough"
          (map ingredient
               [["Rye 1150" "200 g"]
                ["Wheat 1050" "100 g"]
                ["Wheat Full Grain" "50 g"]
                ["Water" "175 g"]
                ["Salt" "9 g"]])))})}
  (material/list
   {}
   (concat [(material/list-subheader {} name)]
    (map ingredient ingredients))))

(def ingredient-list (comp/factory IngredientList))

(defsc Root [this props]
  {:initial-state
   (fn [params] {:sourdough (comp/get-initial-state IngredientList {:name "Sourdough"})
                 :main (comp/get-initial-state IngredientList {:name "Main dough"})})}
  (dom/div
   (app-bar)
   (dom/div {:style {:margin-top "64px"}}
            (ingredient-list (:sourdough props))
            (ingredient-list (:main props)))))
