(ns rems.test.applicant-info
  (:require [clojure.test :refer :all]
            [hiccup-find.core :refer :all]
            ;[rems.applicant-info :refer :all]
            [rems.context :as context]
            [rems.test.tempura :refer [with-fake-tempura]]))

;; NOTE: details function is called applicant-info in spa implementation and is located under `rems.application` namespace
#_
(defn find-from-details [pattern]
  (hiccup-find pattern (details "applicant-info" {"eppn" "developer" "commonName" "Deve Loper"})))

#_
(defn children-of [hiccups]
  (remove nil? (mapcat (partial drop 2) hiccups)))

#_
(deftest test-applicant-details
  (with-fake-tempura
    (testing "Info without role information"
      (binding [context/*roles* #{}]
        (is (not-empty (find-from-details [:.card-header])) "Should see collapsible header")
        (is (empty? (children-of (find-from-details [:.collapse-content]))) "Shouldn't see collapsible block")))
    (testing "Info as an applicant"
      (binding [context/*roles* #{:applicant}]
        (is (not-empty (find-from-details [:.card-header])) "Should see collapsible header")
        (is (empty? (children-of (find-from-details [:.collapse-content]))) "Shouldn't see collapsible block")))
    (testing "Info as an approver"
      (binding [context/*roles* #{:approver}]
        (is (not-empty (find-from-details [:.card-header])) "Collapsible header should be visible.")
        (is (not-empty (children-of (find-from-details [:.collapse-content]))) "Collapsible block should be visible.")))
    (testing "Info as a reviewer"
      (binding [context/*roles* #{:reviewer}]
        (is (not-empty (find-from-details [:.card-header])) "Should see collapsible header")
        (is (not-empty (children-of (find-from-details [:.collapse-content]))) "Collapsible block should be visible.")))))
