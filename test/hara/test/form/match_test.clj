(ns hara.test.form.match-test
  (:use hara.test)
  (:require [hara.test.form.match :refer :all]))

^{:refer hara.test.form.match/match-base :added "3.0"}
(fact "determines whether a term matches with a filter"
  (match-base {:tags #{:web}}
              {:tags #{:web}}
              false)
  => [true false false]
  (match-base {:refer 'user/foo
               :namespace 'user}
              {:refers '[user/other]
               :namespaces '[foo bar]}
              true)
  => [true false false])

^{:refer hara.test.form.match/match-include :added "3.0"}
(fact "determines whether inclusion is a match"
  (match-include {:tags #{:web}}
                 {:tags #{:web}})
  => true

  (match-include {:refer 'user/foo
                  :namespace 'user}
                 {})
  => true)

^{:refer hara.test.form.match/match-exclude :added "3.0"}
(fact "determines whether exclusion is a match"
  (match-exclude {:tags #{:web}}
                 {:tags #{:web}})
  => true
  (match-exclude {:refer 'user/foo
                  :namespace 'user}
                 {})
  => false)

^{:refer hara.test.form.match/match-options :added "3.0"}
(fact "determines whether a set of options can match"
  (match-options {:tags #{:web}
                  :refer 'user/foo}
                 {:include [{:tags #{:web}}]
                  :exclude []})
  => true

  (match-options {:tags #{:web}
                  :refer 'user/foo}
                 {:include [{:tags #{:web}}]
                  :exclude [{:refers '[user/foo]}]})
  => false

  (match-options {:tags #{:web}
                  :ns 'user
                  :refer 'user/foo}
                 {:include [{:namespaces [#"us"]}]})
  => true)