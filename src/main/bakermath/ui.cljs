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

(defsc Ingredient
  [this
   {:item/keys [id quantity]
    :ingredient/keys [name]
    :as props}
   {:keys [on-delete]}]
  {:query [:item/id :item/quantity :ingredient/name]
   :ident (fn [] [:item/id (:item/id props)])
   :initial-state (fn [{:keys [id name quantity]}]
                    {:item/id id
                     :item/quantity quantity
                     :ingredient/name name})}
  (material/list-item
   {:button true}
   (material/list-item-text {:primary name,
                             :secondary quantity})
   (material/list-item-secondary-action
    {}
    (material/icon-button
     {:edge "end"
      :aria-label "delete"
      :onClick #(on-delete id)}
     (material/delete-icon {})))))

(def ingredient (comp/factory Ingredient {:keyfn :item/id}))

(defsc IngredientList
  [this {:list/keys [name items] :as props}]
  {:query [:list/id :list/name {:list/items (comp/get-query Ingredient)}]
   :ident (fn [] [:list/id (:list/id props)])
   :initial-state
   (fn [{:keys [id name]}]
     {:list/id id
      :list/name name
      :list/items
      (let [keys [:id :name :quantity]
            item-state #(comp/get-initial-state Ingredient (zipmap keys %))]
        (case id
          1 (mapv item-state
                  [[1 "Rye Full Grain" "100 g"]
                   [2 "Rye 1150" "50 g"]
                   [3 "Water" "150 g"]
                   [4 "Starter" "15 g"]])

          2 (mapv item-state
                  [[5 "Rye 1150" "200 g"]
                   [6 "Wheat 1050" "100 g"]
                   [7 "Wheat Full Grain" "50 g"]
                   [8 "Water" "175 g"]
                   [9 "Salt" "9 g"]])))})}
  (let [delete (fn [id] (println "Delete Item: " id))]
    (material/list
     {}
     (material/list-subheader {} name)
     (map #(ingredient (comp/computed % {:on-delete delete})) items))))

(def ingredient-list (comp/factory IngredientList {:keyfn :list/id}))

(defsc Root [this {:recipe/keys [lists]}]
  {:query [{:recipe/lists (comp/get-query IngredientList)}]
   :initial-state
   (fn [params]
     {:recipe/lists (mapv #(comp/get-initial-state IngredientList (zipmap [:id :name] %))
                          [[1 "Sourdough"] [2 "Main dough"]])})}
  (dom/div
   (app-bar)
   (dom/div {:style {:marginTop "64px"}}
            (map ingredient-list lists))))
