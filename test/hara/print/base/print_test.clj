(ns hara.print.base.report-test
  (:use hara.test)
  (:require [hara.print.base.report :refer :all]
            [hara.core.base.result :as result]
            [hara.string :as string]))

^{:refer hara.print.base.report/pad :added "3.0"}
(fact "creates `n` number of spaces"

  (pad 1) => " "
  (pad 5) => "     ")

^{:refer hara.print.base.report/pad-right :added "3.0"}
(fact "puts the content to the left, padding missing spaces"

  (pad-right "hello" 10)
  => "hello     ")

^{:refer hara.print.base.report/pad-center :added "3.0"}
(fact "puts the content at the center, padding missing spacing"

  (pad-center "hello" 10)
  => "  hello   ")

^{:refer hara.print.base.report/pad-left :added "3.0"}
(fact "puts the content to the right, padding missing spaces"

  (pad-left "hello" 10)
  => "     hello")

^{:refer hara.print.base.report/pad-down :added "3.0"}
(fact "creates new lines of n spaces"
  (pad-down [] 10 2)
  => ["          " "          "])

^{:refer hara.print.base.report/justify :added "3.0"}
(fact "justifies the content to a given alignment"
  
  (justify :right "hello" 10)
  => "     hello"

  (justify :left "hello" 10)
  => "hello     ")

^{:refer hara.print.base.report/seq-elements :added "3.0"}
(fact "layout an array of elements as a series of rows of a given length"

  (seq-elements ["A" "BC" "DEF" "GHIJ" "KLMNO"]
                {:align :left
                 :length 9}
                0
                1)
  => {:rows ["[A BC DEF"
             " GHIJ    "
             " KLMNO]  "]})

^{:refer hara.print.base.report/row-elements :added "3.0"}
(fact "layout raw elements based on alignment and length properties"

  (row-elements ["hello" :world [:a :b :c :d :e :f]]
                {:padding 0
                 :spacing 1
                 :columns [{:align :right :length 10}
                           {:align :center :length 10}
                           {:align :left :length 10}]})
  => [["     hello"] ["  :world  "] ["[:a :b :c "
                                     " :d :e :f]"]])

^{:refer hara.print.base.report/prepare-elements :added "3.0"}
(fact "same as row-elements but allows for colors and results"
  
  (prepare-elements ["hello" :world [:a :b :c :d]]
                    {:padding 0
                     :spacing 1
                     :columns [{:align :right :length 10}
                               {:align :center :length 10}
                               {:align :left :length 10}]})
  
  => [["     hello" "          "]
      ["  :world  " "          "]
      ["[:a :b :c " " :d]      "]])

^{:refer hara.print.base.report/print-header :added "3.0"}
(fact "prints a header for the row"

  (-> (print-header [:id :name :value]
                    {:padding 0
                     :spacing 1
                     :columns [{:align :right :length 10}
                               {:align :center :length 10}
                               {:align :left :length 10}]})
      (with-out-str))^:hidden
  => "[1m        id   name   value     [0m\n\n")

^{:refer hara.print.base.report/print-row :added "3.0" :tags #{:print}}
(fact "prints a row to output"
  
  (-> (print-row ["hello" :world (result/result {:data [:a :b :c :d :e :f]
                                                 :status :info})]
                 {:padding 0
                  :spacing 1
                  :columns [{:align :right :length 10}
                            {:align :center :length 10}
                            {:align :left :length 10}]})
      (with-out-str))^:hidden
  => "     hello   :world   [34m[:a :b :c [0m\n                      [34m :d :e :f][0m\n")

^{:refer hara.print.base.report/print-title :added "3.0" :tags #{:print}}
(fact "prints the title"

  (-> (print-title "Hello World")
      (with-out-str))^:hidden
  => "[1m\n-----------\nHello World\n-----------\n[0m")

^{:refer hara.print.base.report/print-subtitle :added "3.0"}
(fact "prints the subtitle"

  (-> (print-subtitle "Hello Again")
      (with-out-str))^:hidden
  => "[1mHello Again[0m \n")

^{:refer hara.print.base.report/print-column :added "3.0"}
(fact "prints the column"

  (-> (print-column [[:id.a {:data 100}] [:id.b {:data 200}]]
                    :data
                    #{})
      (with-out-str))^:hidden
  => string?
  )

^{:refer hara.print.base.report/print-compare :added "3.0" :tags #{:print}}
(fact "outputs a side by side comparison"

  (-> (print-compare [['hara.code [[:a :b :c] [:d :e :f]]]])
      (with-out-str)))

^{:refer hara.print.base.report/print-summary :added "3.0" :tags #{:print}}
(fact "outputs the summary of results"

  (-> (print-summary {:count 6 :files 2})
      (with-out-str))^:hidden
  ;; SUMMARY {:count 6, :files 2}
  => "[1mSUMMARY {:count 6, :files 2}\n[0m")

^{:refer hara.print.base.report/format-tree :added "3.0"}
(fact "returns a string representation of a tree"

  (-> (format-tree '[{a "1.1"}
                     [{b "1.2"}
                      [{c "1.3"}
                       {d "1.4"}]]])
      (string/split-lines))
  => ["{a \"1.1\"}"
      "  {b \"1.2\"}"
      "    {c \"1.3\"}"
      "    {d \"1.4\"}" ""])

^{:refer hara.print.base.report/print-tree :added "3.0"}
(fact "outputs the result of `format-tree`"
  
  (print-tree '[{a "1.1"}
                [{b "1.2"}
                 [{c "1.3"}
                  {d "1.4"}]]]))

(comment
  (./code:scaffold)
  (./code:import)
  (./code:arrange)
  (./code:scaffold))
