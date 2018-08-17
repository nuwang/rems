(ns rems.administration.license
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [rems.collapsible :as collapsible]
            [rems.text :refer [text localize-item]]
            [rems.util :refer [dispatch! fetch put!]]))

(def license-type-link "link")
(def license-type-text "text")

(defn parse-textcontent [form license-type]
  (condp = license-type
    license-type-link (:link form)
    license-type-text (:text form)
    nil))

(defn- build-localization [data license-type]
  {:title (:title data)
   :textcontent (parse-textcontent data license-type)})

(defn- valid-localization? [data]
  (and (not (str/blank? (:title data)))
       (not (str/blank? (:textcontent data)))))

(defn- valid-request? [request languages]
  (and (not (str/blank? (:licensetype request)))
       (= (set languages)
          (set (keys (:localizations request))))
       (every? valid-localization? (vals (:localizations request)))))

(defn build-request [form default-language languages]
  (let [license-type (:licensetype form)
        request {:licensetype license-type
                 :localizations (into {} (map (fn [[lang data]]
                                                [lang (build-localization data license-type)])
                                              (:localizations form)))}]
    (when (valid-request? request languages)
      (localize-item request default-language))))

(defn- create-license [form default-language languages]
  (put! "/api/licenses/create" {:params (build-request form default-language languages)
                                :handler (fn [resp]
                                           (dispatch! "#/administration"))}))

(rf/reg-event-fx
  ::create-license
  (fn [{:keys [db]} [_ form]]
    (create-license form (:default-language db) (:languages db))
    {}))

(rf/reg-event-db
  ::reset-create-license
  (fn [db _]
    (dissoc db ::form)))

(rf/reg-sub
  ::form
  (fn [db _]
    (::form db)))

(rf/reg-event-db
  ::set-form-field
  (fn [db [_ keys value]]
    (assoc-in db (concat [::form] keys) value)))


;;;; UI ;;;;

(defn- language-heading [language]
  [:h2 (str/upper-case (name language))])

(defn- license-title-field [keys]
  (let [form @(rf/subscribe [::form])]
    [:div.form-group.field
     [:label {:for "default-title"} (text :t.create-license/title)]
     [:input.form-control {:type "text"
                           :id "default-title"
                           :name "default-title"
                           :value (get-in form keys)
                           :on-change #(rf/dispatch [::set-form-field keys (.. % -target -value)])}]]))

(defn- license-type-radio-button [value label]
  (let [form @(rf/subscribe [::form])]
    [:div.form-check.form-check-inline
     [:input.form-check-input {:type "radio"
                               :id (str value "-licensetype-field")
                               :name "licensetype-field"
                               :value value
                               :checked (= value (:licensetype form))
                               :on-change #(when (.. % -target -checked)
                                             (rf/dispatch [::set-form-field [:licensetype] value]))}]
     [:label.form-check-label {:for (str value "-licensetype-field")} label]]))

(defn- license-type-radio-group []
  [:div.form-group.field
   [license-type-radio-button license-type-link (text :t.create-license/external-link)]
   [license-type-radio-button license-type-text (text :t.create-license/inline-text)]])

(defn- license-link-field [keys]
  (let [form @(rf/subscribe [::form])]
    (when (= license-type-link (:licensetype form))
      [:div.form-group.field
       [:label {:for "link-field"} (text :t.create-license/link-to-license)]
       [:input.form-control {:type "text"
                             :id "link-field"
                             :name "link-field"
                             :placeholder "https://example.com/license"
                             :value (get-in form keys)
                             :on-change #(rf/dispatch [::set-form-field keys (.. % -target -value)])}]])))

(defn- license-text-field [keys]
  (let [form @(rf/subscribe [::form])]
    (when (= license-type-text (:licensetype form))
      [:div.form-group.field
       [:label {:for "text-field"} (text :t.create-license/license-text)]
       [:textarea.form-control {:type "text"
                                :id "text-field"
                                :name "text-field"
                                :value (get-in form keys)
                                :on-change #(rf/dispatch [::set-form-field keys (.. % -target -value)])}]])))

(defn- save-license-button []
  (let [form @(rf/subscribe [::form])
        default-language @(rf/subscribe [:default-language])
        languages @(rf/subscribe [:languages])]
    [:button.btn.btn-primary
     {:on-click #(rf/dispatch [::create-license form])
      :disabled (not (build-request form default-language languages))}
     (text :t.create-resource/save)]))                      ; TODO: rename translation key

(defn- cancel-button []
  [:button.btn.btn-secondary
   {:on-click #(dispatch! "/#/administration")}
   (text :t.create-catalogue-item/cancel)])                 ; TODO: rename translation key

(defn create-license-page []
  (let [default-language (rf/subscribe [:default-language])
        languages (rf/subscribe [:languages])]
    (fn []
      [collapsible/component
       {:id "create-license"
        :title (text :t.navigation/create-license)
        :always [:div
                 [language-heading @default-language]
                 [license-title-field [:localizations @default-language :title]]
                 [license-type-radio-group]
                 [license-link-field [:localizations @default-language :link]]
                 [license-text-field [:localizations @default-language :text]]

                 (for [language (remove #(= % @default-language) @languages)]
                   [:div {:key language}
                    [language-heading language]
                    [license-title-field [:localizations language :title]]
                    [license-link-field [:localizations language :link]]
                    [license-text-field [:localizations language :text]]])

                 [:div.col.commands
                  [cancel-button]
                  [save-license-button]]]}])))
