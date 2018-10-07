(ns hara.core.version)

(defonce +pattern+
  (->> ["^"
        "(?:(\\d+)\\.)?"                          ;; major
        "(?:(\\d+)\\.)?"                          ;; minor
        "(\\*|\\d+)"                              ;; patch
        "(?:[-_]([\\w\\d-]+(?:\\.[\\w\\d]+)*))?"  ;; qualifier
        "(?:\\+([\\w\\d-]+(?:\\.[\\w\\d-]+)*))?"  ;; build
        "$"]
       (apply str)
       (re-pattern)))

(defonce +qualifiers+
  {"alpha"     0
   "beta"      1
   "milestone" 2
   "rc"        3
   "snapshot"  5
   "final"     6
   "stable"    6})

(defonce +order+
  [:major :minor :incremental :qualifier :release :build])

(defn parse-number
  "parse a number from string input
 
   (parse-number \"1\") => 1"
  {:added "3.0"}
  [s]
  (if s (Long/parseLong s)))

(defn parse-qualifier
  "parses a qualifier from string input
 
   (parse-qualifier \"\" \"\") => 6
 
   (parse-qualifier \"alpha\" \"\") => 0"
  {:added "3.0"}
  [release build]
  (let [qstring (cond (and (empty? release) (not (empty? build)))
                      build

                      (empty? release)
                      "stable"

                      :else
                      (first (keep #(if (.startsWith release %)
                                      %)
                                   (keys +qualifiers+))))]
    (if qstring
      (get +qualifiers+ qstring -1)
      -1)))

(defn parse
  "parses a version input
   (parse \"1.0.0-final\")
   => {:major 1, :minor 0, :incremental 0, :qualifier 6, :release \"final\", :build \"\"}
 
   (parse \"1.0.0-alpha+build.123\")
   => {:major 1,
       :minor 0,
       :incremental 0,
       :qualifier 0,
       :release \"alpha\",
       :build \"build.123\"}"
  {:added "3.0"}
  [s]
  (if-let [elems (first (re-seq +pattern+ s))]
    (let [elems  (rest elems)
          [major minor patch] (keep identity (take 3 elems))
          [release build]     (drop 3 elems)
          release (if release (.toLowerCase release) "")
          build   (if build (.toLowerCase build) "")]
      {:major (parse-number major)
       :minor (parse-number minor)
       :incremental (parse-number patch)
       :qualifier (parse-qualifier release build)
       :release release
       :build build})
    {:release s}))

(defn version
  "like parse but also accepts maps
   
   (version \"1.0-RC5\")
   => {:major 1, :minor 0, :incremental nil, :qualifier 3, :release \"rc5\", :build \"\"}"
  {:added "3.0"}
  [x]
  (cond (string? x)
        (parse x)

        (map? x) x

        :else
        (throw (ex-info "Not a valid input" {:input x}))))

(def order (apply juxt +order+))

(defn equal?
  "compares if two versions are the same
 
   (equal? \"1.2-final\" \"1.2\")
   => true"
  {:added "3.0"}
  [a b]
  (= (dissoc (version a) :release)
     (dissoc (version b) :release)))

(defn newer?
  "returns true if the the first argument is newer than the second
 
   (newer? \"1.2\" \"1.0\")
   => true
 
   (newer? \"1.2.2\" \"1.0.4\")
   => true"
  {:added "3.0"}
  [a b]
  (pos? (compare (order (version a))
                 (order (version b)))))

(defn older?
  "returns true if the the first argument is older than the second
 
   (older? \"1.0-alpha\" \"1.0-beta\")
   => true
 
   (older? \"1.0-rc1\" \"1.0\")
   => true"
  {:added "3.0"}
  [a b]
  (neg? (compare (order (version a))
                 (order (version b)))))

(def ^{:arglists '([a b])}
  not-equal?
  (comp not equal?))

(def ^{:arglists '([a b])}
  not-newer?
  (comp not newer?))

(def ^{:arglists '([a b])}
  not-older?
  (comp not older?))

(def ^:dynamic *lookup*
  {:newer newer?
   :not-newer not-newer?
   :older older?
   :not-older not-older?
   :equal equal?
   :not-equal not-equal?})
