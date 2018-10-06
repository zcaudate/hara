(ns hara.state
  (:require [hara.module :as module]))

(module/include
 
 (hara.state.base.common  create
                          clone
                          copy
                          get
                          set
                          empty
                          update
                          update-apply)
                         
 (hara.state.base.cache   cache
                          defcache))
