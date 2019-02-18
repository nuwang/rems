(ns rems.applications
  (:require [re-frame.core :as rf]
            [rems.application-list :as application-list]
            [rems.spinner :as spinner]
            [rems.text :refer [localize-state localize-time text]]
            [rems.util :refer [fetch]]))

(rf/reg-event-fx
 ::enter-page
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc ::loading-my-applications? true)
            (dissoc ::my-applications))
    ::fetch-my-applications nil}))

;;;; applications

(defn- fetch-my-applications []
  (fetch "/api/applications/" {:handler #(rf/dispatch [::fetch-my-applications-result %])}))

(rf/reg-fx
 ::fetch-my-applications
 (fn [_]
   (fetch-my-applications)))

(rf/reg-event-db
 ::fetch-my-applications-result
 (fn [db [_ applications]]
   (-> db
       (assoc ::my-applications applications)
       (dissoc ::loading-my-applications?))))

(rf/reg-sub
 ::my-applications
 (fn [db _]
   (::my-applications db)))

(rf/reg-sub
 ::loading-my-applications?
 (fn [db _]
   (::loading-my-applications? db)))

;;;; table sorting

(rf/reg-sub
 ::sorting
 (fn [db _]
   (or (::sorting db)
       {:sort-column :created
        :sort-order :desc})))

(rf/reg-event-db ::set-sorting (fn [db [_ sorting]] (assoc db ::sorting sorting)))

(rf/reg-sub ::filtering (fn [db _] (::filtering db)))

(rf/reg-event-db ::set-filtering (fn [db [_ filtering]] (assoc db ::filtering filtering)))

;;;; UI

(defn applications-page []
  (let [apps (rf/subscribe [::my-applications])]
    [:div
     [:h2 (text :t.applications/applications)]
     (cond @(rf/subscribe [::loading-my-applications?])
           [spinner/big]

           (empty? @apps)
           [:div.applications.alert.alert-success (text :t/applications.empty)]

           :else
           [application-list/component
            {:visible-columns application-list/+all-columns+
             :sorting (assoc @(rf/subscribe [::sorting]) :set-sorting #(rf/dispatch [::set-sorting %]))
             :filtering (assoc @(rf/subscribe [::filtering]) :set-filtering #(rf/dispatch [::set-filtering %]))
             :items @apps}])]))
