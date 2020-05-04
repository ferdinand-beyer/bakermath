(ns bakermath.mutation
  (:require
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting :refer [integrate-ident*]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(defmutation delete-item
  "Delete item :item/id from list :list/id."
  [{list-id :list/id
    item-id :item/id}]
  (action [{:keys [state] :as env}]
          (swap! state #(-> %
                            (merge/remove-ident* [:item/id item-id]
                                                 [:list/id list-id :list/items])
                            (update-in [:item/id] dissoc item-id)))))

(defmutation add-ingredient
  [{:keys [tempid name]}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:ingredient/id tempid] {:ingredient/id tempid
                                                         :ingredient/name name})))

(defmutation add-item
  [{:keys [tempid quantity]
    list-id :list/id
    ingredient-id :ingredient/id}]
  (action
   [{:keys [state] :as env}]
   (let [ident [:item/id tempid]]
          (swap! state #(-> %
                            (assoc-in ident {:item/id tempid
                                             :item/quantity quantity
                                             :item/ingredient [:ingredient/id ingredient-id]})
                            (integrate-ident* ident :append [:list/id list-id :list/items]))))))
