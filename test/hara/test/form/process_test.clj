(ns hara.test.form.process-test
  (:use hara.test)
  (:require [hara.test.form.process :refer :all]
            [hara.test.common :as common]
            [hara.event :as event]))

^{:refer hara.test.form.process/process :added "3.0"}
(fact "processes a form or a check"
  (defn view-signal [op]
    (let [output (atom nil)]
      (event/with-temp-listener [:test
                                 (fn [{:keys [result]}]
                                   (reset! output (into {} result)))]
        (process op)
        @output)))

  (view-signal (common/op {:type :form
                           :form '(+ 1 2 3)
                           :meta {:line 10 :col 3}}))
  => {:type :success,
      :data 6,
      :form '(+ 1 2 3),
      :from :evaluate,
      :meta {:line 10, :col 3}}

  ((contains {:type :success,
              :data true,
              :checker common/checker?
              :actual 6,
              :from :verify,
              :meta nil})
   (view-signal (common/op {:type :test-equal
                            :input  '(+ 1 2 3)
                            :output 'even?})))
  => true)
