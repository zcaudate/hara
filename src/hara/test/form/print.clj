(ns hara.test.form.print
  (:require [hara.string.base.ansi :as ansi]
            [hara.io.file :as fs]
            [hara.test.checker.base :as checker]
            [hara.test.common :as common]))

(defn- rel
  [path]
  (cond (and common/*root* path)
        (fs/relativize common/*root* path)

        :else path))

(defn print-success
  "outputs the description for a successful test"
  {:added "3.0"}
  [{:keys [path name ns line desc form check] :as summary}]
  (let [line (if line (str "L:" line " @ ") "")]
    (println
     "\n"
     (str (ansi/style "Success" #{:green :bold})
          (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
          (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
          (if desc (str "\n    " (ansi/white "Info") "  \"" desc "" \") "")
          (str "\n    " (ansi/white "Form") "  " form)
          (str "\n   " (ansi/white "Check") "  " check)))))

(defn print-failure
  "outputs the description for a failed test"
  {:added "3.0"}
  [{:keys [path name ns line desc form check actual] :as summary}]
  (let [line (if line (str "L:" line " @ ") "")]
    (println
     "\n"
     (str (ansi/style "Failure" #{:red :bold})
          (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
          (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
          (if desc (str "\n    " (ansi/white "Info") "  \"" desc "" \") "")
          (str "\n    " (ansi/white "Form") "  " form)
          (str "\n   " (ansi/white "Check") "  " check)
          (str "\n  " (ansi/white "Actual") "  " (if (sequential? actual)
                                                   (vec actual)
                                                   actual))))))

(defn print-thrown
  "outputs the description for a form that throws an exception"
  {:added "3.0"}
  [{:keys [path name ns line desc form] :as summary}]
  (let [line (if line (str "L:" line " @ ") "")]
    (println
     "\n"
     (str (ansi/style " Thrown" #{:yellow :bold})
          (ansi/style (format "  %s%s" line (or (rel path) "<current>")) #{:bold})
          (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:bold})) "")
          (if desc (str "\n    " (ansi/white "Info") "  \"" desc "" \") "")
          (str "\n    " (ansi/white "Form") "  " form)))))

(defn print-fact
  "outputs the description for a fact form that contains many statements"
  {:added "3.0"}
  [{:keys [path name ns line desc refer] :as meta}  results]
  (let [name   (if name (str name " @ ") "")
        line   (if line (str ":" line) "")
        all    (->> results (filter #(-> % :from (= :verify))))
        passed (->> all (filter checker/succeeded?))
        num    (count passed)
        total  (count all)
        ops    (->> results (filter #(-> % :from (= :evaluate))))
        errors (->> ops (filter #(-> % :type (= :exception))))
        thrown (count errors)]
    (if (or (common/*print* :print-facts-success)
            (not (and (= num total)
                      (pos? thrown))))
      (println
       "\n"
       (str (ansi/style "   Fact" #{:blue :bold})
            (ansi/style (str "  [" (or path "<current>") line "]") #{:bold})
            (if name (str "\n   " (ansi/white "Refer") "  " (ansi/style name #{:highlight :bold})) "")
            (if desc (str "\n    " (ansi/white "Info") "  \"" desc "" \") "")
            (str "\n  " (ansi/white "Passed") "  "
                 (str (ansi/style num (if (= num total) #{:blue} #{:green}))
                      " of "
                      (ansi/blue total)))
            (if (pos? thrown)
              (str "\n  " (ansi/white "Thrown") "  " (ansi/yellow thrown))
              ""))))))

(defn print-summary
  "outputs the description for an entire test run"
  {:added "3.0"}
  [{:keys [files thrown facts checks passed failed] :as result}]
  (println
   "\n"
   (str (ansi/style (str "Summary (" files ")") #{:blue :bold})
        (str "\n  " (ansi/white " Files") "  " (ansi/blue files))
        (str "\n  " (ansi/white " Facts") "  " (ansi/blue facts))
        (str "\n  " (ansi/white "Checks") "  " (ansi/blue checks))
        (str "\n  " (ansi/white "Passed") "  " ((if (= passed checks)
                                                  ansi/blue
                                                  ansi/yellow) passed))
        (str "\n  " (ansi/white "Thrown") "  " ((if (pos? thrown)
                                                  ansi/yellow
                                                  ansi/blue) thrown))))

  (if (pos? failed)
    (println
     "\n"
     (ansi/style (str "Failed  (" failed ")") #{:red :bold}))

    (println
     "\n"
     (ansi/style (str "Success (" passed ")") #{:cyan :bold})))
  (println ""))

