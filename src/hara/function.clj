(ns hara.function
  (:require [hara.module :as module])
  (:refer-clojure :exclude [memoize]))

(module/include
 
 (hara.function.base.arg          arg-check
                                  arg-count
                                  varg-count
                                  vargs?)
 
 (hara.function.base.dispatch     invoke
                                  call
                                  message 
                                  op)
 
 (hara.function.base.executive    defexecutive)
 
 (hara.function.base.hook         list-patched
                                  patch
                                  patched?
                                  unpatch)

 (hara.function.base.invoke       definvoke
                                  form-arglists)
 
 (hara.function.base.macro        create-args
                                  create-def-form
                                  defcompose
                                  lookup
                                  deflookup)
 
 (hara.function.base.memoize      memoize
                                  memoize-clear
                                  memoize-remove
                                  defmemoize)
 
 (hara.function.base.multi        multi?
                                  multi-clone
                                  multi-keys
                                  multi-has?
                                  multi-list
                                  multi-get
                                  multi-remove
                                  multi-match?
                                  multi-add))

(module/link

 (hara.function.task              task
                                  deftask)
 
 (hara.function.procedure         procedure
                                  defprocedure
                                  procedure-kill
                                  procedure-running?))

(comment
  (hara.code/scaffold)
  (hara.module/reset '[hara])
  (hara.module.namespace/reset '[hara.object]))


