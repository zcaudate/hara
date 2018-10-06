(ns hara.lib.jsoup-test
  (:use hara.test)
  (:require [hara.lib.jsoup :refer :all]))

(def +content+
  (str "<html id=3><head><title>First parse</title></head>"
       "<body><p>Parsed HTML into a doc.</p></body></html>"))

^{:refer hara.lib.jsoup/node->tree :added "3.0"}
(fact "converts a Jsoup node to tree"

  (-> (parse "<body><div>hello</div>world</body>")
      (node->tree))
  => [:body [:div "hello"] "world"])

^{:refer hara.lib.jsoup/tree->node :added "3.0"}
(fact "converts a tree to a Jsoup node"

  (tree->node [:body [:div "hello"] "world"])
  => org.jsoup.nodes.Element)

^{:refer hara.lib.jsoup/parse :added "3.0"}
(fact "reads a Jsoup node from string"

  (parse "<body><div>hello</div>world</body>")
  => org.jsoup.nodes.Element)

^{:refer hara.lib.jsoup/tighten :added "3.0"}
(fact "removes lines for elements that contain no internal elements"

  (tighten "<b>\nhello\n</b>")
  => "<b>hello</b>")

^{:refer hara.lib.jsoup/generate :added "3.0"}
(fact "generates string html for element"

  (generate (tree->node [:body [:div "hello"] "world"]))
  => "<body>\n    <div>hello</div>world\n</body>")

^{:refer hara.lib.jsoup/html :added "3.0"}
(fact "converts either node or tree representation to a html string"

  (html [:body [:div "hello"] "world"])
  => "<body>\n    <div>hello</div>world\n</body>")

^{:refer hara.lib.jsoup/node :added "3.0"}
(fact "converts either a string or tree representation to a Jsoup node"
  
  (node [:body [:div "hello"] "world"])
  => org.jsoup.nodes.Element)

^{:refer hara.lib.jsoup/tree :added "3.0"}
(fact "converts either a string or node representation into a tree"

  (tree +content+)
  => [:html {:id "3"}
      [:head [:title "First parse"]]
      [:body [:p "Parsed HTML into a doc."]]])
