(ns hara.print.pretty-test
  (:use hara.test)
  (:require [hara.print.pretty :refer :all]))

^{:refer hara.print.pretty/format-unknown :added "3.0"}
(fact "custom printer for an unknown type"

  (format-unknown (canonical-printer) :hello)
  => (contains-in [:span "#<" "clojure.lang.Keyword" "@" string? '(" " ":hello") ">"]))

^{:refer hara.print.pretty/format-doc-edn :added "3.0"}
(fact "provides a meta-less print formatter"

  (format-doc-edn (pretty-printer {}) :hello)
  => [:span [:pass "[34m"] ":hello" [:pass "[0m"]])

^{:refer hara.print.pretty/format-doc :added "3.0"}
(fact "provides a format given a printer and value"

  (format-doc (canonical-printer) :hello)
  => ":hello"

  (format-doc (pretty-printer {}) :hello)
  => [:span [:pass "[34m"] ":hello" [:pass "[0m"]])

^{:refer hara.print.pretty/pr-handler :added "3.0"}
(fact "creates a print handler for printing strings"

  (pr-handler (canonical-printer) [1 2 3 4])
  => "[1 2 3 4]")

^{:refer hara.print.pretty/unknown-handler :added "3.0"}
(fact "creates a custom handler for an unknown object"

  (unknown-handler (canonical-printer) (Thread/currentThread))
  => throws)

^{:refer hara.print.pretty/tagged-handler :added "3.0"}
(fact "creates a custom handler for a tagged literal"

  ((tagged-handler 'object (fn [x] (map inc x)))
   (canonical-printer {})
   [1 2 3 4])
  => [:span "#object" " " [:group "(" [:align '("2" " " "3" " " "4" " " "5")] ")"]])

^{:refer hara.print.pretty/canonical-printer :added "3.0"}
(fact "constructs a canonical printer"

  (canonical-printer {})
  => hara.print.pretty.CanonicalPrinter)

^{:refer hara.print.pretty/pretty-printer :added "3.0"}
(fact "constructs a pretty printer"

  (pretty-printer {})
  => hara.print.pretty.PrettyPrinter)

^{:refer hara.print.pretty/render-out :added "3.0"}
(fact "helper to pprint and pprint-str"

  (with-out-str
    (render-out (canonical-printer)
                {:a 1 :b 2 :c (range 5)}))
  => "{:a 1 :b 2 :c (0 1 2 3 4)}")

^{:refer hara.print.pretty/pprint :added "3.0"}
(fact "pretty prints with options"

  (pprint {:a 1 :b 2 :c (range 5)}
          {:width 10}))

^{:refer hara.print.pretty/pprint-str :added "3.0"}
(fact "returns the string that is printed"

  (pprint-str {:a 1 :b 2 :c (range 5)}
              {:width 10}))


(comment
  (./code:import)
  (./code:orphaned '[hara.print.pretty])
  )

(comment
  (./code:scaffold)
  (./code:arrange)
  (./code:import))
