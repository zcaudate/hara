(ns hara.object
  (:require [hara.module :as module]))

(module/include

 (hara.object.element.common          element?
                                      element
                                      context-class)
 (hara.object.element.class           class-convert))

(module/include
 
 (hara.object.element                 to-element
                                      class-info
                                      class-hierarchy
                                      constructor?
                                      method?
                                      field?
                                      static?
                                      instance?
                                      public?
                                      private?
                                      plain?)
                                     
 (hara.object.query                   query-class
                                      query-instance
                                      query-hierarchy
                                      delegate))

(module/include
 
 (hara.object.framework.access        get
                                      get-in
                                      set
                                      keys)
 
 (hara.object.framework.read          meta-read
                                      to-data
                                      to-map
                                      read-getters
                                      read-all-getters
                                      read-fields)
 
 (hara.object.framework.struct        struct-fields
                                      struct-getters
                                      struct-accessor)
 
 (hara.object.framework.write         meta-write
                                      from-data
                                      write-setters
                                      write-all-setters
                                      write-fields)
 
 (hara.object.framework               vector-like
                                      map-like
                                      string-like
                                      unextend))
