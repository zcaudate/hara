(ns hara.core.debug
  (:require [clojure.java.shell :as sh]))

(defn say
  "enables audio debugging on osx
 
   (say \"hello there\")"
  {:added "3.0"}
  [& phrase]
  (future (apply clojure.java.shell/sh "say"
                 (map str phrase))))

(defn wrap-print
  "wraps a function in a `println` statement'
 
   ((hara.core.debug/wrap-print + (quote (+ 1 2 3)) (quote ->)) 1 2 3)
   => 6
 
   (with-out-str
     ((hara.core.debug/wrap-print + (quote (+ 1 2 3)) (quote ->)) 1 2 3))
   => \"-> (+ 1 2 3) :: 6\\n\""
  {:added "3.0"}
  [f expr arrow]
  (fn [& args]
    (let [result (apply f args)]
      (println arrow expr "::" result)
      result)))

(defn dbg-print
  "creates the form for debug print
 
   (dbg-print '(+ 1 2 3) '->)
   => '((hara.core.debug/wrap-print + (quote (+ 1 2 3)) (quote ->)) 1 2 3)"
  {:added "3.0"}
  [form arrow]
  (cond (list? form)
        `((wrap-print ~(first form)
                      (quote ~form)
                      (quote ~arrow))
          ~@(rest form))

        :else
        `((wrap-print ~form (quote ~form) (quote ~arrow)))))

(defmacro dbg->
  "prints each stage of the `->` macro
   (-> (dbg-> {:a 1}
              (assoc :b 2)
              (merge {:c 3}))
       (with-out-str)
       (string/split-lines))
   => [\"\" \"\"
       \"{:a 1}\"
       \"-> (assoc :b 2) :: {:a 1, :b 2}\"
      \"-> (merge {:c 3}) :: {:a 1, :b 2, :c 3}\"]"
  {:added "3.0"}
  [n & funcs]
  (let [wfncs (map #(dbg-print % '->) funcs)]
    `(do (println "\n")
         (println ~n)
         (-> ~n ~@wfncs))))

(defmacro dbg->>
  "prints each stage of the `->>` macro
 
   (->  (dbg->> (range 5)
                (map inc)
                (take 2))
        (with-out-str)
        (string/split-lines))
   => [\"\" \"\"
       \"(0 1 2 3 4)\"
       \"->> (map inc) :: (1 2 3 4 5)\"
      \"->> (take 2) :: (1 2)\"]"
  {:added "3.0"}
  [n & funcs]
  (let [wfncs (map #(dbg-print % '->>) funcs)]
    `(do (println "\n")
         (println ~n)
         (->> ~n ~@wfncs))))

(defmacro ->doto
  "used to perform side-effects within a `->` macro
 
   (-> {:a 1}
       (->doto (update-in [:a] inc) print)
       (assoc :b 2))
   ;; {:a 2}
   => {:a 1, :b 2}"
  {:added "3.0"}
  [x & forms]
  `(do (-> ~x ~@forms)
       ~x))

(defmacro ->>doto
  "used to perform side effects within a `->>` macro
 
   (->> [1 2 3]
        (->>doto (map inc) print)
        (cons 0))
   ;; (2 3 4)
   => [0 1 2 3]"
  {:added "3.0"}
  [& forms]
  (let [[x forms] [(last forms) (butlast forms)]]
    `(do (->> ~x ~@forms)
         ~x)))

(defmacro ->prn
  "used to print within the macro
 
   (-> [1 2 3]
       (->prn)
       (conj 4))
   ;; [1 2 3]
   => [1 2 3 4]"
  {:added "3.0"}
  ([x] `(->prn ~x nil))
  ([x tag]
   `(do (if ~tag
          (print (str ~tag " ")))
        (prn ~x)
        ~x)))
