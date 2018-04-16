(ns rems.api.applications

  (:require [compojure.api.sweet :refer :all]
            [rems.api.schema :refer :all]
            [rems.context :as context]
            [rems.db.applications :as applications]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(def GetApplicationsResponse
  [Application])

(def applications-api
  (context "/applications" []
    :tags ["applications"]

    (GET "/" []
      :summary "Get current user's all applications"
      :return GetApplicationsResponse
      (ok (applications/get-my-applications)))))
