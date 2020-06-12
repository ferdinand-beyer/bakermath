(ns doh.views
  (:require [doh.events :as e]
            [doh.subs :as sub]
            [doh.material-ui :as mui]
            [doh.part-weight-editor :as pw-ed]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [rename-keys]]
            [re-frame.core :as rf]))

(set! *warn-on-infer* true)

(defn debug [& args]
  [:pre (for [x args] (with-out-str (pprint x)))])

(defn event-value
  [^js/Event e]
  (let [^js/HTMLInputElement el (.-target e)]
    (.-value el)))

(defn cancel-button [props]
  [mui/button
   (merge {:color :primary} props)
   "Cancel"])

(defn save-button [props]
  [mui/button
   (merge {:type :submit, :color :primary} props)
   "Save"])

(defn part-list-item
  "Renders a list item for a mixture part."
  [mixture-id ingredient-id]
  (let [quantity @(rf/subscribe [::sub/part-quantity mixture-id ingredient-id])
        ingredient @(rf/subscribe [::sub/ingredient ingredient-id])
        color @(rf/subscribe [::sub/ingredient-avatar-color ingredient-id])
        name (:ingredient/name ingredient)]
    [mui/list-item
     {:button true
      :on-click #(rf/dispatch [::e/edit-part mixture-id ingredient-id])}
     [mui/list-item-avatar
      [mui/avatar
       {:style {:background-color color}}
       (-> name (.substr 0 1) .toUpperCase)]]
     [mui/list-item-text {:primary name
                          :secondary quantity}]
     [mui/list-item-secondary-action
      [mui/icon-button
       {:edge :end
        :on-click #(rf/dispatch [::e/delete-part mixture-id ingredient-id])}
       [mui/delete-icon]]]]))

(def mixture-header
  (mui/with-styles
    {:grow {:flex-grow 1}}
    (fn [{:keys [label classes onAdd]}]
      [mui/tool-bar
       {:class (:grow classes)
        :disable-gutters false}
       [mui/typography
        {:class (:grow classes)
         :variant :subtitle1}
        label]
       [mui/icon-button
        {:on-click onAdd}
        [mui/add-icon]]
       [mui/icon-button
        {:edge :end}
        [mui/more-vert-icon]]])))

(defn mixture
  "Renders a mixture as a list of its parts."
  [mixture-id]
  (let [mixture @(rf/subscribe [::sub/mixture mixture-id])
        ingredient-ids @(rf/subscribe [::sub/mixture-ingredient-ids mixture-id])]
    [:div
     [mixture-header
      {:label (:mixture/name mixture)
       :on-add #(rf/dispatch [::e/new-part mixture-id])}]
     [mui/list
      (for [id ingredient-ids]
        ^{:key id} [part-list-item mixture-id id])]]))

(defn mixture-list
  "Renders a list of mixtures."
  []
  [:<>
   (let [mixture-ids @(rf/subscribe [::sub/recipe-mixture-ids])]
     (for [id mixture-ids]
       ^{:key id} [mixture id]))])

(defn ingredient-input
  "Renders an ingredient selection input control."
  [{:keys [value input-value on-change]
    :as props}]
  (let [options @(rf/subscribe [::sub/ingredient-options])
        id->label (into {} (map (juxt :ingredient/id :ingredient/name) options))]
    ;; TODO: Better interop story...
    [mui/autocomplete
     {:options (clj->js (map :ingredient/id options))
      :get-option-label #(id->label %)
      :value value
      :free-solo true
      :disable-clearable true
      :input-value (or input-value (id->label value) "")
      :on-input-change (fn [evt val _]
                         (when on-change (on-change evt val)))
      :text-field-props (dissoc props :value :input-value :on-change)}]))

(defn part-editor
  "Renders the part editor."
  []
  (when-let [{:editor/keys [visible? mode ingredient-id name quantity]}
             @(rf/subscribe [::sub/part-editor])]
    (let [cancel-fn #(rf/dispatch [::e/cancel-part])]
      [mui/dialog
       {:open visible?
        :on-close cancel-fn
        :max-width :xs
        :full-width true}
       [:form
        {:on-submit (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [::e/save-part]))}
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
           [ingredient-input
            {:label "Ingredient"
             :autoFocus (= mode :new)
             :fullWidth true
             :value ingredient-id
             :input-value (:field/input name)
             :error (some? (:field/error name))
             :helperText (:field/error name)
             :on-change #(rf/dispatch-sync [::e/change-part-name %2])}]]
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
             :value (:field/input quantity)
             :error (some? (:field/error quantity))
             :helper-text (:field/error quantity)
             :on-change #(rf/dispatch-sync
                          [::e/change-part-quantity (event-value %)])}]]]]
        [mui/dialog-actions
         [cancel-button {:on-click cancel-fn}]
         [save-button]]]])))

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

