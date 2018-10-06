(ns hara.string
  (:require [hara.module :as module]
            [hara.string.base.common :as common]
            [hara.string.base.impl :as impl])
  (:refer-clojure :exclude [= subs]))

(defn copy-string-var
  "creates a function, augmenting it with string conversion properties
 
   (string/copy-string-var :op false *ns* '-subs- #'string/subs)
   => #'hara.string-test/-subs-
 
   (-subs- :hello 3)
   => :lo
 
   (-subs- :hello 1 4)
   => :ell"
  {:added "3.0"}
  ([type return ns name ^clojure.lang.Var source]
   (let [func (case type
                :op  (impl/wrap-op @source return)
                :compare (impl/wrap-compare @source))
         sink (intern ns name func)]
     (alter-meta! sink
                  merge
                  (-> (meta source)
                      (dissoc :name :ns)))
     sink)))

(defn =
  "compares two string-like things
 
   (string/= :a 'a)
   => true
 
   (string/= *ns* :hara.string-test)
   => true"
  {:added "3.0"}
  [x y]
  (clojure.core/= (impl/to-string x)
                  (impl/to-string y)))

(defn joinr
  "joins a list together
 
   (string/joinr \".\" [:a :b :c])
   => :a.b.c"
  {:added "3.0"}
  ([arr]
   (joinr arr common/*sep*))
  ([sep arr]
   (impl/from-string (common/joinr sep (map impl/to-string arr))
                     (type (first arr)))))

(module/include
 (hara.string.base.impl      to-string
                             from-string
                             path-separator))

(module/include
 {:fn (partial copy-string-var :compare true)}
 (hara.string.base.common    starts-with?
                             ends-with?
                             includes?
                             caseless=)

 (hara.string.base.type      typeless=))

(module/include
 {:fn (partial copy-string-var :op true)}
 (hara.string.base.common    blank?)
 (hara.string.base.path      path-count))

(module/include
 {:fn (partial copy-string-var :op false)}
 
 (hara.string.base.common    lower-case
                             upper-case
                             capital-case
                             join
                             split
                             split-lines
                             
                             replace
                             reverse
                             
                             trim
                             trim-left
                             trim-right
                             trim-newlines)

 (hara.string.base.type      camel-type
                             capital-type
                             lower-type
                             pascal-type
                             phrase-type
                             snake-type
                             spear-type
                             upper-type)

 (hara.string.base.path      path-split
                             path-join
                             path-stem
                             path-stem-array
                             path-root
                             path-ns
                             path-ns-array
                             path-val
                             path-nth
                             path-sub
                             path-sub-array)

 (clojure.core               subs
                             format))
