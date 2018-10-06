(ns hara.deploy.analyser.clj
  (:require [clojure.set :as set]
            [hara.deploy.common :as common]))

(defn get-namespaces
  "gets the namespaces of a clojure s declaration
 
   (get-namespaces '(:require repack.util.array
                              [repack.util.data]) [:use :require])
   => '(repack.util.array repack.util.data)
 
   (get-namespaces '(:require [repack.util.array :refer :all])
                   [:use :require])
   => '(repack.util.array)
 
   (get-namespaces '(:require [repack.util
                               [array :as array]
                               data]) [:use :require])
   => '(repack.util.array repack.util.data)"
  {:added "3.0"}
  [form fsyms]
  (when (some #(= % (first form)) fsyms)
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (let [[rns & more] x]
                      (if (or (empty? more)
                              (some keyword? more))
                        [rns]
                        (->> more
                             (map (fn [y] (-> rns
                                              (str "."
                                                   (if (vector? y) (first y) y))
                                              symbol))))))))
            (next form))))

(defn get-imports
  "gets the class imports of a clojure ns declaration
 
   (get-imports '(:import java.lang.String
                          java.lang.Class))
   => '(java.lang.String java.lang.Class)
 
   (get-imports '(:import [java.lang String Class]))
   => '(java.lang.String java.lang.Class)"
  {:added "3.0"}
  [form]
  (when (= :import (first form))
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (map #(symbol (str (first x) "." %))
                         (rest x))))
            (next form))))

(defn get-genclass
  "gets the gen-class of a clojure ns declaration
 
   (get-genclass 'hello '[(:gen-class :name im.chit.hello.MyClass)])
   => '[im.chit.hello.MyClass]
 
   (get-genclass 'hello '[(:import im.chit.hello.MyClass)])
   => nil"
  {:added "3.0"}
  [ns body]
  (if-let [gen-form (->> body
                         (filter (fn [form]
                                   (= :gen-class (first form))))
                         first)]
    [(or (->> gen-form next
              (apply hash-map)
              :name)
         ns)]))

(defn get-defclass
  "gets all the defclass and deftype definitions in a set of forms
 
   (get-defclass 'hello '[(deftype Record [])
                          (defrecord Database [])])
   => '(hello.Record hello.Database)"
  {:added "3.0"}
  [ns forms]
  (->> forms
       (keep (fn [form]
               (and (list? form)
                    ('#{deftype defrecord} (first form))
                    (second form))))
       (map (fn [ele] (symbol (str ns "." ele))))))

(defmethod common/-file-info :clj
  [file]
  (let [[[_ ns & body] & forms]
        (try (read-string (str "[" (slurp file) "]"))
             (catch Throwable t
               (throw (ex-info "Read failed." {:file file}))))]
    {:exports (set/union #{[:clj ns]}
                         (set (map (fn [cls] [:class cls]) (get-genclass ns body)))
                         (set (map (fn [cls] [:class cls]) (get-defclass ns forms))))
     :imports (set/union (->> body
                              (mapcat #(get-namespaces % [:use :require]))
                              (map (fn [clj] [:clj clj]))
                              set)
                         (->> body
                              (mapcat get-imports)
                              (map (fn [clj] [:class clj]))
                              set))}))
