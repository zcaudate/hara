(ns hara.print.base.report
  (:require [hara.core.base.result :as result]
            [hara.print.base.common :as common]
            [hara.string :as string]
            [hara.string.base.ansi :as ansi]))

(defn pad
  "creates `n` number of spaces
 
   (pad 1) => \"\"
   (pad 5) => \"\""
  {:added "3.0"}
  [len]
  (apply str (repeat len common/+pad+)))

(defn pad-right
  "puts the content to the left, padding missing spaces
 
   (pad-right \"hello\" 10)
   => \"hello     \""
  {:added "3.0"}
  [content length]
  (str content (pad (- length (count content)))))

(defn pad-center
  "puts the content at the center, padding missing spacing
 
   (pad-center \"hello\" 10)
   => \"hello   \""
  {:added "3.0"}
  [content length]
  (let [total (- length (count content))
        half  (long (/ total 2))
        [left right] (cond (even? total)
                           [half half]

                           :else
                           [half (inc half)])]
    (str (pad left) content (pad right))))

(defn pad-left
  "puts the content to the right, padding missing spaces
 
   (pad-left \"hello\" 10)
   => \"hello\""
  {:added "3.0"}
  [content length]
  (str (pad (- length (count content))) content))

(defn pad-down
  "creates new lines of n spaces
   (pad-down [] 10 2)
   => [\"          \" \"          \"]"
  {:added "3.0"}
  [row length height]
  (let [line (apply str (repeat length common/+pad+))]
    (concat row
            (repeat (- height (count row)) line))))

(defn justify
  "justifies the content to a given alignment
   
   (justify :right \"hello\" 10)
   => \"hello\"
 
   (justify :left \"hello\" 10)
   => \"hello     \""
  {:added "3.0"}
  [align content length]
  (case align
    :center (pad-center content length)
    :right (pad-left content length)
    (pad-right content length)))

(defn seq-elements
  "layout an array of elements as a series of rows of a given length
 
   (seq-elements [\"A\" \"BC\" \"DEF\" \"GHIJ\" \"KLMNO\"]
                 {:align :left
                  :length 9}
                 0
                 1)
   => {:rows [\"[A BC DEF\"
              \" GHIJ    \"
              \" KLMNO]  \"]}"
  {:added "3.0"}
  [arr {:keys [align length]} padding spacing]
  (let [pad   (apply str (repeat padding common/+pad+))
        space (apply str (repeat spacing common/+space+))]
    (if (empty? arr)
      {:rows [(justify align (str pad "[]" pad) length)]}
      (reduce (fn [{:keys [current rows] :as out} row-item]
                (let [s (str row-item)
                      l (count s)]
                  (cond (nil? current)
                        {:rows rows :current (str pad "[" s)}

                        (nil? row-item)
                        {:rows (conj rows (justify align
                                                   (str current "]")
                                                   length))}

                        (> (+ l spacing (count current))
                           length)
                        {:rows (conj rows (justify align
                                                   current
                                                   length))
                         :current (str pad " " s)}

                        :else
                        {:rows rows
                         :current (str current space s)})))
              {:rows []}
              (conj arr nil)))))

(defn row-elements
  "layout raw elements based on alignment and length properties
 
   (row-elements [\"hello\" :world [:a :b :c :d :e :f]]
                 {:padding 0
                  :spacing 1
                  :columns [{:align :right :length 10}
                            {:align :center :length 10}
                            {:align :left :length 10}]})
   => [[\"     hello\"] [\"  :world  \"] [\"[:a :b :c \"
                                      \" :d :e :f]\"]]"
  {:added "3.0"}
  [row {:keys [padding spacing columns] :as params}]
  (let [pad   (apply str (repeat padding common/+pad+))
        prep-fn (fn [row-item {:keys [format align length] :as column}]
                  (let [row-item (if format
                                   (string/format format row-item)
                                   row-item)]
                    (if (sequential? row-item)
                      (:rows (seq-elements row-item column padding spacing))
                      [(justify align (str pad  row-item) length)])))]
    (mapv (fn [row-item column]
            (let [data (if (result/result? row-item)
                         (:data row-item)
                         row-item)]
              (prep-fn data column)))
          row columns)))

(defn prepare-elements
  "same as row-elements but allows for colors and results
   
   (prepare-elements [\"hello\" :world [:a :b :c :d]]
                     {:padding 0
                      :spacing 1
                      :columns [{:align :right :length 10}
                                {:align :center :length 10}
                                {:align :left :length 10}]})
   
   => [[\"     hello\" \"          \"]
       [\"  :world  \" \"          \"]
       [\"[:a :b :c \" \" :d]      \"]]"
  {:added "3.0"}
  [row {:keys [padding columns] :as params}]
  (let [pad       (apply str (repeat padding common/+pad+))
        elements (row-elements row params)
        height   (apply max (map count elements))
        style    (fn [lines color]
                   (if color
                     (map #(ansi/style % color) lines)
                     lines))
        lines    (mapv (fn [s {:keys [align length color]} row-item]
                         (let [color (if (result/result? row-item)
                                       #{(:status row-item)}
                                       color)]
                           (-> (pad-down s length height)
                               (style color))))
                       elements
                       columns
                       row)]
    lines))

(defn print-header
  "prints a header for the row
 
   (-> (print-header [:id :name :value]
                     {:padding 0
                     :spacing 1
                      :columns [{:align :right :length 10}
                                {:align :center :length 10}
                                {:align :left :length 10}]})
       (with-out-str))"
  {:added "3.0"}
  [titles {:keys [padding columns] :as params}]
  (let [pad (pad padding)
        header (-> (apply str
                          (mapv (fn [title {:keys [align length]}]
                                  (str pad
                                       (justify align
                                                (name title)
                                                length)))
                                titles
                                columns))
                   (ansi/style #{:bold}))]
    (println header)
    (print "\n")))

(defn print-row
  "prints a row to output
   
   (-> (print-row [\"hello\" :world (result/result {:data [:a :b :c :d :e :f]
                                                  :status :info})]
                  {:padding 0
                   :spacing 1
                  :columns [{:align :right :length 10}
                             {:align :center :length 10}
                             {:align :left :length 10}]})
       (with-out-str))"
  {:added "3.0"}
  [row params]
  (apply mapv println (prepare-elements row params)))

(defn print-title
  "prints the title
 
   (-> (print-title \"Hello World\")
       (with-out-str))"
  {:added "3.0"}
  [title]
  (let [line (apply str (repeat (count title) \-))]
    (print (ansi/style (string/format "\n%s\n%s\n%s\n" line title line)
                       #{:bold}))))

(defn print-subtitle
  "prints the subtitle
 
   (-> (print-subtitle \"Hello Again\")
       (with-out-str))"
  {:added "3.0"}
  [text]
  (print (ansi/style text #{:bold}) "\n"))

(defn print-column
  "prints the column
 
   (-> (print-column [[:id.a {:data 100}] [:id.b {:data 200}]]
                     :data
                    #{})
       (with-out-str))"
  {:added "3.0"}
  [items name color]
  (let [ns-len  (or (->> items (map (comp count str first)) sort last)
                    20)
        display {:padding 1
                 :spacing 1
                 :columns [{:id :key   :length (inc ns-len) :align :left}
                           {:id name  :length 60 :color color}]}]
    (print-header [:key name] display)
    (doseq [[key m] items]
      (print-row [key (get m name)] display))
    (print "\n")))

(defn print-compare
  "outputs a side by side comparison
 
   (-> (print-compare [['hara.code [[:a :b :c] [:d :e :f]]]])
       (with-out-str))"
  {:added "3.0"}
  [output]
  (let [ns-len  (or (->> output (map (comp count str first)) sort last)
                    20)
        item-params {:padding 1
                     :spacing 1
                     :columns [{:id :ns    :length (inc ns-len) :align :left}
                               {:id :data  :length 50 :color #{:highlight}}
                               {:id :data  :length 50 :color #{:magenta}}]}]
    (doseq [[ns [src test]] output]
      (print-row [ns src (if (empty? test)
                           :no-tests
                           test)]
                 item-params))
    (print "\n")))

(defn print-summary
  "outputs the summary of results
 
   (-> (print-summary {:count 6 :files 2})
       (with-out-str))"
  {:added "3.0"}
  [m]
  (let [ks (sort (keys m))]
    (print (ansi/style (str "SUMMARY " m "\n")  #{:bold}))))

(defn format-tree
  "returns a string representation of a tree
 
   (-> (format-tree '[{a \"1.1\"}
                      [{b \"1.2\"}
                       [{c \"1.3\"}
                        {d \"1.4\"}]]])
       (string/split-lines))
   => [\"{a \\\"1.1\\\"}\"
       \"  {b \\\"1.2\\\"}\"
       \"    {c \\\"1.3\\\"}\"
      \"    {d \\\"1.4\\\"}\" \"\"]"
  {:added "3.0"}
  ([tree] (apply str (map #(format-tree % "" nil?) tree)))
  ([tree prefix check]
   (if (and (vector? tree)
            (not (check tree)))
     (->> (map #(format-tree % (str prefix "  ") check) tree)
          (apply str))
     (str prefix tree "\n"))))

(defn print-tree
  "outputs the result of `format-tree`
   
   (print-tree '[{a \"1.1\"}
                 [{b \"1.2\"}
                  [{c \"1.3\"}
                   {d \"1.4\"}]]])"
  {:added "3.0"}
  ([tree]
   (print-tree tree "" nil?))
  ([tree prefix check]
   (let [output (->> (format-tree tree prefix check)
                     (string/split-lines))
         min-spaces (apply min (map (fn [s] (-> (re-find #"^(\s*)" s)
                                                first
                                                count))
                                    output))
         output (->> output
                     (map #(subs % min-spaces))
                     (map (fn [s] (if (.startsWith s " ")
                                    (ansi/green "" s)
                                    (ansi/bold  "\n" s)))))]
     (println (string/join output "\n")))))
