(ns hara.module.namespace.resolve
  (:require [hara.core.base.error :as error]))

(defn resolve-ns
  "resolves the namespace or else returns nil if it does not exist
 
   (resolve-ns 'clojure.core) => 'clojure.core
 
   (resolve-ns 'clojure.core/some) => 'clojure.core
 
   (resolve-ns 'clojure.hello) => nil"
  {:added "3.0"}
  [^clojure.lang.Symbol sym]
  (let [nsp  (.getNamespace sym)
        nsym (or  (and nsp
                       (symbol nsp))
                  sym)]
    (if nsym
      (error/suppress (do (require nsym) nsym)))))

(defn ns-vars
  "lists the vars in a particular namespace
 
   (ns-vars 'hara.module.namespace.resolve) => '[ns-vars resolve-ns]"
  {:added "3.0"}
  [ns]
  (vec (sort (keys (ns-publics ns)))))
