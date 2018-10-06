(ns hara.print.pretty.engine-test
  (:use hara.test)
  (:require [hara.print.pretty.engine :refer :all]))

^{:refer hara.print.pretty.engine/serialize :added "3.0"}
(fact "main serialize method, converts input into a set of operations"
  
  (serialize [:span "apple" [:group "ball" :line "cat"]])
  => [{:op :text, :text "apple"}
      {:op :begin}
      {:op :text, :text "ball"}
      {:op :line, :inline " ", :terminate ""}
      {:op :text, :text "cat"}
      {:op :end}]^:hidden
  
  (serialize [:span "apple" [:line ","] "ball"])
  => [{:op :text, :text "apple"}
      {:op :line, :inline ",", :terminate ""}
      {:op :text, :text "ball"}]

  (serialize [:group "A" :line
              [:nest 2 "B-XYZ" [:align -3 :line "C"]]
              :line "D"])
  => [{:op :begin}
      {:op :text, :text "A"}
      {:op :line, :inline " ", :terminate ""}
      {:op :nest, :offset 2}
      {:op :text, :text "B-XYZ"}
      {:op :align, :offset -3}
      {:op :line, :inline " ", :terminate ""}
      {:op :text, :text "C"}
      {:op :outdent}
      {:op :outdent}
      {:op :line, :inline " ", :terminate ""}
      {:op :text, :text "D"}
      {:op :end}])

^{:refer hara.print.pretty.engine/serialize-node-text :added "3.0"}
(fact "creates a :text operation"
  
  (serialize-node-text [:text "apple" "ball"])
  => [{:op :text, :text "appleball"}])

^{:refer hara.print.pretty.engine/serialize-node-pass :added "3.0"}
(fact "creates a :pass operation"

  (serialize-node-pass [:pass "apple" "ball"])
  => [{:op :pass, :text "appleball"}])

^{:refer hara.print.pretty.engine/serialize-node-escaped :added "3.0"}
(fact "creates an :escaped operation"

  (serialize-node-escaped [:escaped "apple"])
  => [{:op :escaped, :text "apple"}])

^{:refer hara.print.pretty.engine/serialize-node-span :added "3.0"}
(fact "creates a :span operation"

  (serialize-node-span [:span "apple" "ball"])
  => [{:op :text, :text "apple"}
      {:op :text, :text "ball"}])

^{:refer hara.print.pretty.engine/serialize-node-line :added "3.0"}
(fact "creates a :line operation"

  (serialize-node-line [:line])
  => [{:op :line, :inline " ", :terminate ""}])

^{:refer hara.print.pretty.engine/serialize-node-break :added "3.0"}
(fact "creates a :break operation"

  (serialize-node-break [:break])
  => [{:op :break}])

^{:refer hara.print.pretty.engine/serialize-node-group :added "3.0"}
(fact "creates a :group operation"

  (serialize-node-group [:group "apple" "ball"])
  => [{:op :begin}
      {:op :text, :text "apple"}
      {:op :text, :text "ball"}
      {:op :end}])

^{:refer hara.print.pretty.engine/serialize-node-nest :added "3.0"}
(fact "creates a :nest operation"

  (serialize-node-nest [:nest 2 "apple" "ball"])
  => [{:op :nest, :offset 2}
      {:op :text, :text "apple"}
      {:op :text, :text "ball"}
      {:op :outdent}])

^{:refer hara.print.pretty.engine/serialize-node-align :added "3.0"}
(fact "creates an :align operation"

  (serialize-node-align [:align 2 "apple" "ball"])
  => [{:op :align, :offset 2}
      {:op :text, :text "apple"}
      {:op :text, :text "ball"}
      {:op :outdent}])

^{:refer hara.print.pretty.engine/annotate-right :added "3.0"}
(fact "adds `:right <position>` to all nodes"

  (def -doc- [:group "A" :line [:nest 2 "B" :line "C"] :line "D"])

  (eduction (annotate-right)
            (serialize -doc-))
  => [{:op :begin, :right 0}
      {:op :text, :text "A", :right 1}
      {:op :line, :inline " ", :terminate "", :right 2}
      {:op :nest, :offset 2, :right 2}
      {:op :text, :text "B", :right 3}
      {:op :line, :inline " ", :terminate "", :right 4}
      {:op :text, :text "C", :right 5}
      {:op :outdent, :right 5}
      {:op :line, :inline " ", :terminate "", :right 6}
      {:op :text, :text "D", :right 7}
      {:op :end, :right 7}])

^{:refer hara.print.pretty.engine/annotate-begin :added "3.0"}
(fact "recalculates `:right` value of `{:op :begin}` nodes given line width"

  (eduction (annotate-right)
            (annotate-begin 4)
            (serialize -doc-))
  => [{:op :begin, :right :too-far}
      {:op :text, :text "A", :right 1}
      {:op :line, :inline " ", :terminate "", :right 2}
      {:op :nest, :offset 2, :right 2}
      {:op :text, :text "B", :right 3}
      {:op :line, :inline " ", :terminate "", :right 4}
      {:op :text, :text "C", :right 5}
      {:op :outdent, :right 5}
      {:op :line, :inline " ", :terminate "", :right 6}
      {:op :text, :text "D", :right 7} {:op :end, :right 7}])

^{:refer hara.print.pretty.engine/format-nodes :added "3.0"}
(fact "formats nodes given line width"

  (eduction (annotate-right)
            (annotate-begin 4)
            (format-nodes 4)
            (serialize -doc-))
  => ["" "A" "\n" "  " "B" "\n" "  " "C" "\n" "" "D"])

^{:refer hara.print.pretty.engine/pprint-document :added "3.0"}
(fact "pretty prints a document"

  (with-out-str
    (pprint-document [:group "A" :line [:nest 2 "B" :line "C"] :line "D"]))
  => "A B C D"

  (with-out-str
    (pprint-document [:group "A" :line [:nest 2 "B" :line "C"] :line "D"]
                     {:width 4}))
  => "A\n  B\n  C\nD")

