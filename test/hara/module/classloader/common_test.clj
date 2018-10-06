(ns hara.module.classloader.common-test
  (:use hara.test)
  (:require [hara.module.classloader.common :refer :all]))

^{:refer hara.module.classloader.common/to-url :added "3.0"}
(fact "constructs a `java.net.URL` object from a string"

  (str (to-url "/dev/null"))
  => "file:/dev/null/")
