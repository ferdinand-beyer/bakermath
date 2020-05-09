(ns bakermath.views
  (:require [bakermath.material-ui :as mui]))

(defn app []
  [:div
   [mui/app-bar [mui/toolbar [mui/typography {:variant :h6} "Baker's Math"]]]
   [mui/toolbar]
   [mui/typography {:variant :h3} "Hello, World!"]])
