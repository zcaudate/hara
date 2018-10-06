(ns hara.data.base.transform-test
  (:use hara.test)
  (:require [hara.data.base.transform :refer :all]))

^{:refer hara.data.base.transform/template? :added "3.0"}
(fact "checks if an object is a template"

  (template? "{{template}}")
  => true

  (template? :not-one)
  => false)

^{:refer hara.data.base.transform/find-templates :added "3.0"}
(fact "finds the template with associated path"

  (find-templates {:hash  "{{hash}}"
                   :salt  "{{salt}}"
                   :email "{{email}}"
                   :user {:firstname "{{firstname}}"
                          :lastname  "{{lastname}}"}})
  => {"{{hash}}" [:hash]
      "{{salt}}" [:salt]
      "{{email}}" [:email]
      "{{firstname}}" [:user :firstname]
      "{{lastname}}" [:user :lastname]})

^{:refer hara.data.base.transform/transform-fn :added "3.0"}
(fact "creates a transformation function"
  ((transform-fn {:keystore {:hash  "{{hash}}"
                             :salt  "{{salt}}"
                             :email "{{email}}"}

                  :db       {:login {:type :email
                                     :user {:hash "{{hash}}"
                                            :salt "{{salt}}"}
                                     :value "{{email}}"}}}
                 [:keystore :db])
   {:hash "1234"
    :salt "ABCD"
    :email "a@a.com"})
  => {:login {:type :email,
              :user {:hash "1234",
                     :salt "ABCD"},
              :value "a@a.com"}})

^{:refer hara.data.base.transform/transform :added "3.0"}
(fact "creates a transformation function"
  (transform {:keystore {:hash  "{{hash}}"
                         :salt  "{{salt}}"
                         :email "{{email}}"}

              :db       {:login {:type :email
                                 :user {:hash "{{hash}}"
                                        :salt "{{salt}}"}
                                 :value "{{email}}"}}}
             [:keystore :db]
             {:hash "1234"
              :salt "ABCD"
              :email "a@a.com"})
  => {:login {:type :email,
              :user {:hash "1234",
                     :salt "ABCD"},
              :value "a@a.com"}})