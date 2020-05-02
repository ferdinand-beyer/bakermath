(ns bakermath.mutation
  (:require
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(defmutation delete-item
  "Delete item :item/id from list :list/id."
  [{list-id :list/id
    item-id :item/id}]
  (action [{:keys [state] :as env}]
          (swap! state merge/remove-ident* [:item/id item-id] [:list/id list-id :list/items])))
