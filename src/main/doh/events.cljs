(ns doh.events
  (:require [doh.db :as db]
            [cljs.spec.alpha :as s]
            [re-frame.core :as rf]))

(defn check-and-throw
  "Throws an exception if `val` doesn't match the Spec `spec`."
  [spec val]
  (when-not (s/valid? spec val)
    (throw (ex-info (str "spec check failed")
                    (s/explain-data spec val)))))

(def check-spec-interceptor (rf/after (partial check-and-throw ::db/db)))

(defn dissoc-vec
  "Remove an element by index from a vector."
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(rf/reg-event-db
 ::init-db
 [check-spec-interceptor]
 (constantly db/default-db))

(rf/reg-event-db
 ::select-recipe-tab
 [check-spec-interceptor]
 (fn [db [_ {:keys [tab]}]]
   (assoc db :recipe/tab tab)))

(rf/reg-event-db
 ::edit-part
 [check-spec-interceptor]
 (fn [db [_ {:keys [mixture-index part-index]}]]
   (let [{name :ingredient/name
          quantity :part/quantity}
         (get-in db [:recipe/mixtures mixture-index
                     :mixture/parts part-index])]
     (assoc db :part-editor
            {:editor/mixture-index mixture-index
             :editor/part-index part-index
             :editor/mode :edit
             :editor/visible true
             :ingredient/name name
             :part/quantity quantity}))))

(rf/reg-event-db
 ::edit-new-part
 [check-spec-interceptor]
 (fn [db [_ {:keys [mixture-index]}]]
   (assoc db :part-editor
          {:editor/mixture-index mixture-index
           :editor/mode :new
           :editor/visible true})))

(rf/reg-event-db
 ::update-part-editor-name
 [check-spec-interceptor]
 (fn [db [_ name]]
   (assoc-in db [:part-editor :ingredient/name] name)))

(rf/reg-event-db
 ::update-part-editor-quantity
 [check-spec-interceptor]
 (fn [db [_ quantity]]
   (assoc-in db [:part-editor :part/quantity]
             (int quantity))))

(rf/reg-event-db
 ::cancel-part-edit
 [check-spec-interceptor]
 (fn [db _]
   (assoc-in db [:part-editor :editor/visible] false)))

(rf/reg-event-db
 ::save-part-edit
 [check-spec-interceptor]
 (fn [db _]
   (let [{:editor/keys [mode mixture-index part-index] :as editor} (:part-editor db)
         part (select-keys editor [:ingredient/name
                                   :part/quantity])]
     (cond-> db
       (= :new mode) (update-in [:recipe/mixtures mixture-index :mixture/parts]
                                (fnil #(conj % part) []))
       (= :edit mode) (update-in [:recipe/mixtures mixture-index
                                  :mixture/parts part-index]
                                 merge part)
       :finally (assoc-in [:part-editor :editor/visible] false)))))

(rf/reg-event-db
 ::delete-part
 [check-spec-interceptor]
 (fn [db [_ {:keys [mixture-index part-index]}]]
   (update-in db [:recipe/mixtures mixture-index :mixture/parts]
              dissoc-vec part-index)))
