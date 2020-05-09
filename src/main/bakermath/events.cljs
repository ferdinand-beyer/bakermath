(ns bakermath.events
  (:require [bakermath.db :as db]
            [re-frame.core :as rf]))

(rf/reg-event-db
 ::init-db
 []
 (fn [db _] db/default-db))
