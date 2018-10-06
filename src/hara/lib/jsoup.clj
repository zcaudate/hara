(ns hara.lib.jsoup
  (:require [hara.string :as string]
            [hara.data.base.map :as map])
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes Attributes DataNode Document Element Node TextNode)
           (org.jsoup.parser Tag)))

(defmulti node->tree
  "converts a Jsoup node to tree
 
   (-> (parse \"<body><div>hello</div>world</body>\")
       (node->tree))
   => [:body [:div \"hello\"] \"world\"]"
  {:added "3.0"}
  type)

(defmethod node->tree TextNode
  [node]
  (.text node))

(defmethod node->tree DataNode
  [node]
  (.getWholeData node))

(defmethod node->tree Element
  [node]
  (let [children (->> (map node->tree (.childNodes node))
                      (remove #(= % " ")))
        attrs (->> (.attributes node)
                   (map/map-keys keyword))
        children (if (empty? attrs)
                   children
                   (cons attrs children))]
    (apply vector
           (keyword (.getName (.tag node)))
           children)))

(defmethod node->tree Document
  [node]
  (node->tree (first (.children node))))

(defn tree->node
  "converts a tree to a Jsoup node
 
   (tree->node [:body [:div \"hello\"] \"world\"])
   => org.jsoup.nodes.Element"
  {:added "3.0"}
  [rep]
  (cond (vector? rep)
        (let [[tag attrs? & elements] rep
              _ (if-not (keyword? tag)
                  (throw (ex-info "Invalid form:" {:tag tag :form rep})))
              [attrs elements] (if (map? attrs?)
                                 [attrs? elements]
                                 [{} (cons attrs? elements)])
              nattrs    (reduce-kv (fn [out k v]
                                     (let [k (if (keyword? k)
                                               (name k)
                                               (str k))
                                           v (str v)]
                                       (.put out (name k) v)))
                                   (Attributes.)
                                   attrs)
              children (->> elements
                            (filter identity)
                            (mapv tree->node))]

          (doto (Element. (Tag/valueOf (name tag)) "" nattrs)
            (.insertChildren 0 children)))

        (string? rep)
        (DataNode. rep)

        :else
        (throw (ex-info "Only strings or vectors allowed"
                        {:data rep}))))

(defmethod print-method Node
  [v w]
  (.write w (str "#html" (node->tree v))))

(defn parse
  "reads a Jsoup node from string
 
   (parse \"<body><div>hello</div>world</body>\")
   => org.jsoup.nodes.Element"
  {:added "3.0"}
  [^String s]
  (let [s (string/trim s)
        html? (.startsWith s "<html")
        body? (.startsWith s "<body")]
    (cond html?
          (-> (Jsoup/parse s)
              (.children)
              (first))

          body?
          (-> (Jsoup/parseBodyFragment s)
              (.body))

          :else
          (-> (Jsoup/parseBodyFragment s)
              (.body)
              (.children)
              (first)))))

(defn tighten
  "removes lines for elements that contain no internal elements
 
   (tighten \"<b>\\nhello\\n</b>\")
   => \"<b>hello</b>\""
  {:added "3.0"}
  [html]
  (.replaceAll html "(?m)<(\\w+)>\\s*([^<>]*)$\\s*</\\1>" "<$1>$2</$1>"))

(defn generate
  "generates string html for element
 
   (generate (tree->node [:body [:div \"hello\"] \"world\"]))
   => \"<body>\\n    <div>hello</div>world\\n</body>\""
  {:added "3.0"}
  [^Element elem]
  (let [output  (-> (doto (Document. "")
                      (-> (.outputSettings) (.indentAmount 4)))
                    (.appendChild elem)
                    (.html))]
    (tighten output)))

(defn html
  "converts either node or tree representation to a html string
 
   (html [:body [:div \"hello\"] \"world\"])
   => \"<body>\\n    <div>hello</div>world\\n</body>\""
  {:added "3.0"}
  [rep]
  (cond (string? rep)
        rep

        (vector? rep)
        (generate (tree->node rep))

        (instance? Node rep)
        (generate rep)

        :else
        (throw (ex-info "Invalid input" {:input rep}))))

(defn node
  "converts either a string or tree representation to a Jsoup node
   
   (node [:body [:div \"hello\"] \"world\"])
   => org.jsoup.nodes.Element"
  {:added "3.0"}
  [rep]
  (cond (string? rep)
        (parse rep)

        (instance? Node rep)
        rep

        (vector? rep)
        (tree->node rep)

        :else
        (throw (ex-info "Invalid input" {:input rep}))))

(defn tree
  "converts either a string or node representation into a tree
 
   (tree +content+)
   => [:html {:id \"3\"}
       [:head [:title \"First parse\"]]
       [:body [:p \"Parsed HTML into a doc.\"]]]"
  {:added "3.0"}
  [rep]
  (cond (string? rep)
        (node->tree (parse rep))

        (instance? Node rep)
        (node->tree rep)

        (vector? rep) rep

        :else
        (throw (ex-info "Invalid input" {:input rep}))))
