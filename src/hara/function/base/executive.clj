(ns hara.function.base.executive
  (:require [hara.function.base.macro :as macro]))

(defonce +invoke-args-number+ 17)

(defn create-invoke-form
  "creates an invoke form given params
 
   (create-invoke-form '-hello- 'obj 3)
   => '(invoke [obj a0 a1 a2] (-hello- obj a0 a1 a2))"
  {:added "3.0"}
  [method this n]
  (let [args (map #(symbol (str "a" %)) (range n))]
    `(~'invoke [~this ~@args] (~method ~this ~@args))))

(defn create-executive-form
  "creates the form for defexecutive
 
   (create-executive-form '-EXE-
                          '[name place date]
                          {:type 'deftype
                           :tag \"hello\"
                           :invoke   'hello-invoke
                           :display  'hello-display
                           :print    'hello-print
                           :args-number 3}
                         [])
   => '[(deftype -EXE- [name place date]
          java.lang.Object
          (toString [hello]
            (clojure.core/str \"#\" \"hello\" (hello-display hello)))
          
          clojure.lang.IFn
          (invoke [hello] (hello-invoke hello))
          (invoke [hello a0] (hello-invoke hello a0))
          (invoke [hello a0 a1] (hello-invoke hello a0 a1))
          (applyTo [hello args] (clojure.core/apply hello-invoke hello args)))
        
        (clojure.core/defmethod clojure.core/print-method (clojure.core/resolve (quote -EXE-))
          [hello writer]
          (.write writer (clojure.core/str (hello-print hello))))]"
  {:added "3.0"}
  [name fields settings body]
  (let [{:keys [type
                tag
                display
                status
                print
                invoke
                this
                as-object
                args-number
                refresh]} settings
        type   (or type 'deftype)
        this   (or (if tag (symbol tag)) 'this)
        args-number (or args-number +invoke-args-number+)]
    (if (or refresh (not (resolve name)))
      `[(~type ~name ~fields
         Object
         (~'toString [~this]
          (str "#" ~tag
               ~@(if status [`(~status ~this)])
               (~display ~this)))
         
         clojure.lang.IFn
         ~@(map (fn [n] (create-invoke-form invoke this n)) (range args-number))
         (~'applyTo [~this ~'args]
          (apply ~invoke ~this ~'args))
         
         ~@body)
        
        (defmethod print-method (resolve (quote ~name))
          [~this ~'writer]
          (.write ~'writer 
                  (str ~(if print
                          `(~print ~this)
                          this))))])))

(defmacro defexecutive
  "creates an executable data type
 
   (declare hello-display hello-print)
   
   (def hello-invoke (fn [this & args]
                       (str (.name this) \" \" (apply + args))))
   
   (defexecutive -Hello- [name place date]
     {:tag \"hello\"
      :invoke   hello-invoke
      :display  hello-display
      :print    hello-print})
 
   ((-Hello-. \"hello\" nil nil) 1 2 3 4 5)
   => \"hello 15\""
  {:added "3.0"}
  [name doc? attrs? & [fields settings & body]]
  (let [[doc attrs fields {:keys [keep?] :as settings} & body]
        (macro/create-args (concat [doc? attrs? fields settings]
                                   body))]
    (create-executive-form name fields settings body))) 
