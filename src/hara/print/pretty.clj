(ns hara.print.pretty
  "Enhanced printing functions for rendering Clojure values. The following
  options are available to control the printer:

  #### General Rendering

  `:width`

  Number of characters to try to wrap pretty-printed forms at.

  `:print-meta`

  If true, metadata will be printed before values. Defaults to the value of
  `*print-meta*` if unset.

  #### Collection Options

  `:sort-keys`

  Print maps and sets with ordered keys. Defaults to true, which will sort all
  collections. If a number, counted collections will be sorted up to the set
  size. Otherwise, collections are not sorted before printing.

  `:map-delimiter`

  The text placed between key-value pairs in a map.

  `:map-coll-separator`

  The text placed between a map key and a collection value. The keyword :line
  will cause line breaks if the whole map does not fit on a single line.

  `:seq-limit`

  If set to a positive number, then lists will only render at most the first n
  elements. This can help prevent unintentional realization of infinite lazy
  sequences.

  #### Color Options

  `:print-color`

  When true, ouptut colored text from print functions.

  `:color-markup`

  :ansi for ANSI color text (the default),
  :html-inline for inline-styled html,
  :html-classes to use the names of the keys in the :color-scheme map
  as class names for spans so styling can be specified via CSS.

  `:color-scheme`

  Map of syntax element keywords to color codes.

  #### Type Handling

  `:print-handlers`

  A lookup function which will return a rendering function for a given class
  type. This will be tried before the built-in type logic. See the
  `hara.print.pretty.dispatch` namespace for some helpful constructors. The returned
  function should accept the current printer and the value to be rendered,
  returning a format document.

  `:print-fallback`

  Keyword argument specifying how to format unknown values. Puget supports a few
  different options:

  - `:pretty` renders values with the default colored representation.
  - `:print` defers to the standard print method by rendering unknown values
    using `pr-str`.
  - `:error` will throw an exception when types with no defined handler are
    encountered.
  - A function value will be called with the current printer options and the
    unknown value and is expected to return a formatting document representing
    it.
  "
  (:require [hara.print.pretty.compare :as compare]
            [hara.print.pretty.engine :as engine]
            [hara.print.pretty.edn :as edn]
            [hara.print.pretty.color :as color]
            [hara.print.pretty.dispatch :as dispatch]
            [hara.protocol.print :as protocol.print]
            [hara.string :as string]
            [hara.data.base.nested :as nested]))

;; ## Control Vars

(defonce +defaults+
  {:width 80
   :sort-keys 80
   :map-delimiter ","
   :map-coll-separator " "
   :print-fallback :pretty
   :print-color true
   :color-markup :ansi
   :color-scheme
   {; syntax elements
    :delimiter [:bold :cyan]
    :tag       [:red]
    
    ; primitive values
    :nil       [:white]
    :boolean   [:white]
    :number    [:white]
    :string    [:cyan]
    :character [:cyan]
    :keyword   [:blue]
    :symbol    [:white]

    ; special types
    :function-symbol [:bold :blue]
    :class-delimiter [:blue]
    :class-name      [:bold :blue]}})

;; ## Formatting Methods

(defn- order-collection
  "Takes a sequence of entries and checks the mode to determine whether to sort
  them. Returns an appropriately ordered sequence."
  [mode value sort-fn]
  (if (or (true? mode)
          (and (number? mode)
               (counted? value)
               (>= mode (count value))))
    (sort-fn value)
    (seq value)))

