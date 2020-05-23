(ns doh.material-ui
  (:refer-clojure :exclude [list])
  (:require
   [reagent.core :as r]
   [goog.object :as gobj]

   ["@material-ui/core/AppBar" :as app-bar]
   ["@material-ui/core/Avatar" :as avatar]
   
   ["@material-ui/core/Button" :as button]
   ["@material-ui/core/CssBaseline" :as css-baseline]
   
   ["@material-ui/core/Dialog" :as dialog]
   ["@material-ui/core/DialogActions" :as dialog-actions]
   ["@material-ui/core/DialogContent" :as dialog-content]
   ["@material-ui/core/DialogContentText" :as dialog-content-text]
   ["@material-ui/core/DialogTitle" :as dialog-title]
   
   ["@material-ui/core/Grid" :as grid]

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
   
   ["@material-ui/core/Table" :as table]
   ["@material-ui/core/TableBody" :as table-body]
   ["@material-ui/core/TableCell" :as table-cell]
   ["@material-ui/core/TableContainer" :as table-container]
   ["@material-ui/core/TableFooter" :as table-footer]
   ["@material-ui/core/TableHead" :as table-head]
   ["@material-ui/core/TableRow" :as table-row]
   ["@material-ui/core/TableSortLabel" :as table-sort-label]
   ["@material-ui/core/Tab" :as tab]
   ["@material-ui/core/Tabs" :as tabs]
   ["@material-ui/core/TextField" :as text-field]
   ["@material-ui/core/ToolBar" :as tool-bar]
   ["@material-ui/core/Typography" :as typography]
   
   ["@material-ui/lab/Autocomplete" :as autocomplete]

   ["@material-ui/icons/Add" :as add-icon]
   ["@material-ui/icons/ArrowBack" :as arrow-back-icon]
   ["@material-ui/icons/Delete" :as delete-icon]

   ["@material-ui/core/styles" :refer [ThemeProvider createMuiTheme withStyles]]
   ["@material-ui/core/colors" :as mui-colors]))

;; https://github.com/reagent-project/reagent/blob/master/examples/material-ui/src/example/core.cljs

(set! *warn-on-infer* true)

(defn- map-input-props [props]
  (assoc (dissoc props :inputRef) :ref (:inputRef props)))

(def ^:private -input
  (r/reactify-component
   (fn [props]
     [:input (map-input-props props)])))

(def ^:private -textarea
  (r/reactify-component
   (fn [props]
     [:textarea (map-input-props props)])))

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

(def ^:private -autocomplete (r/adapt-react-class autocomplete/default))

;; TODO: Handle options nicer
(defn autocomplete [props & children]
  (let [tf-props (:text-field-props props)
        render-input (fn [^js params]
                       ;; TODO: These are expected to be camel-case
                       (doseq [[k v] tf-props]
                         (gobj/set params (key->js k) (clj->js v)))
                       (set! (.-inputComponent (.-InputProps params)) -input)
                       (r/create-element text-field/default params))
        props (-> props
                  (dissoc :text-field-props)
                  (assoc :render-input render-input))]
    (into [-autocomplete props] children)))

(def app-bar (r/adapt-react-class app-bar/default))
(def avatar (r/adapt-react-class avatar/default))

(def button (r/adapt-react-class button/default))

(def css-baseline (r/adapt-react-class css-baseline/default))

(def dialog (r/adapt-react-class dialog/default))
(def dialog-actions (r/adapt-react-class dialog-actions/default))
(def dialog-content (r/adapt-react-class dialog-content/default))
(def dialog-content-text (r/adapt-react-class dialog-content-text/default))
(def dialog-title (r/adapt-react-class dialog-title/default))

(def grid (r/adapt-react-class grid/default))

(def icon-button (r/adapt-react-class icon-button/default))

(def list (r/adapt-react-class list/default))
(def list-item (r/adapt-react-class list-item/default))
(def list-item-avatar (r/adapt-react-class list-item-avatar/default))
(def list-item-icon (r/adapt-react-class list-item-icon/default))
(def list-item-secondary-action (r/adapt-react-class list-item-secondary-action/default))
(def list-item-text (r/adapt-react-class list-item-text/default))
(def list-subheader (r/adapt-react-class list-subheader/default))

(def paper (r/adapt-react-class paper/default))

(def table (r/adapt-react-class table/default))
(def table-body (r/adapt-react-class table-body/default))
(def table-cell (r/adapt-react-class table-cell/default))
(def table-container (r/adapt-react-class table-container/default))
(def table-footer (r/adapt-react-class table-footer/default))
(def table-head (r/adapt-react-class table-head/default))
(def table-row (r/adapt-react-class table-row/default))
(def table-sort-label (r/adapt-react-class table-sort-label/default))
(def tab (r/adapt-react-class tab/default))
(def tabs (r/adapt-react-class tabs/default))
(def tool-bar (r/adapt-react-class tool-bar/default))
(def typography (r/adapt-react-class typography/default))

(def add-icon (r/adapt-react-class add-icon/default))
(def arrow-back-icon (r/adapt-react-class arrow-back-icon/default))
(def delete-icon (r/adapt-react-class delete-icon/default))

(def theme-provider (r/adapt-react-class ThemeProvider))

(defn theme
  "Create a theme."
  [options]
  (createMuiTheme (clj->js options)))

(def colors (js->clj mui-colors :keywordize-keys true))

(defn color
  "Return a Material UI color."
  ([hue] (get colors hue))
  ([hue shade] (get-in colors [hue shade])))

(defn decorate
  "Decorate a Reagent component using the higher-order component (HOC) pattern.
   `hoc` needs to be a JavaScript function."
  [hoc component]
  (-> component r/reactify-component hoc r/adapt-react-class))

(defn with-styles
  "Apply Material-UI styles to a Reagent component.  Translates between
   React/JavaScript and Reagent/ClojureScript; styles and classes
   will be ClojureScript maps."
  ([styles]
   (let [hoc (withStyles (if (fn? styles)
                           (fn [theme] (clj->js (styles theme)))
                           (clj->js styles)))]
     (fn [component]
       (->> (fn [{:keys [classes] :as props} & children]
              (apply component
                     (assoc props :classes (js->clj classes :keywordize-keys true))
                     children))
            (decorate hoc)))))
  ([styles component] ((with-styles styles) component)))
