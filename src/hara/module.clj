(ns hara.module
  (:require [hara.module.base.include :as include]))

(include/include 

 (hara.module.base.include    include
                              link)

 (hara.module.base.link       registered-link?
                              registered-links
                              unresolved-links
                              resolve-links)

 (hara.module.base.extend     extend-all)
 (hara.module.base.abstract   extend-abstract
                              extend-implementations))

(include/link
 
 (hara.module.namespace       list-aliases
                              clear-aliases
                              list-imports
                              list-mappings
                              clear-mappings
                              
                              list-interns
                              clear-interns
                              list-publics
                              list-refers
                              clear-refers
                              clear
                              list-in-memory
                              reset
                              unmap
                              unalias))

(comment
  (resolve-links)
  (unresolved-links)
  (clear-interns)
  (list-interns)
  (list-refers)
  (clear-refers))
