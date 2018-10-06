(ns hara.data.table-test
  (:use hara.test)
  (:require [hara.data.table :refer :all]
            [hara.string :as string]
            [hara.io.file :as fs]))

(defn ascii
  [vs]
  (string/join vs "\n"))

^{:refer hara.data.table/generate-basic-table :added "3.0"}
(fact "generates a table for output"

  (generate-basic-table [:id :value]
                        [{:id 1 :value "a"}
                         {:id 2 :value "b"}])
  
  => (ascii ["| :id | :value |"
             "|-----+--------|"
             "|   1 |    \"a\" |"
             "|   2 |    \"b\" |"]))

^{:refer hara.data.table/parse-basic-table :added "3.0"}
(fact "reads a table from a string"

  (parse-basic-table (ascii
                      ["| :id | :value |"
                       "|-----+--------|"
                       "|   1 |    \"a\" |"
                       "|   2 |    \"b\" |"]))
  => {:headers [:id :value]
      :data [{:id 1 :value "a"}
             {:id 2 :value "b"}]})

^{:refer hara.data.table/generate-single-table :added "3.0"}
(fact "generates a single table"

  (generate-single-table {"a@a.com" {:id 1 :value "a"}
                          "b@b.com" {:id 2 :value "b"}}
                         {:headers [:id :email :value]
                          :sort-key :email
                          :id-key :email})
  => (ascii ["| :id |    :email | :value |"
             "|-----+-----------+--------|"
             "|   1 | \"a@a.com\" |    \"a\" |"
             "|   2 | \"b@b.com\" |    \"b\" |"]))


^{:refer hara.data.table/parse-single-table :added "3.0"}
(fact "generates a single table"

  (parse-single-table
   (ascii ["| :id |    :email | :value |"
           "|-----+-----------+--------|"
           "|   1 | \"a@a.com\" |    \"a\" |"
           "|   2 | \"b@b.com\" |    \"b\" |"])
   
   {:headers [:id :email :value]
    :sort-key :email
    :id-key :email})
  => {"a@a.com" {:id 1 :value "a"}
      "b@b.com" {:id 2 :value "b"}})

^{:refer hara.data.table/write-table :added "3.0"}
(fact "generates a single table"

  (write-table
   {:account {"a@a.com" {:id 1 :value "a"}
              "b@b.com" {:id 2 :value "b"}}
    :info {1 {:name "Chris"}
           2 {:name "David"}
           3 {:name "Cain"}}}
   {:path   "dev/scratch/test.db"
    :suffix "txt"
    :levels 1
    :headers {:account [:id :email :value]
              :info    [:id :name]}
    :sort-key {:info :name}
    :id-key {:account :email}})
  => {:account (ascii
                ["| :id |    :email | :value |"
                 "|-----+-----------+--------|"
                 "|   1 | \"a@a.com\" |    \"a\" |"
                 "|   2 | \"b@b.com\" |    \"b\" |"])

      :info (ascii
             ["| :id |   :name |"
              "|-----+---------|"
              "|   3 |  \"Cain\" |"
              "|   1 | \"Chris\" |"
              "|   2 | \"David\" |"])})
  
^{:refer hara.data.table/read-table :added "3.0"}
(fact "generates a single table"

  (read-table
   {:path  "dev/scratch/test.db"
    :suffix "txt"
    :levels 1
    :headers {:account [:id :email :value]
              :info    [:id :name]}
    :sort-key {:info :name}
    :id-key {:account :email}})
  => {:account {"a@a.com" {:id 1 :value "a"}
                "b@b.com" {:id 2 :value "b"}}
      :info {1 {:name "Chris"}
             2 {:name "David"}
             3 {:name "Cain"}}})

^{:refer hara.data.table/write-value :added "3.0"}
(fact "write a value to file"
  
  (write-value {:account {"a@a.com" {:id 1 :value "a"}
                          "b@b.com" {:id 2 :value "b"}}
                :info {3 {:name "Cain"}
                       1 {:name "Chris"}
                       2 {:name "David"}}}
               {:path "dev/scratch/test.db"
                :format :table
                :suffix "txt"
                :levels 1
                :headers  {:account [:id :email :value]
                           :info    [:id :name]}
                :sort-key {:info    :name}
                :id-key   {:account :email}}))

^{:refer hara.data.table/read-value :added "3.0"}
(fact "reads a value from a file"
  
  (read-value {:path "dev/scratch/test.db"
               :format :table})
  => {:account {"a@a.com" {:id 1, :value "a"},
                "b@b.com" {:id 2, :value "b"}},
      :info {3 {:name "Cain"},
             1 {:name "Chris"},
             2 {:name "David"}}})

^{:refer hara.data.table/file-out :added "3.0"}
(fact "adds watch to atom, saving its contents to file on every change"

  (def out-file (str (fs/create-tmpdir) "/test.txt"))
  
  (swap! (file-out (atom 1) {:path out-file})
         inc)
  
  (read-string (slurp out-file))
  => 2)

^{:refer hara.data.table/log-out :added "3.0"}
(comment "adds watch to atom, logging the contents on every change"
  
  (with-out-str
    (swap! (log-out (atom 1) {})
           inc)))

^{:refer hara.data.table/attach-state :added "3.0"}
(comment "used with component, adds watch on record that incorporates state")

^{:refer hara.data.table/detach-state :added "3.0"}
(comment "used with component, remove watch on record that incorporates state")
