(ns hara.print.graphic
  (:require [hara.core.base.result :as result]
            [hara.print.base.common :as common]
            [hara.string :as string]
            [hara.string.base.ansi :as ansi])
  (:refer-clojure :exclude [format]))

(defn print-animation
  "outputs an animated ascii file
 
   (print-ascii \"dev/test/hara.print/plane.ascii\")"
  {:added "3.0"}
  ([file]
   (let [lines (string/split-lines (slurp file))
         x (Integer/parseInt (subs (first lines) 2))
         y (Integer/parseInt (subs (second lines) 2))
         frames (->> (drop 2 lines)
                     (partition (inc y))
                     (map rest))]
     (print-animation x y frames)))
  ([x y frames]
   (print-animation x y frames {:pause 20}))
  ([x y frames {:keys [pause]}]
   (doseq [frame frames]
     (doseq [line frame]
       (println line))
     (Thread/sleep pause)
     (dotimes [i y]
       (print common/+up+)
       (print common/+clearline+)))))

(defn format-bar-graph
  "formats an ascii bar graph for output
 
   (format-bar-graph (range 10) 6)
   => [\"    ▟\"
       \"  ▗██\"
       \" ▟███\"]"
  {:added "3.0"}
  ([xs height]
   (let [width (count xs)
         _    (assert (even? width))
         _    (assert (even? height))
         m+   (apply max xs)
         m-   0
         w    (- m+ m-)
         xs'  (vec (for [x xs] (-> x (- m-) (/ w) (* height) int)))
         f    (fn [col row] (if (< (xs' col) row) 0 1))
         lookup (vec "    ▖▌  ▗ ▐ ▄▙▟█")]
     (for [row (range height 0 -2)]
       (apply str (for [col (range 0 width 2)
                        :let [a (f col row)
                              b (f (inc col) row)
                              c (f col (dec row))
                              d (f (inc col) (dec row))]]
                    (lookup (+ (* 1 a) (* 2 b) (* 4 c) (* 8 d)))))))))

(defn print-bar-graph
  "prints an ascii bar graph
 
   (-> (print-bar-graph (range 10))
       (with-out-str))"
  {:added "3.0"}
  ([xs] (print-bar-graph xs 30))
  ([xs height]
   (let [lines (format-bar-graph xs height)]
     (doseq [l lines]
       (print l "\n")))))

(defn format-sparkline
  "formats a sparkline
 
   (format-sparkline (range 8))
   => \"▁▂▃▅▆▇█\""
  {:added "3.0"}
  ([xs]
   (let [h+ (apply max xs)
         m  (vec " ▁▂▃▄▅▆▇█")
         spark-fn #(-> % double (/ (+ 0.00001 h+)) (* 9) int m)]
     (->> (map spark-fn xs)
          (apply str)))))

(defn print-sparkline
  ([xs]
   (print (format-sparkline xs))))

(defn format-border
  "formats a border around given lines
 
   (format-border [(format-sparkline (range 8))])
   => [\"┌────────┐\"
       \"│ ▁▂▃▅▆▇█│\"
       \"└────────┘\"]"
  {:added "3.0"}
  ([lines]
   (format-border lines (apply max (map count lines))))
  ([lines width]
   (let [hr     (apply str (repeat width "─"))
         top    (str "┌" hr "┐")
         bottom (str "└" hr "┘")
         lines  (mapv (fn [line]
                        (let [pad (- width (count line))]
                          (str "│" line (apply str (repeat pad \space))  "│")))
                      lines)]
     (vec (cons top (conj lines bottom))))))

(defn print-border
  "prints a border around given lines
 
   (-> (format-border [(format-sparkline (range 8))])
       (with-out-str))"
  {:added "3.0"}
  ([lines]
   (let [lines (format-border lines)]
     (doseq [l lines]
       (print l "\n")))))

(defmacro with-border
  "macro to put borders around printed text
 
   (-> (with-border (print-sparkline (range 8)))
       (with-out-str))"
  {:added "3.0"}
  [& body]
  `(let [~'s (with-out-str ~(cons 'do body))
         ~'lines (-> (string/trim-newlines ~'s)
                     (string/split-lines))]
     (print-border ~'lines)))
