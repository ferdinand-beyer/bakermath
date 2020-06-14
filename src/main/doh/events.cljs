(ns doh.events
  (:require [doh.db :as db]
            [cljs.spec.alpha :as s]
            [re-frame.core :as rf]
            [day8.re-frame.undo :refer [undoable]]))

(defn validate-db
  [db]
  (when-let [data (s/explain-data ::db/db db)]
    (throw (ex-info (str "spec check failed") data))))

(def check-spec-interceptor (rf/after validate-db))

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

;;;; Ingredients

(defn toggle-ingredient-flour
  [db ingredient-id]
  (update-in db [:db/ingredients ingredient-id :ingredient/flour?] not))

(rf/reg-event-db
 ::toggle-ingredient-flour
 [check-spec-interceptor]
 (fn [db [_ ingredient-id]]
   (toggle-ingredient-flour db ingredient-id)))

;;;; Mixtures

(defn delete-mixture
  [db mixture-id]
  (let [recipe-id (:view/recipe db)]
    (-> db
        (update-in [:db/recipes recipe-id :recipe/mixtures]
                   #(->> % (remove (partial = mixture-id)) vec)))))

(rf/reg-event-db
 ::delete-mixture
 [(undoable "Mixture deleted")
  check-spec-interceptor]
 (fn [db [_ mixture-id]]
   (delete-mixture db mixture-id)))

;;;; Parts

(rf/reg-event-db
 ::delete-part
 [(undoable "Ingredient deleted")
  check-spec-interceptor]
 (fn [db [_ mixture-id ingredient-id]]
   ; TODO Delete ingredient when no longer referenced?
   (update-in db [:db/mixtures mixture-id :mixture/parts]
              dissoc ingredient-id)))

;;;; Mixture Editor

(defn new-mixture
  [db]
  (assoc db :view/mixture-editor
         #:editor{:visible? true}))

(defn edit-mixture
  [db mixture-id]
  (assoc db :view/mixture-editor
         #:editor{:visible? true
                  :mixture-id mixture-id
                  :name {:field/input (get-in db [:db/mixtures mixture-id :mixture/name])}}))

(defn change-mixture-name
  [db name]
  (assoc-in db [:view/mixture-editor :editor/name :field/input] name))

(defn validate-mixture-name
  [db]
  (let [{{{:field/keys [input]} :editor/name
          :editor/keys [mixture-id]} :view/mixture-editor} db
        value (when (db/not-blank? input) input)
        existing (when value (db/find-mixture-by-name db value))
        error (cond
                (nil? value) "Invalid name"
                (and (some? existing)
                     (not= mixture-id (:mixture/id existing)))
                "Already exists")]
    (assoc-in db [:view/mixture-editor :editor/name]
              (cond-> #:field{:input input, :value value}
                (some? error) (assoc :field/error error)))))

(defn save-mixture
  [db]
  (let [{{:editor/keys [mixture-id]
          {:field/keys [value error]} :editor/name}
         :view/mixture-editor
         :as db}
        (validate-mixture-name db)]
    (if (some? error)
      db
      (-> (if (some? mixture-id)
            (assoc-in db [:db/mixtures mixture-id :mixture/name] value)
            (let [recipe-id (:view/recipe db)
                  [db id] (db/add-mixture db value {})]
              (update-in db [:db/recipes recipe-id :recipe/mixtures] conj id)))
          (assoc-in [:view/mixture-editor :editor/visible?] false)))))

(defn cancel-mixture
  [db]
  (assoc-in db [:view/mixture-editor :editor/visible?] false))

(rf/reg-event-db
 ::new-mixture
 (fn [db _]
   (new-mixture db)))

(rf/reg-event-db
 ::edit-mixture
 (fn [db [_ mixture-id]]
   (edit-mixture db mixture-id)))

(rf/reg-event-db
 ::change-mixture-name
 (fn [db [_ name]]
   (change-mixture-name db name)))

(rf/reg-event-db
 ::save-mixture
 (fn [db _]
   (save-mixture db)))

(rf/reg-event-db
 ::cancel-mixture
 (fn [db _]
   (cancel-mixture db)))

;;;; Part Editor

(defn- parse-float [x]
  (let [n (js/parseFloat x)]
    (when-not (js/isNaN n)
      n)))

(defn new-part
  "Open the part editor to add a part to the mixture."
  [db mixture-id]
  (assoc db :view/part-editor
         #:editor{:visible? true
                  :mixture-id mixture-id}))

(defn edit-part
  "Open the part editor to edit an existing part."
  [db mixture-id ingredient-id]
  (let [name (get-in db [:db/ingredients ingredient-id :ingredient/name])
        quantity (get-in db [:db/mixtures mixture-id :mixture/parts ingredient-id])]
    (assoc db :view/part-editor
           #:editor{:visible? true
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

;;;; Quantity editor

(defn edit-quantity
  [db mixture-id ingredient-id]
  (let [quantity (get-in db [:db/mixtures mixture-id
                             :mixture/parts ingredient-id])]
    (assoc db :view/quantity-editor
           #:editor{:visible? true
                    :mixture-id mixture-id
                    :ingredient-id ingredient-id
                    :quantity #:field{:orig quantity
                                      :input (or quantity "")}})))

(defn change-quantity
  [db input]
  (let [{{:editor/keys [mixture-id ingredient-id]} :view/quantity-editor} db
        quantity (parse-float input)]
    (-> db
        (cond-> (and (some? quantity) (pos? quantity))
          (assoc-in [:db/mixtures mixture-id
                     :mixture/parts ingredient-id] quantity))
        (update-in [:view/quantity-editor :editor/quantity]
                   merge #:field{:input input
                                 :value quantity}))))

(defn save-quantity
  [db]
  (assoc-in db [:view/quantity-editor :editor/visible?] false))

(defn cancel-quantity
  [{{:editor/keys [mixture-id ingredient-id]
     {orig :field/orig} :editor/quantity} :view/quantity-editor
    :as db}]
  (-> (if (and (some? orig) (pos? orig))
        (assoc-in db [:db/mixtures mixture-id
                      :mixture/parts ingredient-id] orig)
        (update-in db [:db/mixtures mixture-id :mixture/parts]
                   dissoc ingredient-id))
      (assoc-in [:view/quantity-editor :editor/visible?] false)))

(rf/reg-event-db
 ::edit-quantity
 [check-spec-interceptor]
 (fn [db [_ mixture-id ingredient-id]]
   (edit-quantity db mixture-id ingredient-id)))

(rf/reg-event-db
 ::change-quantity
 [check-spec-interceptor]
 (fn [db [_ quantity]]
   (change-quantity db quantity)))

(rf/reg-event-db
 ::save-quantity
 [check-spec-interceptor]
 (fn [db _]
   (save-quantity db)))

(rf/reg-event-db
 ::cancel-quantity
 [check-spec-interceptor]
 (fn [db _]
   (cancel-quantity db)))
