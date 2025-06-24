(ns app.ui.pages.profile
  (:require
    [re-frame.core :as rf]
    [reagent.core :as reagent]
    [oops.core :refer [oget oset!]]
    [app.ui.pages.profile.events]
    [app.ui.pages.profile.subscriptions]))


(defn file-upload-inner
  [{:keys [id name on-cancel on-change] :as props}]
  (let [upload-el (atom nil)
        component-update (fn [component] (js/console.log ">>> file-upload.update" component))
        wrapped-change (fn [ev] (on-change (array-seq (oget ev "target.files"))))]
    ; https://reagent-project.github.io/docs/master/reagent.core.html#var-create-class
    (reagent/create-class
      {:reagent-render
       (fn []
         [:input.file-input
         {:type :file
          :id id
          :name "avatar-file"}])
       :component-did-mount
       (fn [component]
         (js/console.log ">>> component-did-mount" component)
         (reset! upload-el (js/document.getElementById id))
         (.addEventListener @upload-el "cancel" on-cancel)
         (.addEventListener @upload-el "change" wrapped-change))
       :component-did-update component-update})))

(defn file-upload
  [{:keys [id on-cancel on-change] :as props}]
  (let []
    [file-upload-inner props]))

(defn set-image-as-src
  [file el-id]
  (let [el (js/document.getElementById el-id)
        img-url (.createObjectURL js/URL file)]
    (oset! el "src" img-url)
    (oset! el "alt" (.-name file))
    (oset! el "title" (.-name file))))

(defn show
  []
  (let [file-name (reagent/atom "Waiting for selection")
        on-change (fn [files]
                    (js/console.log ">>> on-change file name" (.-name (first files)))
                    (reset! file-name (.-name (first files)))
                    (set-image-as-src (first files) "avatar-preview")
                    (rf/dispatch [:profile/set-avatar (first files)]))
        user-name (rf/subscribe [:profile/user-name])]
    (fn []
      [:form
       [:div.field
        [:label.label "Name"]
        [:div.control
         [:input.input {:type :text
                        :value @user-name
                        :on-change #(rf/dispatch [:profile/set-name (oget % "target.value")])}]]]
       [:div.field
        [:label.label "Avatar"]
        [:div {:class [:file :has-name :is-boxed]}
         [:label.file-label
          [file-upload {:id "avatar-upload"
                        :name "avatar-file"
                        :on-cancel (fn [x] (js/console.log ">>> addEventListener#CANCEL" x))
                        :on-change on-change}]
          [:span.file-cta
           [:span.file-icon
            [:i.fas.fa-upload]]
           [:span.file-label "Choose a file..."]]
          [:span.file-name @file-name]]]]

       [:figure {:class [:image :is-128x128]}
        [:img {:id "avatar-preview" }]]

       [:button.button.is-primary {:on-click (fn [e]
                                               (.preventDefault e)
                                               (js/console.log "Button clicked" @user-name)
                                               (rf/dispatch [:profile/save]))} "Save!"]])))
