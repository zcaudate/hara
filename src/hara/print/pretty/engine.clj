(ns hara.print.pretty.engine
  (:require [hara.string :as string]
            [hara.function :as fn :refer [definvoke]]
            [hara.data.deque :as deque]
            [hara.protocol.print :as protocol.print]))

(defn serialize
  "main serialize method, converts input into a set of operations
   
   (serialize [:span \"apple\" [:group \"ball\" :line \"cat\"]])
   => [{:op :text, :text \"apple\"}
       {:op :begin}
       {:op :text, :text \"ball\"}
       {:op :line, :inline \" \", :terminate \"\"}
       {:op :text, :text \"cat\"}
       {:op :end}]"
  {:added "3.0"}
  [input]
  (cond (nil? input)     nil
        (seq? input)     (mapcat serialize input)
        (string? input)  [{:op :text, :text input}]
        (keyword? input) (protocol.print/-serialize-node [input])
        (vector? input)  (protocol.print/-serialize-node input)
        :else (throw (ex-info "Unexpected class for input" {:node input}))))

(definvoke serialize-node-text
  "creates a :text operation
   
   (serialize-node-text [:text \"apple\" \"ball\"])
   => [{:op :text, :text \"appleball\"}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :text}]
  ([[_ & text]]
   [{:op :text, :text (apply str text)}]))

(definvoke serialize-node-pass
  "creates a :pass operation
 
   (serialize-node-pass [:pass \"apple\" \"ball\"])
   => [{:op :pass, :text \"appleball\"}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :pass}]
  ([[_ & text]]
   [{:op :pass, :text (apply str text)}]))

(definvoke serialize-node-escaped
  "creates an :escaped operation
 
   (serialize-node-escaped [:escaped \"apple\"])
   => [{:op :escaped, :text \"apple\"}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :escaped}]
  ([[_ text]]
   (assert (string? text))
   [{:op :escaped, :text text}]))

(definvoke serialize-node-span
  "creates a :span operation
 
   (serialize-node-span [:span \"apple\" \"ball\"])
   => [{:op :text, :text \"apple\"}
       {:op :text, :text \"ball\"}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :span}]
  ([[_ & children]]
   (serialize children)))

(definvoke serialize-node-line
  "creates a :line operation
 
   (serialize-node-line [:line])
   => [{:op :line, :inline \" \", :terminate \"\"}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :line}]
  
  ([[_ inline terminate]]
   (let [inline (or inline " ")
         terminate (or terminate "")]
     (assert (string? inline))
     (assert (string? terminate))
     [{:op :line, :inline inline, :terminate terminate}])))

(definvoke serialize-node-break
  "creates a :break operation
 
   (serialize-node-break [:break])
   => [{:op :break}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :break}]
  ([& _]
   [{:op :break}]))

(definvoke serialize-node-group
  "creates a :group operation
 
   (serialize-node-group [:group \"apple\" \"ball\"])
   => [{:op :begin}
       {:op :text, :text \"apple\"}
       {:op :text, :text \"ball\"}
       {:op :end}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :group}]
  ([[_ & children]]
   (concat [{:op :begin}] (serialize children) [{:op :end}])))

(definvoke serialize-node-nest
  "creates a :nest operation
 
   (serialize-node-nest [:nest 2 \"apple\" \"ball\"])
   => [{:op :nest, :offset 2}
       {:op :text, :text \"apple\"}
       {:op :text, :text \"ball\"}
       {:op :outdent}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :nest}]
  ([[_ & args]]
   (let [[offset & children] (if (number? (first args))
                               args
                               (cons 2 args))]
     (concat [{:op :nest, :offset offset}]
             (serialize children)
             [{:op :outdent}]))))

(definvoke serialize-node-align
  "creates an :align operation
 
   (serialize-node-align [:align 2 \"apple\" \"ball\"])
   => [{:op :align, :offset 2}
       {:op :text, :text \"apple\"}
       {:op :text, :text \"ball\"}
       {:op :outdent}]"
  {:added "3.0"}
  [:method {:multi protocol.print/-serialize-node
            :val :align}]
  ([[_ & args]]
   (let [[offset & children] (if (number? (first args))
                               args
                               (cons 0 args))]
     (concat [{:op :align, :offset offset}]
             (serialize children)
             [{:op :outdent}]))))

(defn annotate-right
  "adds `:right <position>` to all nodes
 
   (def -doc- [:group \"A\" :line [:nest 2 \"B\" :line \"C\"] :line \"D\"])
 
   (eduction (annotate-right)
             (serialize -doc-))
   => [{:op :begin, :right 0}
       {:op :text, :text \"A\", :right 1}
       {:op :line, :inline \" \", :terminate \"\", :right 2}
       {:op :nest, :offset 2, :right 2}
       {:op :text, :text \"B\", :right 3}
       {:op :line, :inline \" \", :terminate \"\", :right 4}
       {:op :text, :text \"C\", :right 5}
       {:op :outdent, :right 5}
       {:op :line, :inline \" \", :terminate \"\", :right 6}
      {:op :text, :text \"D\", :right 7}
       {:op :end, :right 7}]"
  {:added "3.0"}
  ([]
   (annotate-right {:position (volatile! 0)}))
  ([{:keys [position] :as state}]
   (fn [rf]
     (fn
       ([] (rf))
       ([out] (rf out))
       ([out node]
        (let [delta (case (:op node)
                      :text (count (:text node))
                      :line (count (:inline node))
                      :escaped 1
                      0)
              p (vswap! position + delta)]
          (rf out (assoc node :right p))))))))

(defn annotate-begin
  "recalculates `:right` value of `{:op :begin}` nodes given line width
 
   (eduction (annotate-right)
             (annotate-begin 4)
             (serialize -doc-))
   => [{:op :begin, :right :too-far}
       {:op :text, :text \"A\", :right 1}
       {:op :line, :inline \" \", :terminate \"\", :right 2}
       {:op :nest, :offset 2, :right 2}
       {:op :text, :text \"B\", :right 3}
      {:op :line, :inline \" \", :terminate \"\", :right 4}
       {:op :text, :text \"C\", :right 5}
       {:op :outdent, :right 5}
       {:op :line, :inline \" \", :terminate \"\", :right 6}
       {:op :text, :text \"D\", :right 7} {:op :end, :right 7}]"
  {:added "3.0"}
  ([width]
   (annotate-begin width {:position (volatile! 0)
                          :buffers  (volatile! [])}))
  ([width {:keys [position buffers]}]
   (fn [rf]
     (fn
       ([] (rf))
       ([out] (rf out))
       ([out {:keys [op right] :as node}]
        (cond (empty? @buffers)
              (cond (= op :begin) ;; Prepare buffer
                    (let [pos (+ right width)]
                      (vreset! position pos)
                      (vreset! buffers (deque/create {:position pos :nodes []}))
                      out)
                    
                    :else  ;; Reduce
                    (rf out node))
              
              (= op :end) ;; Pop buffer
              (let [rbuff   (deque/peek-right @buffers)
                    nbuffs  (deque/pop-right  @buffers)
                    begin   {:op :begin :right right}
                    nodes   (deque/conj-both begin (:nodes rbuff) node)]
                (if (empty? nbuffs)
                  (do
                    (vreset! position 0)
                    (vreset! buffers [])
                    (reduce rf out nodes))
                  (do
                    (assert (vector? nbuffs))
                    (assert (vector? nodes))
                    (vreset! buffers (deque/update-right nbuffs
                                                         update-in [:nodes] deque/concat nodes))
                    out)))
              

              :else   ;; Lookahead
              (loop [nbuffs (if (= op :begin)
                              (deque/conj-right   @buffers
                                                  {:position (+ right width)
                                                   :nodes []})
                              (deque/update-right @buffers
                                                  update-in [:nodes] deque/conj-right node))
                     out  out]
                (cond (and (<= right @position) (<= (count nbuffs) width))
                      ;; Not too far
                      (do (vreset! buffers nbuffs)
                          out)

                      :else ;; Too far
                      (let [fbuff (first nbuffs)
                            nnbuffs (deque/pop-left nbuffs)
                            begin {:op :begin, :right :too-far}
                            out   (rf out begin)
                            out   (reduce rf out (:nodes fbuff))]
                        (if (empty? nnbuffs)
                          ;; Root buffered group
                          (do
                            (vreset! position 0)
                            (vreset! buffers [])
                            out)
                          ;; Interior group
                          (do
                            (vreset! position (:position (first nnbuffs)))
                            (recur nnbuffs out))))))))))))

(defn format-nodes
  "formats nodes given line width
 
   (eduction (annotate-right)
             (annotate-begin 4)
             (format-nodes 4)
             (serialize -doc-))
   => [\"\" \"A\" \"\\n\" \"  \" \"B\" \"\\n\" \"  \" \"C\" \"\\n\" \"\" \"D\"]"
  {:added "3.0"}
  ([width]
   (format-nodes width {:fits (volatile! 0)
                        :length (volatile! width)
                        :tab-stops (volatile! '(0)) ; Technically, an unbounded stack...
                        :column (volatile! 0)}))
  ([width {:keys [fits length tab-stops column]}]
   (fn [rf]
     (fn
       ([] (rf))
       ([res] (rf res))
       ([res {:keys [op right] :as node}]
        (let [indent (peek @tab-stops)]
          (case op
            :text
            (let [text (:text node)
                  res* (if (zero? @column)
                         (do (vswap! column + indent)
                             (rf res (apply str (repeat indent \space))))
                         res)]
              (vswap! column + (count text))
              (rf res* text))
            :escaped
            (let [text (:text node)
                  res* (if (zero? @column)
                         (do (vswap! column + indent)
                             (rf res (apply str (repeat indent \space))))
                         res)]
              (vswap! column inc)
              (rf res* text))
            :pass
            (rf res (:text node))
            :line
            (if (zero? @fits)
              (do
                (vreset! length (- (+ right width) indent))
                (vreset! column 0)
                (rf res (str (:terminate node) "\n")))
              (let [inline (:inline node)]
                (vswap! column + (count inline))
                (rf res inline)))
            :break
            (do
              (vreset! length (- (+ right width) indent))
              (vreset! column 0)
              (rf res "\n"))
            :nest
            (do (vswap! tab-stops conj (+ indent (:offset node)))
                res)
            :align
            (do (vswap! tab-stops conj (+ @column (:offset node)))
                res)
            :outdent
            (do (vswap! tab-stops pop)
                res)
            :begin
            (do (vreset! fits (cond
                                (pos? @fits) (inc @fits)
                                (= right :too-far) 0
                                (<= right @length) 1
                                :else 0))
                res)
            :end
            (do (vreset! fits (max 0 (dec @fits)))
                res)
            (throw (ex-info "Unexpected node op" {:node node})))))))))

(defn pprint-document
  "pretty prints a document
 
   (with-out-str
     (pprint-document [:group \"A\" :line [:nest 2 \"B\" :line \"C\"] :line \"D\"]))
   => \"A B C D\"
 
   (with-out-str
     (pprint-document [:group \"A\" :line [:nest 2 \"B\" :line \"C\"] :line \"D\"]
                      {:width 4}))
   => \"A\\n  B\\n  C\\nD\""
  {:added "3.0"}
  ([doc]
   (pprint-document doc {}))
  ([doc {:keys [width] :or {width 70}}]
   (->> (serialize doc)
        (eduction (annotate-right)
                  (annotate-begin width)
                  (format-nodes width))
        (run! print))))
