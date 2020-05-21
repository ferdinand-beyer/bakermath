(ns doh.views
  (:require [doh.events :as e]
            [doh.subs :as sub]
            [doh.material-ui :as mui]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]))

(set! *warn-on-infer* true)

(def indexed (partial map vector (range)))

(defn debug [x]
  [:pre (with-out-str (pprint x))])

(defn event-value
  [^js/Event e]
  (let [^js/HTMLInputElement el (.-target e)]
    (.-value el)))

;; TODO compute in sub!
(defn avatar-color [name]
  (str "hsl(" (-> name hash (mod 360)) ", 70%, 60%"))

(defn part-item [{:keys [mixture-index part-index part]}]
  (let [name (:ingredient/name part)
        quantity (:part/quantity part)]
    [mui/list-item
     {:button true
      :on-click #(rf/dispatch [::e/edit-part
                               {:mixture-index mixture-index
                                :part-index part-index}])}
     [mui/list-item-avatar
      [mui/avatar
       {:style {:background-color (avatar-color name)}}
       (-> name (.substr 0 1) .toUpperCase)]]
     [mui/list-item-text {:primary name
                          :secondary quantity}]
     [mui/list-item-secondary-action
      [mui/icon-button
       {:edge :end
        :on-click #(rf/dispatch [::e/delete-part {:mixture-index mixture-index
                                                  :part-index part-index}])}
       [mui/delete-icon]]]]))

(defn mixture [{:keys [mixture-index mixture]}]
  (let [parts @(rf/subscribe [::sub/parts mixture-index])]
    [mui/list
     [mui/list-subheader (:mixture/name mixture)]
     (for [[i p] (indexed parts)]
       ^{:key i} [part-item {:mixture-index mixture-index
                             :part-index i
                             :part p}])
     [mui/list-item
      {:button true
       :on-click #(rf/dispatch [::e/edit-new-part
                                {:mixture-index mixture-index}])}
      [mui/list-item-icon [mui/add-icon]]
      [mui/list-item-text "Add ingredient"]]]))

(defn mixture-list []
  [:<>
   (let [mixtures @(rf/subscribe [::sub/mixtures])]
     (for [[i m] (indexed mixtures)]
       ^{:key i} [mixture {:mixture-index i, :mixture m}]))])

(defn part-editor []
  (when-let [editor @(rf/subscribe [::sub/part-editor])]
    (let [mode (:editor/mode editor)
          name (:ingredient/name editor)
          quantity (:part/quantity editor)
          cancel-fn #(rf/dispatch [::e/cancel-part-edit])]
      [mui/dialog
       {:open (:editor/visible editor)
        :on-close cancel-fn}
       [:form
        {:on-submit (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [::e/save-part-edit]))}
        [mui/dialog-title {} (if (= mode :new)
                               "Add ingredient"
                               "Edit ingredient")]
        [mui/dialog-content
         [mui/text-field
          {:label "Ingredient"
           :auto-focus (= mode :new)
           :value name
           :on-change #(rf/dispatch-sync
                        [::e/update-part-editor-name
                         (event-value %)])}]
         [mui/text-field
          {:label "Quantity"
           :type :number
           :auto-focus (= mode :edit)
           :value quantity
           :on-change #(rf/dispatch-sync
                        [::e/update-part-editor-quantity
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
   [mixture-list]
   [part-editor]])

(defn table-tab []
  (let [{:keys [columns data]} @(rf/subscribe [::sub/table])]
    [mui/table-container
     [mui/table
      [mui/table-head
       [mui/table-row
        (for [[i {:keys [label]}] (indexed columns)]
          ^{:key i} [mui/table-cell label])]]
      [mui/table-body
       (for [[i cells] (indexed data)]
         ^{:key i}
         [mui/table-row
          (for [[i label] (indexed cells)]
            ^{:key i} [mui/table-cell label])])]]]))

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
           :table [table-tab]
           nil)]))))

(def theme
  (mui/theme
   {:palette {:primary (mui/color :amber)}}))

(defn root []
  [:<>
   [mui/css-baseline]
   [mui/theme-provider {:theme theme}
    [app]]])
