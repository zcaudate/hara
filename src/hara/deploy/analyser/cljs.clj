(ns hara.deploy.analyser.cljs
  (:require [clojure.set :as set]
            [hara.deploy.analyser.clj :as clj]
            [hara.deploy.common :as common]))

(defmethod common/-file-info :cljs
  [file]
  (try
    (let [[[_ ns & body] & forms]
          (read-string (str "[" (slurp file) "]"))]
      {:exports #{[:cljs ns]}
       :imports (set/union
                 (->> body
                      (mapcat #(clj/get-namespaces % [:use :require]))
                      (map (fn [clj] [:cljs clj]))
                      set)
                 (->> body
                      (mapcat #(clj/get-namespaces % [:use-macros :require-macros]))
                      (map (fn [clj] [:clj clj]))
                      set))})
    (catch Throwable t
      (prn "FILE FAILED:" file))))

(defmethod common/-file-info :cljc
  [file]
  (let [[[_ ns & body] & forms]
        (try
          (read-string {:read-cond :allow} (str "[" (slurp file) "]"))
          (catch Throwable t
              (throw (ex-info "Read failed." {:file file}))))]
    {:exports #{[:cljs ns] [:clj ns]}
     :imports (set/union
               (->> body
                    (mapcat #(clj/get-namespaces % [:require]))
                    (mapcat (fn [clj] [[:cljs clj] [:clj clj]]))
                    set)
               (->> body
                    (mapcat #(clj/get-namespaces % [:use-macros :require-macros]))
                    (map (fn [clj] [:clj clj]))
                    set))}))
