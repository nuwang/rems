(ns rems.layout
  (:require [cheshire.core :as cheshire]
            [hiccup.page :refer [html5 include-css include-js]]
            [rems.context :as context]
            [rems.guide :refer :all]
            [ring.util.http-response :as response]))

(defn external-link []
  [:i {:class "fa fa-external-link"}])

(defn- page-template
  []
  (html5 [:head
          [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
          [:link {:rel "icon" :href "/img/favicon.ico" :type "image/x-icon"}]
          [:link {:rel "shortcut icon" :href "/img/favicon.ico" :type "image/x-icon"}]
          [:title "Welcome to rems"]
          (include-css "/assets/bootstrap/css/bootstrap.min.css")
          (include-css "/assets/font-awesome/css/font-awesome.min.css")
          (include-css "/css/screen.css")
          [:body
           [:div#app]
           ;; TODO remove?
           [:script {:type "text/javascript"} "
var context = {};
var csrfToken = 'not-set';
"]
           (include-js "/assets/jquery/jquery.min.js")
           (include-js "/assets/popper.js/dist/umd/popper.min.js")
           (include-js "/assets/tether/dist/js/tether.min.js")
           (include-js "/assets/bootstrap/js/bootstrap.min.js")
           (include-js "/js/app.js")
           [:script {:type "text/javascript"}
            (format "rems.app.setUser(%s);" (cheshire/generate-string context/*user*))]]]))

(defn render
  "renders HTML generated by Hiccup

   params: :status -- status code to return, defaults to 200
           :headers -- map of headers to return, optional
           :content-type -- optional, defaults to \"text/html; charset=utf-8\""
  [page-name content & [params]]
  (let [content-type (:content-type params "text/html; charset=utf-8")
        status (:status params 200)
        headers (:headers params {})]
    (response/content-type
     {:status status
      :headers headers
      :body (page-template)}
     content-type)))

(defn- error-content
  [error-details]
  [:div.container-fluid
   [:div.row-fluid
    [:div.col-lg-12
     [:div.centering.text-center
      [:div.text-center
       [:h1
        [:span.text-danger (str "Error: " (error-details :status))]
        [:hr]
        (when-let [title (error-details :title)]
          [:h2.without-margin title])
        (when-let [message (error-details :message)]
          [:h4.text-danger message])]]]]]])

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  (render "error page" (error-content error-details) error-details))

(defn guide
  "Component guide fragment"
  []
  (list
   (component-info error-content)
   (example "error-content"
            (error-content {:status 123 :title "Error title" :message "Error message"}))))
