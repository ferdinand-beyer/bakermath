(ns bakermath.material-ui
  (:refer-clojure :exclude [list])
  (:require
   [reagent.core :as r]
   [reagent.impl.template :as rtpl]
   ["@material-ui/core" :as mui]
   ["@material-ui/core/styles" :refer [createMuiTheme withStyles]]
   ["@material-ui/core/colors" :as mui-colors]
   ["@material-ui/icons" :as mui-icons]
   ["@material-ui/lab" :refer [Autocomplete]]))

;; https://github.com/reagent-project/reagent/blob/master/examples/material-ui/src/example/core.cljs

(set! *warn-on-infer* true)

(defn event-value
  [^js/Event e]
  (let [^js/HTMLInputElement el (.-target e)]
    (.-value el)))

(def ^:private input-component
  (r/reactify-component
   (fn [props]
     [:input (-> props
                 (assoc :ref (:inputRef props))
                 (dissoc :inputRef))])))

(def ^:private textarea-component
  (r/reactify-component
   (fn [props]
     [:textarea (-> props
                    (assoc :ref (:inputRef props))
                    (dissoc :inputRef))])))

(defn text-field [props & children]
  (let [props (-> props
                  (assoc-in [:InputProps :inputComponent]
                            (cond
                              (and (:multiline props) (:rows props) (not (:maxRows props)))
                              textarea-component

                              (:multiline props)
                              nil

                              (:select props)
                              nil

                              :else
                              input-component))
                  rtpl/convert-prop-value)]
    (apply r/create-element mui/TextField props (map r/as-element children))))

(def app-bar (r/adapt-react-class mui/AppBar))

(def list (r/adapt-react-class mui/List))
(def list-item (r/adapt-react-class mui/ListItem))
(def list-item-icon (r/adapt-react-class mui/ListItemIcon))
(def list-item-text (r/adapt-react-class mui/ListItemText))
(def list-subheader (r/adapt-react-class mui/ListSubheader))

(def toolbar (r/adapt-react-class mui/Toolbar))
(def typography (r/adapt-react-class mui/Typography))

(def add-icon (r/adapt-react-class mui-icons/Add))
