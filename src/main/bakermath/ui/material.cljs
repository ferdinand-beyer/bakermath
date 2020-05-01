(ns bakermath.ui.material
  (:require
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   ["@material-ui/core/AppBar" :as app-bar]
   ["@material-ui/core/Button" :as button]
   ["@material-ui/core/Paper" :as paper]
   ["@material-ui/core/ToolBar" :as tool-bar]
   ["@material-ui/core/Typography" :as typography]))

(def app-bar (interop/react-factory app-bar/default))
(def button (interop/react-factory button/default))
(def paper (interop/react-factory paper/default))
(def tool-bar (interop/react-factory tool-bar/default))
(def typography (interop/react-factory typography/default))
