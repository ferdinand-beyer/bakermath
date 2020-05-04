(ns bakermath.ui
  (:require
   [bakermath.mutation :as mut]
   [bakermath.ui.material :as material]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid :refer [tempid]]
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

(defmutation new-item [{:keys [id]}]
  (action [{:keys [state]}]
          (swap! state (fn [s]
                         (-> s
                             (assoc-in [:form/id :item] {:form/id :item
                                                         :form/mode :new}))))))

(defmutation edit-item [{:keys [id]}]
  (action [{:keys [state]}]
          (swap! state (fn [s]
                         (-> s
                             (assoc-in [:form/id :item] {:form/id :item
                                                         :form/mode :edit
                                                         :form/item [:item/id id]}))))))

(defmutation close-item-edit [{:keys [id]}]
  (action [{:keys [state]}]
          (swap! state update-in [:form/id :item] dissoc :form/mode)))

(defsc ItemForm
  [this {:keys [item/quantity ingredient/name]
         :form/keys [mode item ingredient]
         :as props}]
  {:query [:form/id :form/mode :item/quantity :ingredient/name
           {:form/item [:item/id :item/quantity
                        {:item/ingredient [:ingredient/id :ingredient/name]}]}
           {:form/ingredient [:ingredient/id :ingredient/name]}]
   :ident :form/id}
  (let [ingredient* (or ingredient (:item/ingredient item))
        quantity* (or quantity (:item/quantity item) "")
        name* (or name (:ingredient/name ingredient*) "")
        cancel #(comp/transact! this [(close-item-edit {})])
        save (fn []
               (let [ingredient-id (or (:ingredient/id ingredient*) (tempid))
                     item-id (or (:item/id item) (tempid))]
                 ; TODO: Create a new ingredient when changing the name?
                 (comp/transact! this [(mut/add-ingredient {:tempid ingredient-id
                                                            :name name*})
                                       (mut/add-item {:tempid item-id
                                                      :quantity quantity*
                                                      :ingredient/id ingredient-id
                                                      ; TODO: List ID!
                                                      :list/id 1})
                                       (close-item-edit {})])))]
    (material/dialog
     ;; TODO Removing the mode will change the title while phasing
     ;; out the dialog.  Need a dedicated :form/enabled?
     {:open (some? mode)
      :disableBackdropClick true
      :disableEscapeKeyDown true}
     (material/dialog-title {} (if (= mode :new)
                                 "Add ingredient"
                                 "Edit ingredient"))
     (material/dialog-content
      {}
      (material/text-field
       {:label "Ingredient"
        :autoFocus (= mode :new)
        :value name*
        :onChange #(m/set-string! this :ingredient/name :event %)})
      (material/text-field
       {:label "Quantity"
        ;:type "number"
        ;:InputProps {:endAdornment (material/input-adornment {:position "end"} "g")}
        :autoFocus (= mode :edit)
        :value quantity*
        :onChange #(m/set-string! this :item/quantity :event %)}))
     (material/dialog-actions
      {}
      (material/button
       {:onClick cancel}
       "Cancel")
      (material/button
       {:color "primary"
        :onClick save}
       "Save")))))

(def item-form (comp/factory ItemForm {:keyfn :item/id}))

(defn avatar-color [name]
  (str "hsl(" (-> name hash (mod 360)) ", 70%, 60%"))

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
   {:button true
    :onClick #(comp/transact! this [(edit-item {:id id})])}
   (material/list-item-avatar
    {}
    (material/avatar
     {:style {:backgroundColor (avatar-color name)}}
     (-> name (.substr 0 1) .toUpperCase)))
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
         :as props}]
  {:query [:list/id :list/name {:list/items (comp/get-query Item)}]
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
  (letfn [(delete [item-id]
            (comp/transact! this
                            [(mut/delete-item {:list/id id
                                               :item/id item-id})]))
          (add-item []
                    (let [ingredient-id (tempid)
                          item-id (tempid)]
                    (comp/transact! this
                                    [(mut/add-ingredient {:tempid ingredient-id
                                                          :name "Sunflower seeds"})
                                     (mut/add-item {:tempid item-id
                                                    :quantity "123 g"
                                                    :list/id id
                                                    :ingredient/id ingredient-id})])))]
    (material/list
     {}
     (material/list-subheader {} name)
     (map #(ingredient (comp/computed % {:on-delete delete})) items)
     (material/list-item
      {:button true
       :onClick #(comp/transact! this [(new-item {:id (tempid)})])}
      (material/list-item-icon {} (material/add-icon))
      (material/list-item-text {:primary "Add ingredient"})))))

(def ingredient-list (comp/factory IngredientList {:keyfn :list/id}))

(defsc Root [this {lists :root/lists
                   form :root/item-form}]
  {:query [{:root/lists (comp/get-query IngredientList)}
           {:root/item-form (comp/get-query ItemForm)}]
   :initial-state
   (fn [params]
     {:root/lists (mapv #(comp/get-initial-state IngredientList (zipmap [:id :name] %))
                        [[1 "Sourdough"] [2 "Main dough"]])
      :root/item-form {:form/id :item}})}
  (dom/div
   (app-bar)
   (material/tool-bar {})
   (map ingredient-list lists)
   (item-form form)))
