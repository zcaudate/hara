(ns hara.string.base.common
  (:import (java.util.regex Pattern Matcher)
           clojure.lang.LazilyPersistentVector)
  (:refer-clojure :exclude [replace reverse]))

(def ^:dynamic *sep* "/")

(defn blank?
  "checks if string is empty or nil
 
   (blank? nil)
   => true
 
   (blank? \"\")
   => true"
  {:added "3.0"}
  [^CharSequence s]
  (if s
    (loop [index (int 0)]
      (if (= (.length s) index)
        true
        (if (Character/isWhitespace (.charAt s index))
          (recur (inc index))
          false)))
    true))

(defn ^String split
  "splits the string into tokens
 
   (split \"a b c\" #\" \")
   => [\"a\" \"b\" \"c\"]
 
   (string/split :a.b.c (re-pattern \"\\.\"))
   => [:a :b :c]"
  {:added "3.0"}
  [^CharSequence s re]
  (vec (.split re s -1)))

(defn ^String split-lines
  "splits the string into separate lines
 
   (split-lines \"a\\nb\\nc\")
   => [\"a\" \"b\" \"c\"]"
  {:added "3.0"}
  [^CharSequence s]
  (vec (.split s "\\r?\\n", -1)))

(defn ^String join
  "joins an array using a separator
 
   (join [\"a\" \"b\" \"c\"] \".\")
   => \"a.b.c\"
 
   (join [\"a\" \"b\" \"c\"])
   => \"abc\"
 
   (string/join [:a :b :c] \"-\")
   => :a-b-c"
  {:added "3.0"}
  ([coll]
     (apply str coll))
  ([coll separator]
     (loop [sb (StringBuilder. (str (first coll)))
            more (next coll)
            sep (str separator)]
       (if more
         (recur (-> sb (.append sep) (.append (str (first more))))
                (next more)
                sep)
         (str sb)))))

(defn ^String joinr
  "like `join` but used with `->>` opts
 
   (joinr \".\" [\"a\" \"b\" \"c\"])
   => \"a.b.c\"
 
   (string/joinr \".\" '[a b c])
   => 'a.b.c"
  {:added "3.0"}
  [seperator coll]
  (join coll seperator))

(defn ^String upper-case
  "converts a string object to upper case
 
   (upper-case \"hello-world\")
   => \"HELLO-WORLD\"
 
   (string/upper-case :hello-world)
   => :HELLO-WORLD"
  {:added "3.0"}
  [^CharSequence s]
  (.. s toString toUpperCase))

(defn ^String lower-case
  "converts a string object to lower case
   
   (lower-case \"Hello.World\")
   => \"hello.world\"
 
   (string/lower-case 'Hello.World)
   => 'hello.world"
  {:added "3.0"}
  [^CharSequence s]
  (.. s toString toLowerCase))

(defn ^String capital-case
  "converts a string object to capital case
 
   (capital-case \"hello.World\")
   => \"Hello.world\"
 
   (string/capital-case 'hello.World)
   => 'Hello.world"
  {:added "3.0"}
  [^CharSequence s]
  (let [s (.toString s)]
    (if (< (count s) 2)
      (.toUpperCase s)
      (str (.toUpperCase (subs s 0 1))
           (.toLowerCase (subs s 1))))))

(defn ^String reverse
  "reverses the string
 
   (reverse \"hello\")
   => \"olleh\"
 
   (string/reverse :hello)
   => :olleh"
  {:added "3.0"}
  [^CharSequence s]
  (.toString (.reverse (StringBuilder. s))))

(defn starts-with?
  "checks if string starts with another
 
   (starts-with? \"hello\" \"hel\")
   => true
 
   (string/starts-with? 'hello 'hel)
   => true"
  {:added "3.0"}
  [^CharSequence s ^String substr]
  (.startsWith (.toString s) substr))

(defn ends-with?
  "checks if string ends with another
 
   (ends-with? \"hello\" \"lo\")
   => true
 
   (string/ends-with? 'hello 'lo)
   => true"
  {:added "3.0"}
  [^CharSequence s ^String substr]
  (.endsWith (.toString s) substr))

(defn includes?
  "checks if first string contains the second
 
   (includes? \"hello\" \"ell\")
   => true
 
   (string/includes? 'hello 'ell)
   => true"
  {:added "3.0"}
  [^CharSequence s ^CharSequence substr]
  (.contains (.toString s) substr))

(defn ^String trim
  "trims the string of whitespace
 
   (trim \"   hello   \")
   => \"hello\""
  {:added "3.0"}
  [^CharSequence s]
  (let [len (.length s)]
    (loop [rindex len]
      (if (zero? rindex)
        ""
        (if (Character/isWhitespace (.charAt s (dec rindex)))
          (recur (dec rindex))
          ;; there is at least one non-whitespace char in the string,
          ;; so no need to check for lindex reaching len.
          (loop [lindex 0]
            (if (Character/isWhitespace (.charAt s lindex))
              (recur (inc lindex))
              (.. s (subSequence lindex rindex) toString))))))))

(defn ^String trim-left
  "trims the string of whitespace on left
 
   (trim-left \"   hello   \")
   => \"hello   \""
  {:added "3.0"}
  [^CharSequence s]
  (let [len (.length s)]
    (loop [index 0]
      (if (= len index)
        ""
        (if (Character/isWhitespace (.charAt s index))
          (recur (unchecked-inc index))
          (.. s (subSequence index len) toString))))))

(defn ^String trim-right
  "trims the string of whitespace on right
 
   (trim-right \"   hello   \")
   => \"hello\""
  {:added "3.0"}
  [^CharSequence s]
  (loop [index (.length s)]
    (if (zero? index)
      ""
      (if (Character/isWhitespace (.charAt s (unchecked-dec index)))
        (recur (unchecked-dec index))
        (.. s (subSequence 0 index) toString)))))

(defn ^String trim-newlines
  "removes newlines from right
 
   (trim-newlines  \"\\n\\n    hello   \\n\\n\")
   => \"\\n\\n    hello   \""
  {:added "3.0"}
  [^CharSequence s]
  (loop [index (.length s)]
    (if (zero? index)
      ""
      (let [ch (.charAt s (dec index))]
        (if (or (= ch \newline) (= ch \return))
          (recur (dec index))
          (.. s (subSequence 0 index) toString))))))

(defn- replace-by
  [^CharSequence s re f]
  (let [m (re-matcher re s)]
    (if (.find m)
      (let [buffer (StringBuffer. (.length s))]
        (loop [found true]
          (if found
            (do (.appendReplacement m buffer (Matcher/quoteReplacement (f (re-groups m))))
                (recur (.find m)))
            (do (.appendTail m buffer)
                (.toString buffer)))))
      s)))

(defn ^String replace
  "replace value in string with another
 
   (replace \"hello\" \"el\" \"AL\")
   => \"hALlo\"
 
   (string/replace :hello \"el\" \"AL\")
   => :hALlo"
  {:added "3.0"}
  [^CharSequence s match replacement]
  (let [s (.toString s)]
    (cond 
     (instance? Character match) (.replace s ^Character match ^Character replacement)
     (instance? CharSequence match) (.replace s ^CharSequence match ^CharSequence replacement)
     (instance? Pattern match) (if (instance? CharSequence replacement)
                                 (.replaceAll (re-matcher ^Pattern match s)
                                              (.toString ^CharSequence replacement))
                                 (replace-by s match replacement))
     :else (throw (IllegalArgumentException. (str "Invalid match arg: " match))))))


(defn caseless=
  "compares two values ignoring case
 
   (caseless= \"heLLo\" \"HellO\")
   => true
 
   (string/caseless= 'heLLo :HellO)
   => true"
  {:added "3.0"}
  [x y]
  (= (lower-case x)
     (lower-case y)))
