(ns doh.core
  (:require [reagent.dom]
            [re-frame.core :as rf]
            [doh.events :as e]
            ; [doh.subs]
            [doh.views]))

(defn render! []
  (reagent.dom/render [doh.views/root]
                      (.getElementById js/document "app")))

(defn ^:dev/after-load refresh! []
  (rf/clear-subscription-cache!)
  (render!))

(defn ^:export init []
  (enable-console-print!)
  (rf/dispatch-sync [::e/init-db])
  (render!))
