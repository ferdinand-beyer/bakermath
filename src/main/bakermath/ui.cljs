(ns bakermath.ui
  (:require
   [bakermath.ui.material :as material]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]))

(defsc AppBar [this props]
  (material/app-bar
   {}
   (material/tool-bar
    {}
    (material/typography {:variant "h6"} "Baker's Math"))))

(def app-bar (comp/factory AppBar))

(defsc Button [this props]
  (material/button {:variant "contained"
                    :color "primary"
                    :onClick (:act props)}
                   (:text props)))

(def button (comp/factory Button))

(defsc Root [this props]
  (dom/div
   (app-bar)
   (dom/div {:style {:margin-top "84px"}}
    (button {:text "I like my button..."
             :act #(println "Clicked")}))))
