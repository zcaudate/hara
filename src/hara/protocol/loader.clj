(ns hara.protocol.loader)

(defprotocol ILoader
  (-has-url?    [obj path])
  (-get-url     [obj path])
  (-all-urls    [obj])
  (-add-url     [obj path])
  (-remove-url  [obj path]))

(defmulti -load-class
 "loads class from an external source

  (load-class \"target/classes/test/Cat.class\")
  => test.Cat

  (load-class \"<.m2>/org/yaml/snakeyaml/1.5/snakeyaml-1.5.jar\"
              \"org/yaml/snakeyaml/Dumper.class\")
  => org.yaml.snakeyaml.Dumper

  (load-class '[org.yaml/snakeyaml \"1.5\"]
              \"org/yaml/snakeyaml/Dumper.class\")
  => org.yaml.snakeyaml.Dumper"
  {:added "3.0"}
  (fn [x loader opts] [(type x) (type loader) ]))

(defmulti -rep
  "converts various formats to a rep
 
   (rep '[hara/hara \"2.4.0\"])
   => 'hara:hara:jar:2.4.0
 
   (rep \"hara:hara:2.4.0\")
   => 'hara:hara:jar:2.4.0"
  {:added "3.0"}
  type)

(defmulti -artifact
  "converts various artifact formats
 
   (artifact :string '[hara/hara \"2.4.0\"])
   => \"hara:hara:jar:2.4.0\"
 
   (artifact :path \"hara:hara:2.4.0\")
   => (str base/*local-repo*
           \"/hara/hara/2.4.0/hara-2.4.0.jar\")"
  {:added "3.0"}
  (fn [tag x] tag))
