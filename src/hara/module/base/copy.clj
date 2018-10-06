(ns hara.module.base.copy
  (:require [hara.function.base.invoke :refer [definvoke]]
            [hara.module.base.link :as link]))

(defn- select-sym
  [select obj]
  (cond (symbol? obj)
        obj
        
        (sequential? obj)
        (select obj)

        :else (throw (ex-info "Invalid input." {:input obj}))))

(definvoke source-name
  "returns the source var
 
   (transfer-source '[sink < source])
   => 'source"
  {:added "3.0"}
  [:compose {:arglists '([obj])
             :val (partial select-sym last)}])

(definvoke sink-name
  "returns the destination var
 
   (transfer-dest '[dest < source])
   => 'dest"
  {:added "3.0"}
  [:compose {:arglists '([obj])
             :val (partial select-sym first)}])

(defn copy-single
  "Imports a single var from to a given namespace
 
   (copy-single *ns* 'ifl #'clojure.core/if-let)
   => anything ; #'hara.module-test/ifl
   (eval '(ifl [a 1] (inc a))) => 2"
  {:added "3.0"}
  [sink-ns sink-name ^clojure.lang.Var source-var]
  (if (and source-var (.hasRoot source-var))
    (intern sink-ns
            (with-meta sink-name (merge (meta source-var)
                                        (meta sink-name)))
            @source-var)
    (throw (ex-info "Input not found." {:ns sink-ns
                                        :name sink-name
                                        :var source-var}))))

(defn unmap-vars
  "unmaps a set of vars from a given namespace
 
   (unmap-vars *ns* ['ifl])
   => '[ifl]"
  {:added "3.0"}
  [ns vars]
  (let [current  (ns-map ns)
        existing (filter current vars)]
    (mapv #(do (ns-unmap ns %)
               %)
          existing)))

(defn copy-vars
  "copies vars from one namespace to another
 
   (copy-vars 'hara.core.base.check '[bytes? atom?] *ns* copy-single)
   => [#'bytes?
       #'atom?]"
  {:added "3.0"}
  ([source-ns] (copy-vars source-ns nil))
  ([source-ns names] (copy-vars source-ns names *ns* copy-single))
  ([source-ns names sink-ns copy-fn]
   (let [source-lu    (ns-publics source-ns)
         names        (or names (keys source-lu))
         name-lu      (zipmap (map sink-name names)
                              (map source-name names))]
     (unmap-vars sink-ns names)
     (mapv (fn [[nsink nsource]]
             (if-let [source-var (get source-lu nsource)]
               (copy-fn sink-ns nsink source-var)
               (throw (ex-info "Var does not exist." {:ns source-ns
                                                      :name nsource}))))
           name-lu))))

(defn copy
  "copies a set of vars from multiple locations
 
   (copy '[[hara.core.base.check bytes? (bytes?2 < bytes?)]
           [hara.core.base.util T F]])
   => [#'bytes? #'bytes?2 #'T #'F]"
  {:added "3.0"}
  ([sources]
   (copy sources {}))
  ([sources {:keys [ns path] :as opts}]
   (vec (mapcat (fn [[source-ns & vars]]
                  (let [source-ns (if path
                                    (symbol (str path "." source-ns))
                                    source-ns)]
                    (require source-ns)
                    (copy-vars source-ns
                               vars
                               (or ns *ns*)
                               (or (:fn opts) copy-single))))
                sources))))
