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

;; TODO compute in sub!
(defn avatar-color [name]
  (str "hsl(" (-> name hash (mod 360)) ", 70%, 60%"))

(defn dough-ingredient [{:keys [index ingredient dough-index]}]
  (let [name (:ingredient/name ingredient)
        quantity (:dough-ingredient/quantity ingredient)]
    [mui/list-item
     {:button true}
     [mui/list-item-avatar
      [mui/avatar
       {:style {:background-color (avatar-color name)}}
       (-> name (.substr 0 1) .toUpperCase)]]
     [mui/list-item-text {:primary name
                          :secondary quantity}]
     [mui/list-item-secondary-action
      [mui/icon-button
       {:edge :end
        :on-click #(rf/dispatch [::e/delete-dough-ingredient dough-index index])}
       [mui/delete-icon]]]]))

(defn dough [{:keys [index dough]}]
  (let [ingredients @(rf/subscribe [::sub/dough-ingredients index])]
    [mui/list
     [mui/list-subheader (:dough/name dough)]
     (map-indexed (fn [i d]
                    ^{:key i} [dough-ingredient {:dough-index index
                                                 :index i
                                                 :ingredient d}])
                  ingredients)
     [mui/list-item
      {:button true
       :on-click #(rf/dispatch [::e/edit-new-dough-ingredient
                                {:dough-index index}])}
      [mui/list-item-icon [mui/add-icon]]
      [mui/list-item-text "Add ingredient"]]]))

(defn dough-list []
  [:<>
   (let [doughs @(rf/subscribe [::sub/doughs])]
     (map-indexed (fn [i d] ^{:key i} [dough {:index i, :dough d}])
                  doughs))])

(defn dough-ingredient-editor []
  (when-let [editor @(rf/subscribe [::sub/dough-ingredient-editor])]
    (let [mode (:editor/mode editor)
          name (:ingredient/name editor)
          quantity (:dough-ingredient/quantity editor)
          cancel-fn #(rf/dispatch [::e/cancel-dough-ingredient-edit])]
      [mui/dialog
       {:open (:editor/visible editor)
        :on-close cancel-fn}
       [:form
        {:on-submit (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [::e/save-dough-ingredient-edit]))}
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
          {:color :primary
           :type :submit}
          "Save"]]]])))

(defn recipe-tab []
  [:<>
   [dough-list]
   [dough-ingredient-editor]])

(def app
  (mui/with-styles
    (fn [theme]
      {:root {:flexGrow 1}
       :menuButton {:marginRight (.spacing theme 2)}
       :title {:flexGrow 1}})
    (fn [{:keys [classes]}]
      (let [recipe @(rf/subscribe [::sub/recipe])
            tab (or (:recipe/tab recipe) :recipe)]
        [:div {:class (:root classes)}
         [mui/app-bar
          {:position :sticky}
          [mui/tool-bar
           [mui/icon-button
            {:edge :start
             :class (:menuButton classes)
             :color :inherit}
            [mui/arrow-back-icon]]
           [mui/typography
            {:variant :h6
             :class (:title classes)}
            (:recipe/name recipe)]]
          [mui/tabs
           {:centered true
            :value tab
            :on-change #(rf/dispatch [::e/select-recipe-tab {:tab (keyword %2)}])}
           [mui/tab {:value :recipe
                     :label "Recipe"}]
           [mui/tab {:value :table
                     :label "Table"}]
           [mui/tab {:value :ingredients
                     :label "Ingredients"}]]]
         (case tab
           :recipe [recipe-tab]
           nil)]))))

(def theme
  (mui/theme
   {:palette {:primary (mui/color :amber)}}))

(defn root []
  [:<>
   [mui/css-baseline]
   [mui/theme-provider {:theme theme}
    [app]]])
