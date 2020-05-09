(ns bakermath.views
  (:require [bakermath.subs :as sub]
            [bakermath.material-ui :as mui]
            [re-frame.core :as rf]))

(defn dough [dough]
  [mui/list
   [mui/list-subheader (:dough/name dough)]
   [mui/list-item {:button true}
    [mui/list-item-icon [mui/add-icon]]
    [mui/list-item-text "Add ingredient"]]])

(defn dough-list []
  [:div
   (for [d @(rf/subscribe [::sub/doughs])]
     ^{:key (:dough/id d)} [dough d])])

(defn app []
  (let [recipe @(rf/subscribe [::sub/recipe])]
    [:div
     [mui/app-bar [mui/toolbar [mui/typography {:variant :h6} "Baker's Math"]]]
     [mui/toolbar]
     [mui/typography {:variant :h3} (:recipe/name recipe)]
     [dough-list]]))
