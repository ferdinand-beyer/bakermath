(ns bakermath.ui.material
  (:refer-clojure :exclude [list])
  (:require
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   ["@material-ui/core/AppBar" :as app-bar]
   ["@material-ui/core/Button" :as button]
   ["@material-ui/core/IconButton" :as icon-button]
   ["@material-ui/core/List" :as list]
   ["@material-ui/core/ListItem" :as list-item]
   ["@material-ui/core/ListItemSecondaryAction" :as list-item-secondary-action]
   ["@material-ui/core/ListItemText" :as list-item-text]
   ["@material-ui/core/ListSubheader" :as list-subheader]
   ["@material-ui/core/Paper" :as paper]
   ["@material-ui/core/ToolBar" :as tool-bar]
   ["@material-ui/core/Typography" :as typography]
   ["@material-ui/icons/Delete" :as delete-icon]))

(def app-bar (interop/react-factory app-bar/default))
(def button (interop/react-factory button/default))
(def icon-button (interop/react-factory icon-button/default))
(def list (interop/react-factory list/default))
(def list-item (interop/react-factory list-item/default))
(def list-item-secondary-action (interop/react-factory list-item-secondary-action/default))
(def list-item-text (interop/react-factory list-item-text/default))
(def list-subheader (interop/react-factory list-subheader/default))
(def paper (interop/react-factory paper/default))
(def tool-bar (interop/react-factory tool-bar/default))
(def typography (interop/react-factory typography/default))

(def delete-icon (interop/react-factory delete-icon/default))
