(ns hara.core.event.util
  (:require [hara.core.base.check :as check]
            [hara.core.base.util :as primitive]))

(defn handler-form
  "creates a bindings form based on input
 
   (handler-form '[name date]
                 '(str name \" \" date))
   => '(clojure.core/fn [{:keys [name date]}] str name \" \" date)"
  {:added "3.0"}
  [bindings body]
  (let [bind (cond (vector? bindings)    [{:keys bindings}]
                   (check/hash-map? bindings)  [bindings]
                   (symbol? bindings)    [bindings]
                   :else (throw (ex-info "Bindings should be a vector, hashmap or symbol"
                                         {:bindings bindings})))]
    `(fn ~bind ~@body)))

(defn checker-form
  "creates a checker form
 
   (checker-form '_) => hara.core.base.util/T
 
   (checker-form 'string?) => 'string?"
  {:added "3.0"}
  [checker]
  (if (= '_ checker)
    `primitive/T
    checker))

(def sp-forms {:anticipate '#{catch finally}
               :raise      '#{option default catch finally}
               :raise-on   '#{option default catch finally}
               :manage     '#{on on-any option}})

(defn is-special-form
  "checks if a form is special in term of the condition framework
 
   (is-special-form :anticipate '(catch Throwable t))
   => true
 
   (is-special-form :manage '(on :stuff []))
   => true"
  {:added "3.0"}
  ([k form]
   (and (instance? clojure.lang.ISeq form)
        (symbol? (first form))
        (contains? (sp-forms k) (first form))))
  ([k form syms]
   (if (list? form)
     (or (get syms (first form)) (is-special-form k form)))))

(defn parse-option-forms
  "create a label read the option forms
 
   (parse-option-forms '((option :NIL [] nil)))
   => '{:NIL (clojure.core/fn [] nil)}"
  {:added "3.0"}
  [forms]
  (into {}
        (for [[type key & body] forms
              :when (= type 'option)]
          [key `(fn ~@body)])))

(defn parse-default-form
  "create option for default behavior
 
   (parse-default-form '((default 1000)))
   => [1000]"
  {:added "3.0"}
  [forms]
  (if-let [default (->> forms
                        (filter
                         (fn [[type]]
                           (= type 'default)))
                        (last)
                        (next))]
    (vec default)))

(defn parse-on-handler-forms
  "create form for handling `manage/on` blocks
 
   (parse-on-handler-forms '((on :stuff [] :do-something)))
   => '[{:checker :stuff, :fn (clojure.core/fn [{:keys []}] :do-something)}]"
  {:added "3.0"}
  [forms]
  (vec (for [[type chk bindings & body] forms
             :when (= type 'on)]
         (let [chk (if (= chk '_)
                     (quote '_)
                     chk)]
           {:checker chk
            :fn (handler-form bindings body)}))))

(defn parse-on-any-handler-forms
  "create form for handling `manage/on-any` blocks
 
   (parse-on-any-handler-forms '((on-any [] :do-something)))
   => '[{:checker (quote _), :fn (clojure.core/fn [{:keys []}] :do-something)}]"
  {:added "3.0"}
  [forms]
  (vec (for [[type bindings & body] forms
             :when (= type 'on-any)]
         {:checker (quote '_)
          :fn (handler-form bindings body)})))

(defn parse-try-forms
  "create form for handling try blocks
 
   (parse-try-forms '((catch Throwable t)))
   => '[(catch Throwable t)]"
  {:added "3.0"}
  [forms]
  (vec (for [[type & body :as form] forms
             :when (#{'finally 'catch} type)]
         form)))
