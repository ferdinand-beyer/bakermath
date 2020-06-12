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

;;;; Initialization

(def init-db (constantly db/default-db))

(rf/reg-event-db
 ::init-db
 [check-spec-interceptor]
 init-db)

;;;; Views

(rf/reg-event-db
 ::select-recipe-tab
 [check-spec-interceptor]
 (fn [db [_ {:keys [tab]}]]
   (assoc db :view/tab tab)))

;;;; Parts

(rf/reg-event-db
 ::delete-part
 [check-spec-interceptor]
 (fn [db [_ mixture-id ingredient-id]]
   ; TODO Delete ingredient when no longer referenced?
   (update-in db [:db/mixtures mixture-id :mixture/parts]
              dissoc ingredient-id)))

;;;; Part Editor

(defn- parse-float [x]
  (let [n (js/parseFloat x)]
    (when-not (js/isNaN n)
      n)))

(defn new-part
  "Open the part editor to add a part to the mixture."
  [db mixture-id]
  (assoc db :view/part-editor
         #:editor {:visible? true
                   :mode :new
                   :mixture-id mixture-id}))

(defn edit-part
  "Open the part editor to edit an existing part."
  [db mixture-id ingredient-id]
  (let [name (get-in db [:db/ingredients ingredient-id :ingredient/name])
        quantity (get-in db [:db/mixtures mixture-id :mixture/parts ingredient-id])]
    (assoc db :view/part-editor
           #:editor {:visible? true
                     :mode :edit
                     :mixture-id mixture-id
                     :ingredient-id ingredient-id
                     :name {:field/input name}
                     :quantity {:field/input quantity}})))

(defn change-part-name [db name]
  (assoc-in db [:view/part-editor :editor/name] {:field/input name}))

(defn change-part-quantity [db quantity]
  (assoc-in db [:view/part-editor :editor/quantity] {:field/input quantity}))

(defn cancel-part
  [db]
  (assoc-in db [:view/part-editor :editor/visible?] false))

(defn validate-name
  [db]
  (let [{{{input :field/input} :editor/name
          mixture-id :editor/mixture-id
          prev-id :editor/ingredient-id} :view/part-editor} db
        value (when (db/not-blank? input) input)
        id (when-let [ingredient (and value (db/find-ingredient-by-name db value))]
             (:ingredient/id ingredient))
        error (cond
                (nil? value) "Invalid name"
                (and (some? id)
                     (not= id prev-id)
                     (some? (get-in db [:db/mixtures mixture-id
                                        :mixture/parts id])))
                "Already exists")]
    (assoc-in db [:view/part-editor :editor/name]
              (cond-> #:field{:input input, :value value}
                (some? error) (assoc :field/error error)
                (some? id) (assoc :ingredient/id id)))))

(defn validate-quantity
  [db]
  (let [input (get-in db [:view/part-editor :editor/quantity :field/input])
        value (parse-float input)
        error (cond
                (nil? value) "Invalid number"
                (not (pos? value)) "Must be positive")]
    (assoc-in db [:view/part-editor :editor/quantity]
              (cond-> #:field{:input input, :value value}
                (some? error) (assoc :field/error error)))))

(defn- exclusive-ingredient?
  [db ingredient-id]
  (nil? (second (db/find-mixtures-by-ingredient-id db ingredient-id))))

(defn save-part
  [db]
  (let [{{mixture-id :editor/mixture-id
          prev-ingredient-id :editor/ingredient-id
          {name :field/value
           name-error :field/error
           ingredient-id :ingredient/id} :editor/name
          {quantity :field/value
           quantity-error :field/error} :editor/quantity}
         :view/part-editor
         :as db}
        (-> db validate-name validate-quantity)]
    (if (or name-error quantity-error)
      db
      (let [[db ingredient-id]
            (cond
              (some? ingredient-id) [db ingredient-id]
              
              (and (some? prev-ingredient-id)
                   (exclusive-ingredient? db prev-ingredient-id))
              [(assoc-in db [:db/ingredients prev-ingredient-id
                             :ingredient/name] name)
               prev-ingredient-id]
              
              :else (db/add-ingredient db name false))]
        (-> db
            (cond->
             (and (some? prev-ingredient-id)
                  (not= prev-ingredient-id ingredient-id))
              (update-in [:db/mixtures mixture-id :mixture/parts]
                         dissoc prev-ingredient-id))
            (assoc-in [:db/mixtures mixture-id
                       :mixture/parts ingredient-id]
                      quantity)
            (assoc-in [:view/part-editor :editor/visible?] false))))))

(rf/reg-event-db
 ::new-part
 [check-spec-interceptor]
 (fn [db [_ mixture-id]]
   (new-part db mixture-id)))

(rf/reg-event-db
 ::edit-part
 [check-spec-interceptor]
 (fn [db [_ mixture-id ingredient-id]]
   (edit-part db mixture-id ingredient-id)))

(rf/reg-event-db
 ::change-part-name
 [check-spec-interceptor]
 (fn [db [_ name]]
   (change-part-name db name)))

(rf/reg-event-db
 ::change-part-quantity
 [check-spec-interceptor]
 (fn [db [_ quantity]]
   (change-part-quantity db quantity)))

(rf/reg-event-db
 ::cancel-part
 [check-spec-interceptor]
 (fn [db _]
   (cancel-part db)))

(rf/reg-event-db
 ::save-part
 [check-spec-interceptor]
 (fn [db _]
   (save-part db)))