(defn format-unknown
  "custom printer for an unknown type
 
   (format-unknown (canonical-printer) :hello)
   => (contains-in [:span \"#<\" \"clojure.lang.Keyword\" \"@\" string? '(\" \" \":hello\") \">\"])"
  {:added "3.0"}
  ([printer value]
   (format-unknown printer value (str value)))
  ([printer value repr]
   (format-unknown printer value (.getName (class value)) repr))
  ([printer value tag repr]
   (let [sys-id (Integer/toHexString (System/identityHashCode value))]
     [:span
      (color/-document printer :class-delimiter "#<")
      (color/-document printer :class-name tag)
      (color/-document printer :class-delimiter "@")
      sys-id
      (when (not= repr (str tag "@" sys-id))
        (list " " repr))
      (color/-document printer :class-delimiter ">")])))

(defn format-doc-edn
  "provides a meta-less print formatter
 
   (format-doc-edn (pretty-printer {}) :hello)
   => [:span [:pass \"[34m\"] \":hello\" [:pass \"[0m\"]]"
  {:added "3.0"}
  [printer value]
  (let [lookup (:print-handlers printer)
        handler (and lookup (lookup (class value)))]
    (if handler
      (handler printer value)
      (edn/visit-edn printer value))))

(defn format-doc
  "provides a format given a printer and value
 
   (format-doc (canonical-printer) :hello)
   => \":hello\"
 
   (format-doc (pretty-printer {}) :hello)
   => [:span [:pass \"[34m\"] \":hello\" [:pass \"[0m\"]]"
  {:added "3.0"}
  [printer value]
  (if-let [metadata (meta value)]
    (edn/visit-meta printer metadata value)
    (format-doc-edn printer value)))

;; ## Type Handlers

(defn pr-handler
  "creates a print handler for printing strings
 
   (pr-handler (canonical-printer) [1 2 3 4])
   => \"[1 2 3 4]\""
  {:added "3.0"}
  [printer value]
  (pr-str value))

(defn unknown-handler
  "creates a custom handler for an unknown object
 
   (unknown-handler (canonical-printer) (Thread/currentThread))
   => throws"
  {:added "3.0"}
  [printer value]
  (edn/visit-unknown printer value))

(defn tagged-handler
  "creates a custom handler for a tagged literal
 
   ((tagged-handler 'object (fn [x] (map inc x)))
    (canonical-printer {})
    [1 2 3 4])
   => [:span \"#object\" \" \" [:group \"(\" [:align '(\"2\" \" \" \"3\" \" \" \"4\" \" \" \"5\")] \")\"]]"
  {:added "3.0"}
  [tag value-fn]
  (when-not (symbol? tag)
    (throw (ex-info (str "Cannot create tagged handler with non-symbol tag "
                         (pr-str tag))
                    {:tag tag, :value-fn value-fn})))
  (when-not (ifn? value-fn)
    (throw (ex-info (str "Cannot create tagged handler for " tag
                         " with non-function value transform")
                    {:tag tag, :value-fn value-fn})))
  (fn handler
    [printer value]
    (format-doc printer (tagged-literal tag (value-fn value)))))


(def java-handlers
  "Map of print handlers for Java types. This supports syntax for regular
  expressions, dates, UUIDs, and futures."
  {java.lang.Class
   (fn class-handler
     [printer value]
     (format-unknown printer value "Class" (.getName ^Class value)))

   java.util.concurrent.Future
   (fn future-handler
     [printer value]
     (let [doc (if (future-done? value)
                 (format-doc printer @value)
                 (color/-document printer :nil "pending"))]
       (format-unknown printer value "Future" doc)))

   java.util.Date
   (tagged-handler
     'inst
     #(-> "yyyy-MM-dd'T'HH:mm:ss.SSS-00:00"
          (java.text.SimpleDateFormat.)
          (doto (.setTimeZone (java.util.TimeZone/getTimeZone "GMT")))
          (.format ^java.util.Date %)))

   java.util.UUID
   (tagged-handler 'uuid str)})

(def clojure-handlers
  "Map of print handlers for 'primary' Clojure types. These should take
  precedence over the handlers in `clojure-interface-handlers`."
  {clojure.lang.Atom
   (fn atom-handler
     [printer value]
     (format-unknown printer value "Atom" (format-doc printer @value)))

   clojure.lang.Delay
   (fn delay-handler
     [printer value]
     (let [doc (if (realized? value)
                 (format-doc printer @value)
                 (color/-document printer :nil "pending"))]
       (format-unknown printer value "Delay" doc)))

   clojure.lang.ISeq
   (fn iseq-handler
     [printer value]
     (edn/visit-seq printer value))})


(def clojure-interface-handlers
  "Fallback print handlers for other Clojure interfaces."
  {clojure.lang.IPending
   (fn pending-handler
     [printer value]
     (let [doc (if (realized? value)
                 (format-doc printer @value)
                 (color/-document printer :nil "pending"))]
       (format-unknown printer value doc)))

   clojure.lang.Fn
   (fn fn-handler
     [printer value]
     (let [doc (let [[vname & tail] (-> (.getName (class value))
                                        (.replaceFirst "$" "/")
                                        (string/split #"\$"))]
                 (if (seq tail)
                   (str vname "["
                        (->> tail
                             (map #(first (string/split % #"__")))
                             (string/joinr "/"))
                        "]")
                   vname))]
       (format-unknown printer value "Fn" doc)))})


(def common-handlers
  "Print handler dispatch combining Java and Clojure handlers with inheritance
  lookups. Provides a similar experience as the standard Clojure
  pretty-printer."
  (dispatch/chained-lookup
    (dispatch/inheritance-lookup java-handlers)
    (dispatch/inheritance-lookup clojure-handlers)
    (dispatch/inheritance-lookup clojure-interface-handlers)))


;; ## Canonical Printer Implementation

(defrecord CanonicalPrinter
  [print-handlers]

  protocol.print/IVisitor

  ; Primitive Types

  (-visit-nil
    [this]
    "nil")

  (-visit-boolean
    [this value]
    (str value))

  (-visit-number
    [this value]
    (pr-str value))

  (-visit-character
    [this value]
    (pr-str value))

  (-visit-string
    [this value]
    (pr-str value))

  (-visit-keyword
    [this value]
    (str value))

  (-visit-symbol
    [this value]
    (str value))


  ; Collection Types

  (-visit-seq
    [this value]
    (let [entries (map (partial format-doc this) value)]
      [:group "(" [:align (interpose " " entries)] ")"]))

  (-visit-vector
    [this value]
    (let [entries (map (partial format-doc this) value)]
      [:group "[" [:align (interpose " " entries)] "]"]))

  (-visit-set
    [this value]
    (let [entries (map (partial format-doc this)
                       (sort compare/compare value))]
      [:group "#{" [:align (interpose " " entries)] "}"]))

  (-visit-map
    [this value]
    (let [entries (map #(vector :span (format-doc this (key %))
                                " "   (format-doc this (val %)))
                       (sort-by first compare/compare value))]
      [:group "{" [:align (interpose " " entries)] "}"]))


  ; Clojure Types

  (-visit-meta
    [this metadata value]
    ; Metadata is not printed for canonical rendering.
    (format-doc-edn this value))

  (-visit-var
    [this value]
    ; Defer to unknown, cover with handler.
    (edn/visit-unknown this value))

  (-visit-pattern
    [this value]
    ; Defer to unknown, cover with handler.
    (edn/visit-unknown this value))

  (-visit-record
    [this value]
    ; Defer to unknown, cover with handler.
    (edn/visit-unknown this value))


  ; Special Types

  (-visit-tagged
    [this value]
    [:span (str "#" (:tag value)) " " (format-doc this (:form value))])

  (-visit-unknown
    [this value]
    (throw (IllegalArgumentException.
             (str "No defined representation for " (class value) ": "
                  (pr-str value))))))

(defn canonical-printer
  "constructs a canonical printer
 
   (canonical-printer {})
   => hara.print.pretty.CanonicalPrinter"
  {:added "3.0"}
  ([]
   (canonical-printer nil))
  ([handlers]
   (assoc (CanonicalPrinter. handlers)
          :width 0)))


;; ## Pretty Printer Implementation

(defrecord PrettyPrinter
  [width
   print-meta
   sort-keys
   map-delimiter
   map-coll-separator
   seq-limit
   print-color
   color-markup
   color-scheme
   print-handlers
   print-fallback]

  protocol.print/IVisitor

  ; Primitive Types

  (-visit-nil
    [this]
    (color/-document this :nil "nil"))

  (-visit-boolean
    [this value]
    (color/-document this :boolean (str value)))

  (-visit-number
    [this value]
    (color/-document this :number (pr-str value)))

  (-visit-character
    [this value]
    (color/-document this :character (pr-str value)))

  (-visit-string
    [this value]
    (color/-document this :string (pr-str value)))

  (-visit-keyword
    [this value]
    (color/-document this :keyword (str value)))

  (-visit-symbol
    [this value]
    (color/-document this :symbol (str value)))


  ; Collection Types

  (-visit-seq
    [this value]
    (let [[values trimmed?]
          (if (and seq-limit (pos? seq-limit))
            (let [head (take seq-limit value)]
              [head (<= seq-limit (count head))])
            [(seq value) false])
          elements
          (cond-> (if (symbol? (first values))
                    (cons (color/-document this :function-symbol (str (first values)))
                          (map (partial format-doc this) (rest values)))
                    (map (partial format-doc this) values))
            trimmed? (concat [(color/-document this :nil "...")]))]
      [:group
       (color/-document this :delimiter "(")
       [:align (interpose :line elements)]
       (color/-document this :delimiter ")")]))

  (-visit-vector
    [this value]
    [:group
     (color/-document this :delimiter "[")
     [:align (interpose :line (map (partial format-doc this) value))]
     (color/-document this :delimiter "]")])

  (-visit-set
    [this value]
    (let [entries (order-collection sort-keys value (partial sort compare/compare))]
      [:group
       (color/-document this :delimiter "#{")
       [:align (interpose :line (map (partial format-doc this) entries))]
       (color/-document this :delimiter "}")]))

  (-visit-map
    [this value]
    (let [ks (order-collection sort-keys value (partial sort-by first compare/compare))
          entries (map (fn [[k v]]
                         [:span
                          (format-doc this k)
                          (if (coll? v)
                            map-coll-separator
                            " ")
                          (format-doc this v)])
                       ks)]
      [:group
       (color/-document this :delimiter "{")
       [:align (interpose [:span map-delimiter :line] entries)]
       (color/-document this :delimiter "}")]))


  ; Clojure Types

  (-visit-meta
    [this metadata value]
    (if print-meta
      [:align
       [:span (color/-document this :delimiter "^") (format-doc this metadata)]
       :line (format-doc-edn this value)]
      (format-doc-edn this value)))

  (-visit-var
    [this value]
    [:span
     (color/-document this :delimiter "#'")
     (color/-document this :symbol (subs (str value) 2))])

  (-visit-pattern
    [this value]
    [:span
     (color/-document this :delimiter "#")
     (color/-document this :string (str \" value \"))])

  (-visit-record
    [this value]
    (edn/visit-tagged
      this
      (tagged-literal (symbol (.getName (class value)))
                      (into {} value))))


  ; Special Types

  (-visit-tagged
    [this value]
    (let [{:keys [tag form]} value]
      [:group
       (color/-document this :tag (str "#" (:tag value)))
       (if (coll? form) :line " ")
       (format-doc this (:form value))]))

  (-visit-unknown
    [this value]
    (case print-fallback
      :pretty
        (format-unknown this value)

      :print
        [:span (pr-str value)]

      :error
        (throw (IllegalArgumentException.
                 (str "No defined representation for " (class value) ": "
                      (pr-str value))))

      (if (ifn? print-fallback)
        (print-fallback this value)
        (throw (IllegalStateException.
                 (str "Unsupported value for print-fallback: "
                      (pr-str print-fallback))))))))


(defn pretty-printer
  "constructs a pretty printer
 
   (pretty-printer {})
   => hara.print.pretty.PrettyPrinter"
  {:added "3.0"}
  [opts]
  (->> [{:print-meta *print-meta*
         :print-handlers common-handlers}
        +defaults+
        opts]
       (reduce nested/merge-nested)
       (map->PrettyPrinter)))

;; ## Printing Functions

(defn render-out
  "helper to pprint and pprint-str
 
   (with-out-str
     (render-out (canonical-printer)
                 {:a 1 :b 2 :c (range 5)}))
   => \"{:a 1 :b 2 :c (0 1 2 3 4)}\""
  {:added "3.0"}
  [printer value]
  (binding [*print-meta* false]
    (engine/pprint-document
     (format-doc printer value)
     {:width (:width printer)})))

(defn pprint
  "pretty prints with options
 
   (pprint {:a 1 :b 2 :c (range 5)}
           {:width 10})"
  {:added "3.0"}
  ([value]
   (pprint value nil))
  ([value opts]
   (render-out (pretty-printer (merge {:print-color true} opts))
               value)))

(defn pprint-str
  "returns the string that is printed
 
   (pprint-str {:a 1 :b 2 :c (range 5)}
               {:width 10})"
  {:added "3.0"}
  ([value]
   (pprint-str value nil))
  ([value opts]
   (string/trim-newlines
    (with-out-str
      (render-out (pretty-printer (merge {:print-color true} opts))
                  value)))))
