(ns hara.state.group
  (:require [hara.string :as string]
            [hara.data.base.map :as map]))

(defrecord Group [])

(defmethod print-method
  Group
  [{:keys [tag] :as v} ^java.io.Writer w]
  (.write w (str  (if tag
                    (str "#" (string/to-string tag))
                    "")
                  (into {} (:items v)))))

(defn group?
  "checks to see if an element is a group
   (group? people)
   => true"
  {:added "3.0"}
  [x]
  (instance? hara.state.group.Group x))

(defn list-items
  "returns a list of keys to items in the group
 
   (list-items people)
   => [:andy :bob :chris]"
  {:added "3.0"}
  [{:keys [items] :as group}]
  (sort (keys items)))

(defn find-item
  "finds an item based on the given key
   (find-item people :andy)
   => {:name :andy}"
  {:added "3.0"}
  [{:keys [items] :as group} tag]
  (get items tag))

(defn add-item
  "adds an item to the group
   (-> (add-item people {:name :chris})
       (list-items))
   => [:andy :bob :chris]"
  {:added "3.0"}
  [{:keys [constructor key] :as group} data]
  (let [new (constructor data)
        tag (key new)
        ngroup (assoc-in group [:items tag] new)]
    (alter-var-root (:var group) (fn [_] ngroup))))

(defn remove-item
  "removes items based on the key
   (-> (remove-item people :chris)
       (list-items))
   => [:andy :bob]"
  {:added "3.0"}
  [group tag]
  (let [ngroup (update-in group [:items] dissoc tag)]
    (alter-var-root (:var group) (fn [_] ngroup))))

(defn append-items
  "appends a set of data to the group
   (-> (append-items people [{:name :dave} {:name :erin}])
       (list-items))
   => [:andy :bob :dave :erin]
   "
  {:added "3.0"}
  [{:keys [constructor key items] :as group} data]
  (update-in group [:items]
             (fn [items]
               (cond (vector? data)
                     (reduce (fn [out item]
                               (assoc out (key item) (constructor item)))
                             items
                             data)

                     (map? data)
                     (reduce-kv (fn [out k item]
                                  (assoc out k (constructor (assoc item key k))))
                                items
                                data)

                     :else items))))

(defn install-items
  "reads a set of data from a resource and loads it into the group
   (-> (install-items people (java.io.StringReader.
                              \"[{:name :dave} {:name :erin}]\"))
       (list-items))
   => [:andy :bob :dave :erin]
   "
  {:added "3.0"}
  [group file]
  (let [data (-> file
                 slurp
                 read-string)]
    (append-items group data)))

(defn group
  "creates a group from a map
   (group {:tag :hello})
   => #(-> % :tag (= :hello))"
  {:added "3.0"}
  [m]
  (let [{:keys [constructor key items] :as m} (map/merge-nil m {:key :name
                                                                :constructor identity})]
    (-> (dissoc m :items)
        (append-items items)
        (map->Group))))

(defmacro defgroup
  "creates a group of items
   (defgroup people
     {:tag :people
      :constructor map->Person
      :items [{:name :andy}
              {:name :bob}]})
   => (comp group? deref)"
  {:added "3.0"}
  ([name] `(defgroup ~name {}))
  ([name m]
   `(def ~name (-> ~m
                   (map/merge-nil {:var (var ~name)
                                   :tag ~(keyword (str name))})
                   (group)))))

(defmacro defitem
  "adds an item to the group
   (-> (defitem people {:name :chris})
       deref
       list-items)
   => [:andy :bob :chris]
   "
  {:added "3.0"}
  [group data]
  `(do (add-item ~group ~data)
       (:var ~group)))
