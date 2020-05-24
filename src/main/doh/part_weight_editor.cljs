(ns doh.part-weight-editor
  (:require [doh.events :refer [check-spec-interceptor]]
            [re-frame.core :as rf]))

(defn- parse-float [x]
  (let [n (js/parseFloat x)]
    (when-not (js/isNaN n)
      n)))

(defn- parse-weight [x]
  (when-let [n (parse-float x)]
    (when (> n 0)
      n)))

(defn- db-path [{:keys [mixture-index part-index]} & ks]
  (apply conj [:recipe/mixtures mixture-index
               :mixture/parts part-index] ks))

;;;; Subscriptions

(defn get-part
  "Extract a part from the database."
  [db [_ part-ident]]
  (get-in db (db-path part-ident)))

(rf/reg-sub ::part get-part)

(defn- part-signal
  "Subscribe to a part."
  [[_ part-ident] _]
  (rf/subscribe [::part part-ident]))

;; TODO: Delete
(defn editing?
  "Tells if we are currently editing a mixture part."
  [part _]
  (some? (::data part)))

;; TODO: Delete
(defn get-entered-weight
  "Extracts the weight as entered by the user."
  [part _]
  (get-in part [::data ::entered]))

;; TODO: Delete
(rf/reg-sub ::editing? part-signal editing?)

;; TODO: Delete
(rf/reg-sub ::entered-weight part-signal get-entered-weight)

(defn editor
  [part _]
  {:editing? (some? (::data part))
   :input (or (get-in part [::data ::entered])
              (:part/quantity part))})

(rf/reg-sub ::editor part-signal editor)

;;;; Events

(defn start-edit
  "Starts editing a part's weight."
  [db [_ part-ident]]
  (update-in db (db-path part-ident) #(assoc % ::data %)))

(rf/reg-event-db ::start-edit [check-spec-interceptor] start-edit)

(defn enter-weight
  "Enters a new value for a part's weight."
  [db [_ part-ident value]]
  (let [db (assoc-in db (db-path part-ident ::data ::entered) value)]
    (if-let [new-weight (parse-weight value)]
      (assoc-in db (db-path part-ident :part/quantity) new-weight)
      db)))

(rf/reg-event-db ::enter-weight [check-spec-interceptor] enter-weight)

(defn- clean-up
  [db part-ident]
  (update-in db (db-path part-ident) dissoc ::data))

(defn cancel-edit
  "Cancels editing previously stared with `start-edit`."
  [db [_ part-ident]]
  (let [new-db (clean-up db part-ident)]
    (if-let [old-weight (get-in db (db-path part-ident ::data :part/quantity))]
      (assoc-in new-db (db-path part-ident :part/quantity) old-weight)
      new-db)))

(rf/reg-event-db ::cancel-edit [check-spec-interceptor] cancel-edit)

(defn save-edit
  "Saves editing."
  [db [_ part-ident]]
  (clean-up db part-ident))

(rf/reg-event-db ::save-edit [check-spec-interceptor] save-edit)
