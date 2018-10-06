(ns hara.module.base.include
  (:require [hara.module.base.copy :as copy]
            [hara.module.base.link :as link]))

(defmacro include
  "Imports all or a selection of vars from one namespace to the current one.
 
   (include (hara.core.base.check atom? long?))
   (eval '(long? 1))  => true
   (eval '(atom? 1)) => false
 
   (include
    {:fn (fn [ns sym var]
           (intern ns sym (fn [x] (@var (bigint x)))))}
    (hara.core.base.check bigint?))
   (eval '(bigint? 1)) => true
 
   (include
    {:ns 'clojure.core}
    (hara.core.base.check bigint?))
   => [#'clojure.core/bigint?]
 
   "
  {:added "3.0"}
  [& [opts? & sources]]
  (let [[opts sources] (if (map? opts?)
                         [opts? sources]
                         [{} (cons opts? sources)])]
    `(copy/copy (quote ~sources)
                ~opts)))


(defn link-sources
  "helper function for `link`"
  {:added "3.0"}
  [sources {:keys [ns path resolve] :as opts
            :or {resolve :auto}}]
  (let [sink-ns (or ns (.getName *ns*))]
    (vec (mapcat (fn [[source-ns & vars]]
                   (let [source-ns (if path
                                     (symbol (str path "." source-ns))
                                     source-ns)
                         lookup      (zipmap (map copy/sink-name vars)
                                             (map copy/source-name vars))]
                     (mapv (fn [[sink-name source-name]]
                             (let [lk (link/intern-link sink-ns sink-name {:ns source-ns
                                                                           :name source-name})]
                               (link/bind-init lk resolve)
                               (.sink lk)))
                           lookup)))
                 sources))))

(defmacro link
  "creates links to vars that can be resolved in a controllable manner
   
   (link
    {:resolve :lazy}
    (hara.core.base.check atom?))
   
   (meta #'atom?) => {:name 'atom?, :ns *ns*}"
  {:added "3.0"}
  [& [opts? & sources]]
  (let [[opts sources] (if (map? opts?)
                         [opts? sources]
                         [{} (cons opts? sources)])]
    `(link-sources (quote ~sources)
                   ~opts)))
