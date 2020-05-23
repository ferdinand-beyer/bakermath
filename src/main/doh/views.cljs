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
  (when-let [{:editor/keys [mode visible?]
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
       {:open visible?
        :on-close cancel-fn
        :max-width :xs
        :full-width true}
       [:form
        {:on-submit (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [::e/save-part-edit]))}
        [mui/dialog-title {} (if (= mode :new)
                               "Add ingredient"
                               "Edit ingredient")]
        [mui/dialog-content
         [mui/grid
          {:container true
           :spacing 2}
          [mui/grid
           {:item true
            :xs 8}
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
                                :autoFocus (= mode :new)
                                :fullWidth true}}]]
          [mui/grid
           {:item true
            :xs 4}
           [mui/text-field
            {:label "Quantity"
             :type :number
             :input-props {:min 0.01
                           :step 0.01}
             :full-width true
             :auto-focus (= mode :edit)
             :value quantity
             :on-change #(rf/dispatch-sync
                          [::e/update-part-editor-quantity
                           (event-value %)])}]]]]
        [mui/dialog-actions
         [mui/button
          {:on-click cancel-fn}
          "Cancel"]
         [mui/button
          {:color :primary
           :type :submit}
          "Save"]]]])))

(def recipe-tab
  (mui/with-styles
    (fn [theme]
      (let [spacing (.spacing theme 2)]
        {:root {}
         :fab {:position :fixed
               :bottom spacing
               :right spacing}}))
    (fn [{:keys [classes]}]
      [:div {:class (:root classes)}
       [mixture-list]
       [part-editor]
       [mui/fab
        {:class (:fab classes)
         :color :secondary}
        [mui/add-icon]]])))

(defn format% [n]
  (str (.toFixed n 2) "%"))

(defn table-tab
  "Renders the 'Table' tab."
  []
  (let [mixtures @(rf/subscribe [::sub/mixture-names])
        ingredient-weights @(rf/subscribe [::sub/ingredient-weights])]
    [mui/table-container
     [mui/table
      [mui/table-head
       [mui/table-row
        [mui/table-cell "Ingredient"]
        (for [{:mixture/keys [index name]} mixtures]
          ^{:key index} [mui/table-cell {:align :right} name])
        [mui/table-cell {:align :right} "Total"]
        [mui/table-cell {:align :right} "Percentage"]]]
      [mui/table-body
       (for [{:keys [id name flour-proportion weights total percentage]}
             ingredient-weights]
         ^{:key id}
         [mui/table-row
          {:hover true}
          [mui/table-cell
           name 
           (when flour-proportion [:strong " (F)"])]
          (for [{:mixture/keys [index]} mixtures]
            ^{:key index}
            [mui/table-cell {:align :right} (get weights index)])
          [mui/table-cell {:align :right} total]
          [mui/table-cell {:align :right} (format% percentage)]])]]]))

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
                     :label "Table"}]]]
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
