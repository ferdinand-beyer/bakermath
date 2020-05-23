(ns doh.events
  (:require [doh.db :as db]
            [cljs.spec.alpha :as s]
            [re-frame.core :as rf]))

(defn validate-db
  [db]
  (when-let [data (s/explain-data ::db/db db)]
    (throw (ex-info (str "spec check failed") data))))

(def check-spec-interceptor (rf/after validate-db))

(defn dissoc-vec
  "Remove an element by index from a vector."
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(def init-db (constantly db/default-db))

(rf/reg-event-db
 ::init-db
 [check-spec-interceptor]
 init-db)

(rf/reg-event-db
 ::select-recipe-tab
 [check-spec-interceptor]
 (fn [db [_ {:keys [tab]}]]
   (assoc db :recipe/tab tab)))

(rf/reg-event-db
 ::edit-part
 [check-spec-interceptor]
 (fn [db [_ {:keys [mixture-index part-index]}]]
   (let [{:part/keys [ingredient-id quantity]}
         (get-in db [:recipe/mixtures mixture-index
                     :mixture/parts part-index])
         {:ingredient/keys [name]}
         (get-in db [:ingredients ingredient-id])]
     (assoc db :part-editor
            {:editor/mixture-index mixture-index
             :editor/part-index part-index
             :editor/mode :edit
             :editor/visible true
             :ingredient/name name
             :part/ingredient-id ingredient-id
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
             ;; TODO: Express everything in milligrams?
             (float quantity))))

(rf/reg-event-db
 ::cancel-part-edit
 [check-spec-interceptor]
 (fn [db _]
   (assoc-in db [:part-editor :editor/visible] false)))

(defn save-ingredient
  [db name]
  (let [ingredients (:ingredients db)]
    (if-let [id (->> ingredients
                     (filter #(= name (:ingredient/name (val %))))
                     (map key)
                     first)]
      [db id]
      (let [new-id (count ingredients)
            new-db (assoc-in db [:ingredients new-id] {:ingredient/name name})]
        [new-db new-id]))))

(defn save-part-editor
  [db _]
  (let [{:editor/keys [mode mixture-index part-index]
         :ingredient/keys [name]
         :part/keys [quantity]}
        (:part-editor db)
        
        [db ingredient-id] (save-ingredient db name)
        part {:part/ingredient-id ingredient-id
              :part/quantity quantity}]
    (cond-> db
      (= :new mode) (update-in [:recipe/mixtures mixture-index :mixture/parts]
                               (fnil #(conj % part) []))
      (= :edit mode) (update-in [:recipe/mixtures mixture-index
                                 :mixture/parts part-index]
                                merge part)
      :finally (assoc-in [:part-editor :editor/visible] false))))

(rf/reg-event-db
 ::save-part-edit
 [check-spec-interceptor]
 save-part-editor)

(rf/reg-event-db
 ::delete-part
 [check-spec-interceptor]
 (fn [db [_ {:keys [mixture-index part-index]}]]
   (update-in db [:recipe/mixtures mixture-index :mixture/parts]
              dissoc-vec part-index)))
