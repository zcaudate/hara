(ns hara.lib.jgit.interop.map-like
  (:require [hara.object :as object]))

(object/map-like
 org.eclipse.jgit.api.ApplyResult             {:tag "apply"}
 org.eclipse.jgit.api.CheckoutResult          {:tag "checkout"}
 org.eclipse.jgit.api.CherryPickResult        {:tag "cherrypick"}
 org.eclipse.jgit.api.MergeResult             {:tag "merge"}
 org.eclipse.jgit.api.RebaseResult            {:tag "rebase"}
 org.eclipse.jgit.api.PullResult              {:tag "pull"}
 org.eclipse.jgit.lib.ReflogEntry             {:tag "entry"}
 org.eclipse.jgit.lib.Ref                     {:tag "ref" :exclude [:leaf :target]}
 org.eclipse.jgit.diff.DiffEntry              {:tag "entry"}
 org.eclipse.jgit.transport.OperationResult   {:tag "result"}
 org.eclipse.jgit.revwalk.RevCommit
 {:tag "commit"
  :read {:methods
         (-> (object/read-getters org.eclipse.jgit.revwalk.RevCommit)
             (select-keys [:commit-time :author-ident :full-message])
             (assoc :name (fn [^org.eclipse.jgit.revwalk.RevCommit commit]
                            (.getName commit))))}}
 org.eclipse.jgit.lib.PersonIdent             {:tag "person" :exclude [:time-zone]}
 org.eclipse.jgit.transport.RemoteRefUpdate   {:tag "remote" :exclude [:tracking-ref-update]}
 org.eclipse.jgit.transport.TrackingRefUpdate {:tag "track"}
 org.eclipse.jgit.diff.Edit                   {:tag "edit"}
 org.eclipse.jgit.internal.storage.file.FileSnapshot {:tag "snapshot" :read :fields}
 org.eclipse.jgit.api.Status
 {:tag "status"
  :display (fn [m]
             (reduce-kv (fn [m [k v]]
                          (if (and (or (instance? java.util.Collection v)
                                       (instance? java.util.Map v))
                                   (empty? v))
                            m
                            (assoc m k v)))
                        m
                        {}))})
