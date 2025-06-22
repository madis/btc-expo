(ns app.ui.pages.profile
  (:require
    [re-frame.core :as rf]
    [reagent.core :as reagent]
    [oops.core :refer [oget oset!]]))


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

(def last-file (atom nil))

(defn set-image-as-src
  [file el-id]
  (let [el (js/document.getElementById el-id)
        img-url (.createObjectURL js/URL file)]
    (oset! el "src" img-url)
    (oset! el "alt" (.-name file))
    (oset! el "title" (.-name file))))

(defn set-image-as-background
  [file el-id]
  (let [el (js/document.getElementById el-id)
        reader (new js/FileReader)]
    (oset! reader "onloadend"
           (fn []
             (oset! el "style.backgroundImage" (str "url(" (oget reader "result") ")"))))
    (when file (.readAsDataURL reader file))))

(rf/reg-event-db
  :profile/set-name
  (fn [db [_ value]]
    (assoc-in db [:profile :name] value)))

(rf/reg-event-db
  :profile/set-avatar
  (fn [db [_ value]]
    (assoc-in db [:profile :avatar] value)))

(rf/reg-event-db
  :profile/save-success
  (fn [_ _]
    (js/console.log ">>> :profile/save-success")))

(rf/reg-event-db
  :profile/save-failure
  (fn [_ _]
    (js/console.log ">>> :profile/save-failure")))

(rf/reg-event-fx
  :profile/save
  (fn [{:keys [db]} _]
    (let [profile {}
          avatar (get-in db [:profile :avatar])
          form-data (new js/FormData)]
      (.append form-data "avatar-file" avatar)
      (.append form-data "json" (.stringify js/JSON (clj->js {:some-text "Hello" :some-arrays [1 2 3]})))
      {:fx [[:fetch {:method :post
                     :url "http://localhost:3000/api/profile"
                     :mode :cors
                     :body form-data
                     :response-content-type :json
                     :credentials :omit
                     :on-success [:profile/save-success]
                     :on-failure [:profile/save-failure]}]]})))

(rf/reg-sub
  :profile/user-name
  (fn [db _]
    (get-in db [:profile :name])))

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
