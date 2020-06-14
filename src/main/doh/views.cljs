(ns doh.views
  (:require [doh.events :as e]
            [doh.subs :as sub]
            [doh.material-ui :as mui]
            [cljs.pprint :refer [pprint]]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [reagent.core :as r]
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

;;;; Recipe view

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
  "Renders the header toolbar for a mixture."
  (mui/with-styles
    {:grow {:flex-grow 1}}
    (fn [{classes :classes
          mixture-id :mixtureId}]
      (let [anchor-el (r/atom nil)
            mixture (rf/subscribe [::sub/mixture mixture-id])
            first? (rf/subscribe [::sub/recipe-mixture-first? mixture-id])
            last? (rf/subscribe [::sub/recipe-mixture-last? mixture-id])]
        (fn [_]
          [:<>
           [mui/tool-bar
            {:class (:grow classes)}
            [mui/typography
             {:class (:grow classes)
              :variant :subtitle1}
             (:mixture/name @mixture)]
            [mui/icon-button
             {:on-click #(rf/dispatch [::e/new-part mixture-id])}
             [mui/add-icon]]
            [mui/icon-button
             {:edge :end
              :on-click #(reset! anchor-el (.-currentTarget %))}
             [mui/more-vert-icon]]]
           (let [close-menu #(reset! anchor-el nil)
                 dispatch (fn [key] #(do (close-menu) (rf/dispatch [key mixture-id])))]
             [mui/menu
              {:open (some? @anchor-el)
               :anchor-el @anchor-el
               :on-close close-menu}
              [mui/menu-item
               {:on-click (dispatch ::e/edit-mixture)}
               "Edit"]
              [mui/menu-item
               {:on-click (dispatch ::e/delete-mixture)}
               "Delete"]
              [mui/menu-item
               {:disabled @first?
                :on-click (dispatch ::e/move-mixture-forward)}
               "Move Up"]
              [mui/menu-item
               {:disabled @last?
                :on-click (dispatch ::e/move-mixture-backward)}
               "Move Down"]])])))))

(defn mixture
  "Renders a mixture as a list of its parts."
  [mixture-id]
  (let [ingredient-ids @(rf/subscribe [::sub/mixture-ingredient-ids mixture-id])]
    [:div
     [mixture-header {:mixture-id mixture-id}]
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

(defn dialog-editor
  "Renders a modal editor."
  [{:keys [visible? title on-save on-cancel]} children]
  [mui/dialog
   {:open visible?
    :on-close on-cancel
    :max-width :xs
    :full-width true}
   [:form
    {:on-submit (fn [e]
                  (.preventDefault e)
                  (on-save))}
    [mui/dialog-title title]
    [mui/dialog-content children]
    [mui/dialog-actions
     [cancel-button {:on-click on-cancel}]
     [save-button]]]])

(defn mixture-editor
  []
  (when-let [{:editor/keys [visible? mixture-id name]}
             @(rf/subscribe [::sub/mixture-editor])]
    [dialog-editor
     {:visible? visible?
      :title (str (if mixture-id "Edit" "New") " Mixture")
      :on-save #(rf/dispatch [::e/save-mixture])
      :on-cancel #(rf/dispatch [::e/cancel-mixture])}
     [mui/text-field
      {:label "Name"
       :full-width true
       :auto-focus true
       :value (:field/input name)
       :error (some? (:field/error name))
       :helper-text (:field/error name)
       :on-change #(rf/dispatch-sync
                    [::e/change-mixture-name (event-value %)])}]]))

(defn part-editor
  "Renders the part editor."
  []
  (when-let [{:editor/keys [visible? ingredient-id name quantity]}
             @(rf/subscribe [::sub/part-editor])]
    [dialog-editor
     {:visible? visible?
      :title (str (if ingredient-id "Edit" "New") " Ingredient")
      :on-save #(rf/dispatch [::e/save-part])
      :on-cancel #(rf/dispatch [::e/cancel-part])}
     [mui/grid
      {:container true
       :spacing 2}
      [mui/grid
       {:item true
        :xs 8}
       [ingredient-input
        {:label "Ingredient"
         :autoFocus (nil? ingredient-id)
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
         :auto-focus (some? ingredient-id)
         :value (:field/input quantity)
         :error (some? (:field/error quantity))
         :helper-text (:field/error quantity)
         :on-change #(rf/dispatch-sync
                      [::e/change-part-quantity (event-value %)])}]]]]))

(def recipe-tab
  (mui/with-styles
    (fn [theme]
      (let [spacing (.spacing theme 2)]
        {:root {:padding-bottom (+ spacing 56)}
         :fab {:position :fixed
               :bottom spacing
               :right spacing}}))
    (fn [{:keys [classes]}]
      [:div {:class (:root classes)}
       [mixture-list]
       [mixture-editor]
       [part-editor]
       [mui/fab
        {:class (:fab classes)
         :color :secondary
         :on-click #(rf/dispatch [::e/new-mixture])}
        [mui/add-icon]]])))

;;;; Table View

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
                     {:padding "16px 24px 16px 16px"
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
     [mui/switch
      {:checked (boolean flour?)
       :on-change #(rf/dispatch [::e/toggle-ingredient-flour ingredient-id])}]]))

(defn part-cell
  [{:keys [mixture-id ingredient-id on-edit-part]}]
  (let [quantity @(rf/subscribe [::sub/part-quantity mixture-id ingredient-id])]
    [table-button-cell
     {:align :right
      :label (or quantity 0)
      :on-click (fn [e]
                  (on-edit-part mixture-id ingredient-id
                                (.-currentTarget e)))}]))

(defn ingredient-total-cell
  [ingredient-id]
  (let [total @(rf/subscribe [::sub/ingredient-total ingredient-id])]
    [mui/table-cell {:align :right} total]))

(defn format% [n]
  (-> (.toFixed n 2)
      (str/replace #"(\.0|(?<=\.\d*[^0])0)0*$" "")
      (str "%")))

(defn ingredient-percentage-cell
  [ingredient-id]
  (let [percentage @(rf/subscribe [::sub/ingredient-percentage ingredient-id])]
    [mui/table-cell {:align :right} (format% percentage)]))

(defn mixture-total-cell
  [{:keys [mixture-id]}]
  (let [total @(rf/subscribe [::sub/mixture-total mixture-id])]
    [mui/table-cell {:align :right} total]))

(defn ingredient-table
  [{:keys [on-edit-part]}]
  (let [mixtures @(rf/subscribe [::sub/recipe-mixtures])
        ingredient-ids @(rf/subscribe [::sub/recipe-ingredient-ids])
        grand-total @(rf/subscribe [::sub/grand-total])
        flour-total @(rf/subscribe [::sub/flour-total])]
    [mui/table-container
     [mui/table
      {:size :small}
      [mui/table-head
       [mui/table-row
        [mui/table-cell "Ingredient"]
        (for [{:mixture/keys [id name]} mixtures]
          ^{:key id} [mui/table-cell {:align :right} name])
        [mui/table-cell {:align :right} "Total"]
        [mui/table-cell {:align :right} "Percent"]
        [mui/table-cell "Flour?"]]]
      [mui/table-body
       (for [ingredient-id ingredient-ids]
         ^{:key ingredient-id}
         [mui/table-row
          {:hover false}
          [ingredient-cell ingredient-id]
          (for [{:mixture/keys [id]} mixtures]
            ^{:key id}
            [part-cell {:mixture-id id 
                        :ingredient-id ingredient-id
                        :on-edit-part on-edit-part}])
          [ingredient-total-cell ingredient-id]
          [ingredient-percentage-cell ingredient-id]
          [ingredient-flour-cell ingredient-id]])]
      [mui/table-footer
       [mui/table-row
        [mui/table-cell]
        (for [{:mixture/keys [id name]} mixtures]
          ^{:key id} [mixture-total-cell {:mixture-id id}])
        [mui/table-cell {:align :right} grand-total]
        [mui/table-cell {:align :right} flour-total]
        [mui/table-cell]]]]]))

(defn quantity-editor
  [{:keys [anchor-el]}]
  (let [{visible? :editor/visible?
         {quantity :field/input} :editor/quantity}
        @(rf/subscribe [::sub/quantity-editor])
        cancel-fn #(rf/dispatch [::e/cancel-quantity])]
    [mui/popover
     {:open (boolean visible?)
      :anchor-el anchor-el
      :anchor-origin {:horizontal :center
                      :vertical :bottom}
      :transform-origin {:horizontal :center
                         :vertical :top}
      :on-close cancel-fn}
     [:form
      {:on-submit (fn [evt]
                    (.preventDefault evt)
                    (rf/dispatch [::e/save-quantity]))}
      [mui/dialog-content
       [mui/text-field
        {:label "Quantity"
         :type :number
         :value quantity
         :on-change #(rf/dispatch-sync [::e/change-quantity (event-value %)])
         :auto-focus true}]]
      [mui/dialog-actions
       [cancel-button {:on-click cancel-fn}]
       [save-button]]]]))

(defn table-tab
  "Renders the 'Table' tab."
  []
  (let [element (r/atom nil)]
    (fn []
      [:<>
       [ingredient-table
        {:on-edit-part
         (fn [mixture-id ingredient-id el]
           (reset! element el)
           (rf/dispatch [::e/edit-quantity mixture-id ingredient-id]))}]
       [quantity-editor {:anchor-el @element}]])))

;;;; App root

(defn undo-snackbar
  []
  (let [ack (r/atom [])
        explanations (rf/subscribe [:undo-explanations])]
    (fn []
      (let [handle-close #(reset! ack @explanations)]
        [mui/snackbar
         {:open (not= @ack @explanations)
          :anchor-origin {:vertical :bottom
                          :horizontal :left}
          :auto-hide-duration 3000
          :on-close handle-close
          :message (last @explanations)
          :action (r/as-element
                   [mui/button
                    {:color :primary
                     :size :small
                     :on-click #(rf/dispatch [:undo])}
                    "Undo"])}]))))

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
           nil)
         [undo-snackbar]]))))

(def theme
  (mui/theme
   {:palette {:primary (mui/color :amber)
              :secondary (mui/color :indigo)}}))

(defn root []
  [:<>
   [mui/css-baseline]
   [mui/theme-provider {:theme theme}
    [app]]])
