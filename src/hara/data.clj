(ns hara.data
  (:require [hara.module :as module]))

(module/link

 (hara.data.base.combine   combine
                           decombine)

 (hara.data.base.complex   assocs
                           dissocs
                           gets
                           merges
                           merges-nested
                           gets-in
                           dissocs-in)

 (hara.data.base.diff      diff
                           changed
                           patch
                           unpatch)

 (hara.data.base.map       dissoc-in
                           unique
                           assoc-if
                           assoc-in-if
                           update-in-if
                           merge-if
                           into-if
                           select-keys-if
                           merge-nil
                           assoc-in-nil
                           transform-in
                           retract-in
                           map-keys
                           map-vals
                           map-entries
                           transpose)
                          
 (hara.data.base.nested    keys-nested
                           key-paths
                           update-keys-in
                           update-vals-in
                           merge-nested
                           merge-nil-nested
                           dissoc-nested
                           unique-nested
                           clean-nested)
                          
 (hara.data.base.seq       positions
                           remove-index
                           index-of
                           element-of
                           flatten-all))
