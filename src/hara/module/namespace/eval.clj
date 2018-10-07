(ns hara.module.namespace.eval)

(defn eval-ns
  "Evaluates a list of forms in an existing namespace
   (eval-ns 'hara.core.base.check
            '[(long? 1)])
   => true"
  {:added "3.0"}
  [ns forms]
  (binding [*ns* (the-ns ns)]
    (->> forms
         (mapv (fn [form] (eval form)))
         last)))

(defmacro with-ns
  "Evaluates `body` forms in an existing namespace given by `ns`.
 
   (require '[hara.core.base.check])
   (with-ns 'hara.core.base.check
     (long? 1)) => true"
  {:added "3.0"}
  [ns & forms]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) forms)))

(defn eval-temp-ns
  "Evaluates a list of forms in a temporary namespace
   (eval-temp-ns
    '[(def  inc1 inc)
      (defn inc2 [x] (+ 1 x))
      (-> 1 inc1 inc2)])
   => 3
 
   \"All created vars will be destroyed after evaluation.\"
 
   (resolve 'inc1) => nil"
  {:added "3.0"}
  [forms]
  (let [sym (gensym)]
    (try
      (create-ns sym)
      (eval-ns sym (cons '(clojure.core/refer-clojure) forms))
      (finally (remove-ns sym)))))

(defmacro with-temp-ns
  "Evaluates `body` forms in a temporary namespace.
 
   (with-temp-ns
     (def  inc1 inc)
     (defn inc2 [x] (+ 1 x))
     (-> 1 inc1 inc2))
   => 3
 
   \"All created vars will be destroyed after evaluation.\"
 
   (resolve 'inc1) => nil"
  {:added "3.0"}
  [& forms]
  `(try
     (create-ns 'sym#)
     (let [res# (with-ns 'sym#
                  (clojure.core/refer-clojure)
                  ~@forms)]
       res#)
     (finally (remove-ns 'sym#))))
