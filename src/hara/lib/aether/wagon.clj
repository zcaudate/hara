(ns hara.lib.aether.wagon
  (:require [hara.object.query :as reflect])
  (:import (org.apache.maven.wagon AbstractWagon Wagon)
           (org.apache.maven.wagon.authentication AuthenticationInfo)
           (org.apache.maven.wagon.proxy ProxyInfo)
           (org.apache.maven.wagon.repository Repository)
           (org.eclipse.aether.transport.wagon WagonProvider WagonTransporterFactory)))

(def ^:dynamic *wagon-factories*
  (atom {:https org.apache.maven.wagon.providers.webdav.WebDavWagon}))

(defn add-factory
  "registers a wagon factory for creating transports"
  {:added "3.0"}
  [scheme wagon]
  (swap! *wagon-factories* assoc scheme wagon))

(defn remove-factory
  "removes the registered wagon factory"
  {:added "3.0"}
  [scheme]
  (swap! *wagon-factories* dissoc scheme))

(defn all-factories
  "list all registered factories
 
   (all-factories)
   => {:https org.apache.maven.wagon.providers.webdav.WebDavWagon}"
  {:added "3.0"}
  []
  @*wagon-factories*)

(deftype Provider []
  WagonProvider
  (release [_ wagon])
  (lookup [_ scheme]
    (when-let [cls (get @*wagon-factories* (keyword scheme))]
      (let [constructor (reflect/query-class cls ["new" :#])]
        (constructor)))))

(defn create
  "create a wagon given a scheme
 
   (create :https)
   => org.apache.maven.wagon.providers.webdav.WebDavWagon"
  {:added "3.0"}
  [scheme]
  (-> (Provider.)
      (.lookup (name scheme))))
