(ns hara.print.pretty.edn-test
  (:use hara.test)
  (:require [hara.print.pretty.edn :refer :all]
            [hara.print.pretty :as printer]))

^{:refer hara.print.pretty.edn/override? :added "3.0"}
(fact "implements `hara.protocol.print/IOverride`")

^{:refer hara.print.pretty.edn/edn :added "3.0"}
(fact "converts an object to a tagged literal"

  (edn 1)
  => 1

  (edn nil)
  => nil

  (edn (java.lang.ClassLoader/getPlatformClassLoader))
  => clojure.lang.TaggedLiteral)

^{:refer hara.print.pretty.edn/class->edn :added "3.0"}
(fact "converts a type to edn"

  (class->edn (type (byte-array [])))
  => "[B"

  (class->edn (type (into-array String [])))
  => "[Ljava.lang.String;"

  (class->edn (type :keyword))
  => 'clojure.lang.Keyword)

^{:refer hara.print.pretty.edn/tagged-object :added "3.0"}
(fact "converts a type to a tagged literal"

  (tagged-object (java.lang.ClassLoader/getPlatformClassLoader) :classloader)
  ;;=> #object [jdk.internal.loader.ClassLoaders$PlatformClassLoader "0x73698a00" :classloader]
  => clojure.lang.TaggedLiteral)

^{:refer hara.print.pretty.edn/format-date :added "3.0"}
(fact "helper function for formatting date")

^{:refer hara.print.pretty.edn/visit-seq :added "3.0"}
(fact "creates a form for a seq"

  (visit-seq (printer/canonical-printer)
             [1 2 3 4])
  => [:group "(" [:align ["1" " " "2" " " "3" " " "4"]] ")"])

^{:refer hara.print.pretty.edn/visit-tagged :added "3.0"}
(fact "creates a form for a tagged literal"

  (visit-tagged (printer/canonical-printer)
                (tagged-literal 'hello [1 2 3]))
  => [:span "#hello" " " [:group "[" [:align ["1" " " "2" " " "3"]] "]"]])

^{:refer hara.print.pretty.edn/visit-unknown :added "3.0"}
(fact "creatse a form for an unknown element"

  (visit-unknown (printer/canonical-printer)
                 (Thread/currentThread))
  => throws)

^{:refer hara.print.pretty.edn/visit-meta :added "3.0"}
(fact "creates a form for a meta"
  (visit-meta (printer/canonical-printer)
              {:a 1} {})
  => [:group "{" [:align ()] "}"])

^{:refer hara.print.pretty.edn/visit-edn :added "3.0"}
(fact "creates a form for a non-edn element"

  (visit-edn (printer/canonical-printer)
             (doto (java.util.ArrayList.)
               (.add 1)
               (.add 2)
               (.add 3)))
  => [:group "[" [:align ["1" " " "2" " " "3"]] "]"])

^{:refer hara.print.pretty.edn/visit :added "3.0"}
(fact "a extensible walker for printing `edn`` data"

  (visit (printer/canonical-printer)
         (Thread/currentThread))
  => (contains-in [:span "#object" " " [:group "[" [:align coll?] "]"]]))
