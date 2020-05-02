(ns bakermath.ui.material
  (:refer-clojure :exclude [list])
  (:require
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop :refer [react-factory]]
   ["@material-ui/core/AppBar" :as app-bar]
   ["@material-ui/core/Avatar" :as avatar]
   ["@material-ui/core/Button" :as button]

   ["@material-ui/core/Dialog" :as dialog]
   ["@material-ui/core/DialogActions" :as dialog-actions]
   ["@material-ui/core/DialogContent" :as dialog-content]
   ["@material-ui/core/DialogContentText" :as dialog-content-text]
   ["@material-ui/core/DialogTitle" :as dialog-title]
   
   ["@material-ui/core/Divider" :as divider]
   ["@material-ui/core/IconButton" :as icon-button]
   ["@material-ui/core/InputAdornment" :as input-adornment]

   ["@material-ui/core/List" :as list]
   ["@material-ui/core/ListItem" :as list-item]
   ["@material-ui/core/ListItemAvatar" :as list-item-avatar]
   ["@material-ui/core/ListItemIcon" :as list-item-icon]
   ["@material-ui/core/ListItemSecondaryAction" :as list-item-secondary-action]
   ["@material-ui/core/ListItemText" :as list-item-text]
   ["@material-ui/core/ListSubheader" :as list-subheader]

   ["@material-ui/core/Paper" :as paper]
   ["@material-ui/core/TextField" :as text-field]
   ["@material-ui/core/ToolBar" :as tool-bar]
   ["@material-ui/core/Typography" :as typography]

   ["@material-ui/icons/Add" :as add-icon]
   ["@material-ui/icons/Delete" :as delete-icon]))

(def app-bar (react-factory app-bar/default))
(def avatar (react-factory avatar/default))
(def button (react-factory button/default))

(def dialog (react-factory dialog/default))
(def dialog-actions (react-factory dialog-actions/default))
(def dialog-content (react-factory dialog-content/default))
(def dialog-content-text (react-factory dialog-content-text/default))
(def dialog-title (react-factory dialog-title/default))

(def divider (react-factory divider/default))
(def icon-button (react-factory icon-button/default))
(def input-adornment (react-factory input-adornment/default))

(def list (react-factory list/default))
(def list-item (react-factory list-item/default))
(def list-item-avatar (react-factory list-item-avatar/default))
(def list-item-icon (react-factory list-item-icon/default))
(def list-item-secondary-action (react-factory list-item-secondary-action/default))
(def list-item-text (react-factory list-item-text/default))
(def list-subheader (react-factory list-subheader/default))

(def paper (react-factory paper/default))
(def tool-bar (react-factory tool-bar/default))
(def text-field (react-factory text-field/default))
(def typography (react-factory typography/default))

(def add-icon (react-factory add-icon/default))
(def delete-icon (react-factory delete-icon/default))
