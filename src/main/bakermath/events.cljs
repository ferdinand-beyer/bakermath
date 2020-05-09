(ns bakermath.events
  (:require [bakermath.db :as db]
            [cljs.spec.alpha :as s]
            [re-frame.core :as rf]))

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `spec`."
  [spec db]
  (when-not (s/valid? spec db)
    (throw (ex-info (str "spec check failed")
                    {:explain-str (s/explain-str spec db)}))))

(def check-spec-interceptor (rf/after (partial check-and-throw ::db/db)))

(rf/reg-event-db
 ::init-db
 [check-spec-interceptor]
 (constantly db/default-db))

(rf/reg-event-db
 ::edit-new-dough-ingredient
 [check-spec-interceptor]
 (fn [db [_ {:keys [dough-index]}]]
   (assoc db :dough-ingredient-editor
          {:editor/dough-ref dough-index
           :editor/mode :new
           :editor/visible true})))

(rf/reg-event-db
 ::update-dough-ingredient-editor-name
 [check-spec-interceptor]
 (fn [db [_ name]]
   (assoc-in db [:dough-ingredient-editor :ingredient/name] name)))

(rf/reg-event-db
 ::update-dough-ingredient-editor-quantity
 [check-spec-interceptor]
 (fn [db [_ quantity]]
   (assoc-in db [:dough-ingredient-editor :dough-ingredient/quantity]
             (int quantity))))

(rf/reg-event-db
 ::cancel-dough-ingredient-edit
 [check-spec-interceptor]
 (fn [db _]
   (assoc-in db [:dough-ingredient-editor :editor/visible] false)))

(rf/reg-event-db
 ::save-dough-ingredient-edit
 [check-spec-interceptor]
 (fn [db _]
   (let [editor (:dough-ingredient-editor db)
         ingredient (select-keys editor [:ingredient/name
                                         :dough-ingredient/quantity])]
     (-> db
         (update-in [:recipe/doughs
                     (:editor/dough-ref editor)
                     :dough/ingredients]
                    ;; TODO: Force into vector to append to the end?
                    conj ingredient)
         (assoc-in [:dough-ingredient-editor :editor/visible] false)))))
