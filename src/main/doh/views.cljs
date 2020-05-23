(ns doh.views
  (:require [doh.events :as e]
            [doh.subs :as sub]
            [doh.material-ui :as mui]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]))

(set! *warn-on-infer* true)

(def indexed (partial map vector (range)))

(defn debug [& args]
  [:pre (for [x args] (with-out-str (pprint x)))])

(defn event-value
  [^js/Event e]
  (let [^js/HTMLInputElement el (.-target e)]
    (.-value el)))

(defn part-list-item
  "Renders a list item for a mixture part."
  [{:keys [mixture-index part-index part]}]
  (let [ingredient-id (:part/ingredient-id part)
        ingredient @(rf/subscribe [::sub/ingredient ingredient-id])
        name (:ingredient/name ingredient)
        quantity (:part/quantity part)]
    [mui/list-item
     {:button true
      :on-click #(rf/dispatch [::e/edit-part
                               {:mixture-index mixture-index
                                :part-index part-index}])}
     [mui/list-item-avatar
      [mui/avatar
       {:style {:background-color (:avatar/color ingredient)}}
       (-> name (.substr 0 1) .toUpperCase)]]
     [mui/list-item-text {:primary name
                          :secondary quantity}]
     [mui/list-item-secondary-action
      [mui/icon-button
       {:edge :end
        :on-click #(rf/dispatch [::e/delete-part {:mixture-index mixture-index
                                                  :part-index part-index}])}
       [mui/delete-icon]]]]))

(defn mixture
  "Renders a mixture as a list of its parts."
  [{:keys [mixture-index mixture]}]
  (let [parts @(rf/subscribe [::sub/parts mixture-index])]
    [mui/list
     [mui/list-subheader (:mixture/name mixture)]
     (for [[i p] (indexed parts)]
       ^{:key i} [part-list-item {:mixture-index mixture-index
                                  :part-index i
                                  :part p}])
     [mui/list-item
      {:button true
       :on-click #(rf/dispatch [::e/edit-new-part
                                {:mixture-index mixture-index}])}
      [mui/list-item-icon [mui/add-icon]]
      [mui/list-item-text "Add ingredient"]]]))

(defn mixture-list
  "Renders a list of mixtures."
  []
  [:<>
   (let [mixtures @(rf/subscribe [::sub/mixtures])]
     (for [[i m] (indexed mixtures)]
       ^{:key i} [mixture {:mixture-index i, :mixture m}]))])

(defn part-editor
  "Renders the part editor."
  []
  (when-let [{:editor/keys [mode visible]
              :part/keys [ingredient-id quantity]
              :ingredient/keys [name]}
             @(rf/subscribe [::sub/part-editor])]
    (let [ingredients @(rf/subscribe [::sub/ingredients])
          cancel-fn #(rf/dispatch [::e/cancel-part-edit])
          options (->> ingredients
                       (sort-by #(:ingredient/name (val %)))
                       (map key)
                       clj->js)
          option-fn #(:ingredient/name (get ingredients %))]
      [mui/dialog
       {:open visible
        :on-close cancel-fn}
       [:form
        {:on-submit (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [::e/save-part-edit]))}
        [mui/dialog-title {} (if (= mode :new)
                               "Add ingredient"
                               "Edit ingredient")]
        [mui/dialog-content
         [mui/autocomplete
          ;; TODO: Better interop story...
          {:options options
           :get-option-label option-fn
           :value ingredient-id
           :free-solo true
           :disable-clearable true
           :input-value (or name "")
           :on-input-change (fn [_ val _]
                              (rf/dispatch-sync [::e/update-part-editor-name val]))
           :text-field-props {:label "Ingredient"
                              :autoFocus (= mode :new)}}]
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

(defn recipe-tab
  "Renders the 'Recipe' tab."
  []
  [:<>
   [mixture-list]
   [part-editor]])

(defn table-tab
  "Renders the 'Table' tab."
  []
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
