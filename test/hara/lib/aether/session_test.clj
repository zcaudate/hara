(ns hara.lib.aether.session-test
  (:use hara.test)
  (:require [hara.lib.aether.session :refer :all]
            [hara.lib.aether.system :as system]))

^{:refer hara.lib.aether.session/session :added "3.0"}
(fact "creates a session from a system:"

  (session (system/repository-system)
           {})
  => org.eclipse.aether.RepositorySystemSession)