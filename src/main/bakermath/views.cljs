(ns bakermath.views
  (:require [bakermath.events :as e]
            [bakermath.subs :as sub]
            [bakermath.material-ui :as mui]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]))

(set! *warn-on-infer* true)

(defn debug [x]
  [:pre (with-out-str (pprint x))])

(defn event-value
  [^js/Event e]
  (let [^js/HTMLInputElement el (.-target e)]
    (.-value el)))

(defn dough [i dough]
  [mui/list
   [mui/list-subheader (:dough/name dough)]
   [mui/list-item {:button true
                   :on-click #(rf/dispatch [::e/edit-new-dough-ingredient {:dough-index i}])}
    [mui/list-item-icon [mui/add-icon]]
    [mui/list-item-text "Add ingredient"]]])

(defn dough-list []
  [:div
   (let [doughs @(rf/subscribe [::sub/doughs])]
     (map-indexed (fn [i d] ^{:key i} [dough i d]) doughs))])

(defn dough-ingredient-editor []
  (when-let [editor @(rf/subscribe [::sub/dough-ingredient-editor])]
    (let [mode (:editor/mode editor)
          name (:ingredient/name editor)
          quantity (:dough-ingredient/quantity editor)
          cancel-fn #(rf/dispatch [::e/cancel-dough-ingredient-edit])]
      [mui/dialog
       {:open (:editor/visible editor)
        :on-close cancel-fn}
       [mui/dialog-title {} (if (= mode :new)
                              "Add ingredient"
                              "Edit ingredient")]
       [mui/dialog-content
        [mui/text-field
         {:label "Ingredient"
          :auto-focus (= mode :new)
          :value name
          :on-change #(rf/dispatch [::e/update-dough-ingredient-editor-name
                                    (event-value %)])}]
        [mui/text-field
         {:label "Quantity"
          :type :number
          :auto-focus (= mode :edit)
          :value quantity
          :on-change #(rf/dispatch [::e/update-dough-ingredient-editor-quantity
                                    (event-value %)])}]]
       [mui/dialog-actions
        [mui/button
         {:on-click cancel-fn}
         "Cancel"]
        [mui/button
         {:color "primary"
          :on-click #(rf/dispatch [::e/save-dough-ingredient-edit])}
         "Save"]]])))

(defn app []
  (let [recipe @(rf/subscribe [::sub/recipe])]
    [:div
     [mui/app-bar [mui/toolbar [mui/typography {:variant :h6} "Baker's Math"]]]
     [mui/toolbar]
     [mui/typography {:variant :h3} (:recipe/name recipe)]
     [dough-list]
     [dough-ingredient-editor]]))
