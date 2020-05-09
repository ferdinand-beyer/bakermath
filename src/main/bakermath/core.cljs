(ns bakermath.core
  (:require [reagent.dom]
            [re-frame.core :as rf]
            ; [bakermath.events]
            ; [bakermath.subs]
            [bakermath.views]))

(enable-console-print!)

(defn render! []
  (reagent.dom/render [bakermath.views/app]
                      (.getElementById js/document "app")))

(defn ^:dev/after-load refresh! []
  (rf/clear-subscription-cache!)
  (render!))

(defn ^:export init []
  (render!))
