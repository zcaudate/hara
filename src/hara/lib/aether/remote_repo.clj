(ns hara.lib.aether.remote-repo
  (:require [hara.object :as object]
            [hara.lib.aether.authentication])
  (:import (org.eclipse.aether.repository RemoteRepository RemoteRepository$Builder RepositoryPolicy)))

(object/map-like
 RepositoryPolicy
 {:tag "policy"
  :read :class
  :write {:construct {:fn (fn [enabled update checksum]
                            (RepositoryPolicy. enabled update checksum))
                      :params [:enabled? :update-policy :checksum-policy]}}}

 RemoteRepository
 {:tag "remote"
  :read :class
  :write {:from-map (fn [m]
                      (.build (object/from-data m RemoteRepository$Builder)))}}

 RemoteRepository$Builder
 {:tag "builder.remote"
  :read  {:methods (dissoc (object/read-fields RemoteRepository$Builder)
                           :repoman :auth :snapshots :releases :delta :mirrored)}
  :write {:construct {:fn (fn [id type url]
                            (RemoteRepository$Builder. id (or type "default") url))
                      :params [:id :type :url]}
          :methods (object/write-setters RemoteRepository$Builder)}})
