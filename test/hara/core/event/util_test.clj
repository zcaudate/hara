(ns hara.core.event.util-test
  (:use hara.test)
  (:require [hara.core.event.util :refer :all]))

^{:refer hara.core.event.util/handler-form :added "3.0"}
(fact "creates a bindings form based on input"

  (handler-form '[name date]
                '(str name " " date))
  => '(clojure.core/fn [{:keys [name date]}] str name " " date))

^{:refer hara.core.event.util/checker-form :added "3.0"}
(fact "creates a checker form"

  (checker-form '_) => hara.core.base.util/T

  (checker-form 'string?) => 'string?)

^{:refer hara.core.event.util/is-special-form :added "3.0"}
(fact "checks if a form is special in term of the condition framework"

  (is-special-form :anticipate '(catch Throwable t))
  => true

  (is-special-form :manage '(on :stuff []))
  => true)

^{:refer hara.core.event.util/parse-option-forms :added "3.0"}
(fact "create a label read the option forms"

  (parse-option-forms '((option :NIL [] nil)))
  => '{:NIL (clojure.core/fn [] nil)})

^{:refer hara.core.event.util/parse-default-form :added "3.0"}
(fact "create option for default behavior"

  (parse-default-form '((default 1000)))
  => [1000])

^{:refer hara.core.event.util/parse-on-handler-forms :added "3.0"}
(fact "create form for handling `manage/on` blocks"

  (parse-on-handler-forms '((on :stuff [] :do-something)))
  => '[{:checker :stuff, :fn (clojure.core/fn [{:keys []}] :do-something)}])

^{:refer hara.core.event.util/parse-on-any-handler-forms :added "3.0"}
(fact "create form for handling `manage/on-any` blocks"

  (parse-on-any-handler-forms '((on-any [] :do-something)))
  => '[{:checker (quote _), :fn (clojure.core/fn [{:keys []}] :do-something)}])

^{:refer hara.core.event.util/parse-try-forms :added "3.0"}
(fact "create form for handling try blocks"

  (parse-try-forms '((catch Throwable t)))
  => '[(catch Throwable t)])

(comment
  (hara.code/import))