(def table-button-cell
  "Renders a table cell that acts as a button.
   
   Attributes:
   * align
   * button-ref: Get a Ref of the button element
   * on-click"
  (mui/with-styles
   (fn [theme]
     {:cell {:padding 0}
      :button (merge (js->clj (.. theme -typography -body1))
                     {:padding (.spacing theme 2)
                      :width "100%"
                      :justify-content :inherit
                      :text-align :inherit
                      :fontSize :inherit
                      "&:hover" {:background-color (.. theme -palette -action -hover)}})
      :align-right {:justify-content :flex-end}
      :label {}})
   (fn [{:keys [align classes children label]
         :as props}]
     [mui/table-cell
      {:class (:cell classes)
       :align (or align :left)}
      [mui/button-base
       (-> props
           (select-keys [:onClick :buttonRef])
           (rename-keys {:buttonRef :ref})
           (merge {:class [(:button classes)
                           (when (= "right" align)
                             (:align-right classes))]
                   :focus-ripple true}))
       [:span {:class (:label classes)} label]]
      children])))

(defn ingredient-cell
  [ingredient-id]
  (let [{:ingredient/keys [name]}
        @(rf/subscribe [::sub/ingredient ingredient-id])]
    [table-button-cell {:label name}]))

(defn ingredient-flour-cell
  [ingredient-id]
  (let [{:ingredient/keys [flour?]}
        @(rf/subscribe [::sub/ingredient ingredient-id])]
    [mui/table-cell
     [mui/switch  {:checked flour?}]]))

(defn part-cell
  [mixture-id ingredient-id]
  (let [quantity (rf/subscribe [::sub/part-quantity mixture-id ingredient-id])
        part-ident {:mixture-id mixture-id :ingredient-id ingredient-id}
        editor (rf/subscribe [::pw-ed/editor part-ident])
        !element (atom nil)]
    (fn [_]
      (let [{:keys [editing? input]} @editor
            cancel-fn #(rf/dispatch [::pw-ed/cancel-edit part-ident])]
        [table-button-cell
         {:align :right
          :label (or @quantity 0)
          :button-ref #(reset! !element %)
          :on-click #(rf/dispatch [::pw-ed/start-edit part-ident])}
         [mui/popover
          {:open editing?
           :anchor-el @!element
           :anchor-origin {:horizontal :center
                           :vertical :bottom}
           :transform-origin {:horizontal :center
                              :vertical :top}
           :on-close cancel-fn}
          [:form
           {:on-submit (fn [evt]
                         (.preventDefault evt)
                         (rf/dispatch [::pw-ed/save-edit part-ident]))}
           [mui/dialog-content
            [mui/text-field
             {:label "Weight"
              :type :number
              :value input
              :on-change #(rf/dispatch-sync [::pw-ed/enter-weight part-ident (event-value %)])
              :auto-focus true}]]
           [mui/dialog-actions
            [cancel-button {:on-click cancel-fn}]
            [save-button]]]]]))))

(defn ingredient-total-cell
  [ingredient-id]
  (let [total @(rf/subscribe [::sub/ingredient-total ingredient-id])]
    [mui/table-cell {:align :right} total]))

(defn format% [n]
  (str (.toFixed n 2) "%"))

(defn ingredient-percentage-cell
  [ingredient-id]
  (let [percentage @(rf/subscribe [::sub/ingredient-percentage ingredient-id])]
    [mui/table-cell {:align :right} (format% percentage)]))

(defn table-tab
  "Renders the 'Table' tab."
  []
  (let [mixtures @(rf/subscribe [::sub/recipe-mixtures])
        ingredient-ids @(rf/subscribe [::sub/recipe-ingredient-ids])]
    [mui/table-container
     [mui/table
      {:size :small}
      [mui/table-head
       [mui/table-row
        [mui/table-cell "Ingredient"]
        [mui/table-cell "Flour?"]
        (for [{:mixture/keys [id name]} mixtures]
          ^{:key id} [mui/table-cell {:align :right} name])
        [mui/table-cell {:align :right} "Total"]
        [mui/table-cell {:align :right} "Percentage"]]]
      [mui/table-body
       (for [ingredient-id ingredient-ids]
         ^{:key ingredient-id}
         [mui/table-row
          {:hover false}
          [ingredient-cell ingredient-id]
          [ingredient-flour-cell ingredient-id]
          (for [{:mixture/keys [id]} mixtures]
            ^{:key id}
            [part-cell id ingredient-id])
          [ingredient-total-cell ingredient-id]
          [ingredient-percentage-cell ingredient-id]])]]]))

(def app
  (mui/with-styles
    (fn [theme]
      {:root {:flexGrow 1}
       :menuButton {:marginRight (.spacing theme 2)}
       :title {:flexGrow 1}})
    (fn [{:keys [classes]}]
      (let [recipe @(rf/subscribe [::sub/recipe])
            tab @(rf/subscribe [::sub/recipe-tab])]
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
   {:palette {:primary (mui/color :amber)
              :secondary (mui/color :indigo)}}))

(defn root []
  [:<>
   [mui/css-baseline]
   [mui/theme-provider {:theme theme}
    [app]]])
