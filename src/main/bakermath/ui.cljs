(ns bakermath.ui
  (:require
   [bakermath.mutation :as mut]
   [bakermath.ui.material :as material]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

(def ingredients {:rfg "Rye Full Grain"
                  :r1150 "Rye 1150"
                  :wfg "Wheat Full Grain"
                  :w1050 "Wheat 1050"
                  :starter "Sourdough Starter"
                  :w "Water"
                  :s "Salt"})

(defsc AppBar [this props]
  (material/app-bar
   {}
   (material/tool-bar
    {}
    (material/typography {:variant "h6"} "Baker's Math"))))

(def app-bar (comp/factory AppBar))

(defsc Ingredient [this props]
  {:query [:ingredient/id :ingredient/name]
   :ident [:ingredient/id :ingredient/id]
   :initial-state
   (fn [id] #:ingredient{:id id
                         :name (get ingredients id)})})

(defsc Item
  [this
   {:item/keys [id quantity ingredient]
    {name :ingredient/name} :item/ingredient
    :as props}
   {:keys [on-delete]}]
  {:query [:item/id :item/quantity
           {:item/ingredient (comp/get-query Ingredient)}]
   :ident (fn [] [:item/id (:item/id props)])
   :initial-state (fn [{:keys [id ingredient quantity]}]
                    {:item/id id
                     :item/quantity quantity
                     :item/ingredient
                     (comp/get-initial-state Ingredient ingredient)})}
  (material/list-item
   {:button true}
   (material/list-item-avatar
    {} (material/avatar {} (-> name (.substr 0 1) .toUpperCase)))
   (material/list-item-text {:primary name,
                             :secondary quantity})
   (material/list-item-secondary-action
    {}
    (material/icon-button
     {:edge "end"
      :aria-label "delete"
      :onClick #(on-delete id)}
     (material/delete-icon {})))))

(def ingredient (comp/factory Item {:keyfn :item/id}))

(defsc IngredientList
  [this {:list/keys [id name items]
         :ui/keys [editing]
         :as props}]
  {:query [:list/id :list/name :ui/editing
           {:list/items (comp/get-query Item)}]
   :ident (fn [] [:list/id (:list/id props)])
   :initial-state
   (fn [{:keys [id name]}]
     {:list/id id
      :list/name name
      :list/items
      (let [keys [:id :ingredient :quantity]
            item-state #(comp/get-initial-state Item (zipmap keys %))]
        (case id
          1 (mapv item-state
                  [[1 :rfg "100 g"]
                   [2 :r1150 "50 g"]
                   [3 :w "150 g"]
                   [4 :starter "15 g"]])

          2 (mapv item-state
                  [[5 :r1150 "200 g"]
                   [6 :w1050 "100 g"]
                   [7 :wfg "50 g"]
                   [8 :w "175 g"]
                   [9 :s "9 g"]])))})}
  (let [delete (fn [item-id]
                 (comp/transact! this
                                 [(mut/delete-item {:list/id id
                                                    :item/id item-id})]))]
    (material/list
     {}
     (material/list-subheader {} name)
     (map #(ingredient (comp/computed % {:on-delete delete})) items)
     (material/list-item
      {:button true
       :onClick #(m/toggle! this :ui/editing)}
      (material/list-item-icon {} (material/add-icon))
      (material/list-item-text {:primary "Add ingredient"}))
     (material/dialog
      {:open (boolean editing)
       :disableBackdropClick true
       :disableEscapeKeyDown true}
      (material/dialog-title {} "Add ingredient")
      (material/dialog-content
       {}
       (material/text-field
        {:label "Ingredient"
         :autoFocus true})
       (material/text-field
        {:label "Quantity"
         :type "number"
         :InputProps {:endAdornment (material/input-adornment {:position "end"} "g")}}))
      (material/dialog-actions
       {}
       (material/button
        {:onClick #(m/toggle! this :ui/editing)}
        "Cancel")
       (material/button
        {:color "primary"
         :onClick #(m/toggle! this :ui/editing)}
        "Save"))))))

(def ingredient-list (comp/factory IngredientList {:keyfn :list/id}))

(defsc Root [this {:recipe/keys [lists]}]
  {:query [{:recipe/lists (comp/get-query IngredientList)}]
   :initial-state
   (fn [params]
     {:recipe/lists (mapv #(comp/get-initial-state IngredientList (zipmap [:id :name] %))
                          [[1 "Sourdough"] [2 "Main dough"]])})}
  (dom/div
   (app-bar)
   (material/tool-bar {})
   (map ingredient-list lists)))
