(ns hara.print.graphic-test
  (:use hara.test)
  (:require [hara.print.graphic :refer :all]))

^{:refer hara.print.graphic/print-animation :added "3.0"}
(comment "outputs an animated ascii file"

  (print-animation "dev/test/hara.print/plane.ascii"))

^{:refer hara.print.graphic/format-bar-graph :added "3.0"}
(fact "formats an ascii bar graph for output"

  (format-bar-graph (range 10) 6)
  => ["    ▟"
      "  ▗██"
      " ▟███"])

^{:refer hara.print.graphic/print-bar-graph :added "3.0"}
(fact "prints an ascii bar graph"

  (-> (print-bar-graph (range 10))
      (with-out-str)))

^{:refer hara.print.graphic/format-sparkline :added "3.0"}
(fact "formats a sparkline"

  (format-sparkline (range 8))
  => " ▁▂▃▅▆▇█")

^{:refer hara.print.graphic/print-sparkline :added "3.0"}
(fact "prints a sparkline"

  (-> (print-sparkline (range 8))
      (with-out-str)))

^{:refer hara.print.graphic/format-border :added "3.0"}
(fact "formats a border around given lines"

  (format-border [(format-sparkline (range 8))])
  => ["┌────────┐"
      "│ ▁▂▃▅▆▇█│"
      "└────────┘"])

^{:refer hara.print.graphic/print-border :added "3.0"}
(fact "prints a border around given lines"

  (-> (format-border [(format-sparkline (range 8))])
      (with-out-str)))

^{:refer hara.print.graphic/with-border :added "3.0"}
(fact "macro to put borders around printed text"

  (-> (with-border (print-sparkline (range 8)))
      (with-out-str)))
