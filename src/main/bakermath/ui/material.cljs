(ns bakermath.ui.material
  (:refer-clojure :exclude [list])
  (:require
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   ["@material-ui/core/AppBar" :as app-bar]
   ["@material-ui/core/Button" :as button]
   ["@material-ui/core/List" :as list]
   ["@material-ui/core/ListItem" :as list-item]
   ["@material-ui/core/ListItemText" :as list-item-text]
   ["@material-ui/core/ListSubheader" :as list-subheader]
   ["@material-ui/core/Paper" :as paper]
   ["@material-ui/core/ToolBar" :as tool-bar]
   ["@material-ui/core/Typography" :as typography]))

(def app-bar (interop/react-factory app-bar/default))
(def button (interop/react-factory button/default))
(def list (interop/react-factory list/default))
(def list-item (interop/react-factory list-item/default))
(def list-item-text (interop/react-factory list-item-text/default))
(def list-subheader (interop/react-factory list-subheader/default))
(def paper (interop/react-factory paper/default))
(def tool-bar (interop/react-factory tool-bar/default))
(def typography (interop/react-factory typography/default))
