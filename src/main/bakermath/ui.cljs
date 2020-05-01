(ns bakermath.ui
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   ["@material-ui/core/Button" :as mui-button]))

(def mui-button (interop/react-factory mui-button/default))

(defsc Btn [this props]
  (mui-button {:variant "contained"
               :color "primary"}
              (:text props)))

(def btn (comp/factory Btn))

(defsc Root [this props]
  (dom/div (btn {:text "I like my button"})))
