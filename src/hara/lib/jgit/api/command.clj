(ns hara.lib.jgit.api.command
  (:require [hara.core.base.enum :as enum]
            [hara.core.base.inheritance :as inheritance]
            [hara.string :as string]
            [hara.object :as object]
            [hara.object.query :as reflect]
            [hara.object.element.util :as util]
            [hara.string.base.type :as case]
            [hara.lib.jgit.interop :deps true])
  (:import (org.eclipse.jgit.api Git)))

(defn may-coerce
  "if possible, coerce the value into the type, else throw exception
 
   (may-coerce java.io.File \"path\")
   ;;=> #// \"path\" 
 "
  {:added "3.0"}
  [^Class param arg]
  (let [^Class targ (type arg)
        {:keys [types from-map from-string from-vector]} (object/meta-write param)]
    (cond (.isArray targ)
          (object/from-data arg param)

          (util/param-arg-match param targ) arg

          from-map (from-map arg)
          from-string (from-string arg)
          from-vector (from-vector arg)

          :else
          (throw (Exception. (str "Cannot convert value " arg
                                  " of type " (.getName targ) " to " (.getName param)))))))

(defn apply-with-coercion
  "applies inputs with coercion included
 
   (let [init-command (Git/init)
         set-dir  (reflect/query-class init-command [:# \"setGitDir\"])]
     (->> (apply-with-coercion set-dir [init-command \".git\"])
          (reflect/delegate)
          (into {})))
   => (contains {:directory nil, :bare false, :gitDir (io/file \".git\")})"
  {:added "3.0"}
  ([{:keys [params] :as ele} args]
   (apply ele (map may-coerce params args))))

(defn git-all-commands
  "list all commands that work with git
   (-> (git-all-commands) keys sort)
   => '[:add :apply :archive :blame :branch :checkout
        :cherry :clean :clone :commit :describe :diff
        :fetch :gc :init :log :ls :merge :name :notes
        :pull :push :rebase :reflog :remote :reset
        :revert :rm :stash :status :submodule :tag]
 
   (->> (git-all-commands)
        (filter (fn [[k v]] (not (empty? v))))
        (into {}))
   => {:cherry #{:pick},
       :name #{:rev},
       :submodule #{:deinit :init :update :sync :status :add},
       :ls #{:remote},
       :clone #{:repository},
       :notes #{:remove :list :add :show},
       :remote #{:add :list :remove :set},
       :stash #{:create :drop :list :apply},
       :tag #{:delete :list},
      :branch #{:create :delete :rename :list}}"
  {:added "3.0"}
  []
  (->> (reflect/query-class Git [:name :type])
       (map (fn [m] (assoc m :command (string/to-string (:type m)))))
       (filter (fn [m] (.endsWith ^String (:command m) "Command")))
       (map (fn [m] (-> m :name (case/spear-type) (string/split #"-") (->> (map keyword)))))
       (reduce (fn [m [root sub]]
                 (if sub
                   (update-in m [root] (fnil conj #{}) sub)
                   (assoc-in m [root] #{})))
               {})))

(defn command-options
  "list options for a particular command
 
   (->> (command-options (Git/init))
        (reduce-kv (fn [m k v]
                     (assoc m k (command-input v)))
                   {}))
   => (contains {:git-dir String, :directory String, :bare anything})"
  {:added "3.0"}
  [command]
  (->> (reflect/query-class command [:public :method (type command)])
       (map (fn [ele]
              (let [nm   (case/spear-type (:name ele))
                    op-type (if (= "set" (subs nm 0 3))
                              :single
                              :multi)
                    op-key  (keyword (if (re-find #"((add)|(set)).+" nm)
                                       (subs nm 4)
                                       nm))]
                [op-key {:type  op-type
                         :key   op-key
                         :element ele}])))
       (reduce (fn [m [k val]]
                 (if (or (not (get m k))
                         (-> val :params second object/meta-write (dissoc :class) empty? not))
                   (assoc m k val)
                   m))
               {})))

(defn command-input
  "grabs the input type for a particular command
 
   (-> (Git/init)
       (command-options)
       :bare
       (command-input))
   => anything
 
   (-> (Git/init)
       (command-options)
       :directory
       (command-input))
   => java.lang.String"
  {:added "3.0"}
  [opt]
  (let [param    (-> opt :element :params second)
        {:keys [to-map string/to-string to-vector]} (object/meta-read param)
        out  (cond (-> param
                       (inheritance/ancestor-list)
                       set
                       (get Enum))
                   (->> param enum/enum-values (map object/to-data) set)

                   to-map java.util.Map
                   string/to-string String
                   to-vector java.util.List

                   :else param)]
    (case (:type opt)
      :single out
      :multi [out])))

(defn command-initialize-inputs
  "initialize inputs for a particular command 
   (->> (Git/init)
        (reflect/delegate)
        (into {}))
   => (contains {:directory nil, :bare false, :gitDir nil})
 
   (-> (Git/init)
       (command-initialize-inputs [:bare true
                                   :git-dir  (io/file \".git\")])
       (reflect/delegate)
       (->> (into {})))
   =>  (contains {:directory nil, :bare true, :gitDir (io/file \".git\")})"
  {:added "3.0"}
  [command inputs]
  (let [options (command-options command)]
    (loop [[slug & more] inputs
           command command]
      (cond (nil? slug) command

            :else
            (if-let [field (get options slug)]
              (let [ele (:element field)
                    ptypes (:params ele)
                    pcount (dec (count ptypes))]
                (case (:type field)
                  :single (let [curr (take pcount more)
                                nxt  (drop pcount more)]
                            (recur nxt (apply-with-coercion ele (cons command curr))))
                  :multi  (let [[arr & xs] more
                                arr (if (vector? arr) arr [arr])]
                            (recur xs
                                   (reduce (fn [command entry]
                                             (cond (vector? entry)
                                                   (apply-with-coercion ele (cons command entry))

                                                   :else (apply-with-coercion ele [command entry])))
                                           command arr)))))
              (throw (Exception. (str "Option " slug " is not avaliable: " (-> options keys sort)))))))))

(defn git-element
  "gets an element based on `hara.object.query`
 
   (git-element [:checkout])
   ;;=> #[checkout :: (org.eclipse.jgit.api.Git) -> org.eclipse.jgit.api.CheckoutCommand]
 "
  {:added "3.0"}
  [keywords]
  (->> keywords
       (map string/to-string)
       (string/joinr "-")
       (case/camel-type)
       (vector :#)
       (reflect/query-class Git)))

(defn command
  "returns a command based on keyword input
 
   (command (git-all-commands) [:checkout])
   ;;=> [#[checkout :: (org.eclipse.jgit.api.Git) -> org.eclipse.jgit.api.CheckoutCommand] nil]
 "
  {:added "3.0"}
  [all-commands [input & more]]
  (if-let [subcommands (get all-commands input)]
    (cond (empty? subcommands)
          [(git-element (cons input subcommands)) more]

          (> (count subcommands) 1)
          (if (get subcommands (first more))
            [(git-element (cons input [(first more)])) (rest more)]
            (do (println (str "Options for " input " are: " subcommands))
                subcommands))

          (= (count subcommands) 1)
          [(git-element (cons input subcommands)) more])
    (throw (Exception. (str "Cannot find " input " in the list of Git commands")))))
