(ns bakermath.core
  (:require [reagent.dom]
            [re-frame.core :as rf]
            [bakermath.events :as e]
            ; [bakermath.subs]
            [bakermath.views]))

(defn render! []
  (reagent.dom/render [bakermath.views/root]
                      (.getElementById js/document "app")))

(defn ^:dev/after-load refresh! []
  (rf/clear-subscription-cache!)
  (render!))

(defn ^:export init []
  (enable-console-print!)
  (rf/dispatch-sync [::e/init-db])
  (render!))
