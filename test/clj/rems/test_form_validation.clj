(ns rems.test-form-validation
  (:require [clojure.test :refer :all]
            [rems.form-validation :refer [validate-fields]]))

(deftest test-validate-fields
  (testing "all fields filled"
    (is (nil? (validate-fields [{:field/title "A"
                                 :field/optional false
                                 :field/value "a"}]))))


  (testing "optional fields"
    (is (nil? (validate-fields [{:field/title "A"
                                 :field/optional true
                                 :field/value nil}
                                {:field/title "B"
                                 :field/optional true
                                 :field/value ""}]))))

  (testing "error: field required"
    (is (= [{:type :t.form.validation/required :field-id 2}
            {:type :t.form.validation/required :field-id 3}]
           (validate-fields [{:field/id 1
                              :field/optional true
                              :field/value nil}
                             {:field/id 2
                              :field/optional false
                              :field/value ""}
                             {:field/id 3
                              :field/optional false
                              :field/value nil}]))))

  (testing "error: field input too long"
    (is (= [{:type :t.form.validation/toolong :field-id 2}]
           (validate-fields [{:field/id 1
                              :field/maxlength 5
                              :field/value "abcde"}
                             {:field/id 2
                              :field/maxlength 5
                              :field/value "abcdef"}])))))
