(ns hara.data.base.common
  (:require [clojure.core :as clojure]
            [hara.protocol.data :as protocol.data])
  (:refer-clojure :exclude [assoc conj]))

(defn assoc
  ([m k v])
  ([m k v & more]))

(defn conj
  ([coll v])
  ([coll v & more]))
