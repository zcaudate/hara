(ns hara.object.element.impl.constructor
  (:require [hara.object.element.common :as common]
            [hara.object.element.impl.type :as type]
            [hara.object.element.util :as util]))

(defmethod common/-invoke-element :constructor [ele & args]
  (let [bargs (util/box-args ele args)]
    (.newInstance ^java.lang.reflect.Constructor (:delegate ele) (object-array bargs))))

(defmethod common/-to-element java.lang.reflect.Constructor [^java.lang.reflect.Constructor obj]
  (let [body (type/seed :constructor obj)]
    (-> body
        (assoc :name "new")
        (assoc :static true)
        (assoc :type (.getDeclaringClass obj))
        (assoc :params (vec (seq (.getParameterTypes obj))))
        (common/element))))

(defmethod common/-format-element :constructor [ele]
  (util/format-element-method ele))

(defmethod common/-element-params :constructor [ele]
  (apply list (util/element-params-method ele)))
