(ns hara.deploy.analyser.java-test
  (:use hara.test)
  (:require [hara.deploy.analyser.java :refer :all]
            [hara.deploy.analyser :as analyser]
            [clojure.java.io :as io]))

^{:refer hara.deploy.analyser.java/get-class :added "3.0"}
(fact "grabs the symbol of the class in the java file"
  (get-class
   (io/file "code/java/test/Cat.java"))
  => 'test.Cat)

^{:refer hara.deploy.analyser.java/get-imports :added "3.0"}
(fact "grabs the symbol of the class in the java file"
  (get-imports
   (io/file "code/java/test/Cat.java"))
  => '())
