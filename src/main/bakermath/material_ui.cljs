(ns bakermath.material-ui
  (:refer-clojure :exclude [list])
  (:require
   [reagent.core :as r]

   ["@material-ui/core/AppBar" :as app-bar]
   ["@material-ui/core/Avatar" :as avatar]
   
   ["@material-ui/core/Button" :as button]
   ["@material-ui/core/CssBaseline" :as css-baseline]
   
   ["@material-ui/core/Dialog" :as dialog]
   ["@material-ui/core/DialogActions" :as dialog-actions]
   ["@material-ui/core/DialogContent" :as dialog-content]
   ["@material-ui/core/DialogContentText" :as dialog-content-text]
   ["@material-ui/core/DialogTitle" :as dialog-title]

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
   
   ["@material-ui/core/Tab" :as tab]
   ["@material-ui/core/Tabs" :as tabs]
   ["@material-ui/core/TextField" :as text-field]
   ["@material-ui/core/ToolBar" :as tool-bar]
   ["@material-ui/core/Typography" :as typography]
   
   ["@material-ui/lab/Autocomplete" :as autocomplete]

   ["@material-ui/icons/Add" :as add-icon]
   ["@material-ui/icons/ArrowBack" :as arrow-back-icon]
   ["@material-ui/icons/Delete" :as delete-icon]

   ["@material-ui/core/styles" :refer [createMuiTheme withStyles]]
   ["@material-ui/core/colors" :as mui-colors]))

;; https://github.com/reagent-project/reagent/blob/master/examples/material-ui/src/example/core.cljs

(set! *warn-on-infer* true)

(def ^:private -input
  (r/reactify-component
   (fn [props]
     [:input (-> props
                 (assoc :ref (:inputRef props))
                 (dissoc :inputRef))])))

(def ^:private -textarea
  (r/reactify-component
   (fn [props]
     [:textarea (-> props
                    (assoc :ref (:inputRef props))
                    (dissoc :inputRef))])))

(def ^:private -text-field (r/adapt-react-class text-field/default))

(defn text-field [props & children]
  (let [input (cond
                (and (:multiline props)
                     (:rows props)
                     (not (:maxRows props))) -textarea
                (:multiline props) nil
                (:select props) nil
                :else -input)]
    (into
     [-text-field (assoc-in props [:InputProps :inputComponent] input)]
     children)))

(def app-bar (r/adapt-react-class app-bar/default))
(def avatar (r/adapt-react-class avatar/default))

(def button (r/adapt-react-class button/default))

(def css-baseline (r/adapt-react-class css-baseline/default))

(def dialog (r/adapt-react-class dialog/default))
(def dialog-actions (r/adapt-react-class dialog-actions/default))
(def dialog-content (r/adapt-react-class dialog-content/default))
(def dialog-content-text (r/adapt-react-class dialog-content-text/default))
(def dialog-title (r/adapt-react-class dialog-title/default))

(def icon-button (r/adapt-react-class icon-button/default))

(def list (r/adapt-react-class list/default))
(def list-item (r/adapt-react-class list-item/default))
(def list-item-avatar (r/adapt-react-class list-item-avatar/default))
(def list-item-icon (r/adapt-react-class list-item-icon/default))
(def list-item-secondary-action (r/adapt-react-class list-item-secondary-action/default))
(def list-item-text (r/adapt-react-class list-item-text/default))
(def list-subheader (r/adapt-react-class list-subheader/default))

(def paper (r/adapt-react-class paper/default))

(def tab (r/adapt-react-class tab/default))
(def tabs (r/adapt-react-class tabs/default))
(def tool-bar (r/adapt-react-class tool-bar/default))
(def typography (r/adapt-react-class typography/default))

(def add-icon (r/adapt-react-class add-icon/default))
(def arrow-back-icon (r/adapt-react-class arrow-back-icon/default))
(def delete-icon (r/adapt-react-class delete-icon/default))

(defn decorate
  "Decorate a Reagent component using the higher-order component pattern.
   `decorator` needs to be a JavaScript function."
  [decorator component]
  (-> component r/reactify-component decorator r/adapt-react-class))

(defn with-styles
  "Apply Material-UI styles to a Reagent component.  Translates between
   React / JavaScript and Reagent / ClojureScript; styles and classes
   will be ClojureScript maps."
  ([styles] (partial with-styles styles))
  ([styles component]
   (decorate
    (withStyles (if (fn? styles)
                  (fn [theme] (clj->js (styles theme)))
                  (clj->js styles)))
    (fn [{:keys [classes] :as props} & children]
      (apply component
             (assoc props :classes (js->clj classes :keywordize-keys true))
             children)))))
