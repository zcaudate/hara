(ns hara.core.base.error-test
  (:use hara.test)
  (:require [hara.core.base.error :refer :all]))

^{:refer hara.core.base.error/suppress :added "3.0"}
(fact "Suppresses any errors thrown in the body."

  (suppress (throw (ex-info "Error" {}))) => nil

  (suppress (throw (ex-info "Error" {})) :error) => :error

  (suppress (throw (ex-info "Error" {}))
            (fn [e]
              (.getMessage e))) => "Error")
