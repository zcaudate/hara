(ns hara.state.base.atom)

(defn cursor
  "adds a cursor to the atom to update on any change
 
   (def a (atom {:a {:b 1}}))
   
   (def ca (cursor a [:a :b]))
 
   (do (swap! ca + 10)
       (swap! a update-in [:a :b] + 100)
       [(deref a) (deref ca)])
   => [{:a {:b 111}} 111]"
  {:added "3.0"}
  ([ref selector]
   (cursor ref selector (str (java.util.UUID/randomUUID))))
  ([ref selector key]
   (let [getter  (fn [m] (get-in m selector))
         setter  (fn [m v] (assoc-in m selector v))
         initial (getter @ref)
         cursor  (atom initial)]
     (add-watch ref key (fn [_ _ _ v]
                          (let [cv (getter v)]
                            (if (not= cv @cursor)
                              (reset! cursor cv)))))
     (add-watch cursor key (fn [_ _ _ v]
                             (swap! ref setter v)))
     cursor)))

(defn derived
  "constructs an atom derived from other atoms
 
   (def a (atom 1))
   (def b (atom 10))
   (def c (derived [a b] +))
 
   (do (swap! a + 1)
       (swap! b + 10)
       [@a @b @c])
   => [2 20 22]"
  {:added "3.0"}
  ([atoms f]
   (derived atoms f (str (java.util.UUID/randomUUID))))
  ([atoms f key]
   (let [derived-fn #(apply f (map deref atoms))
         derived  (atom (derived-fn))]
     (doseq [atom atoms]
       (add-watch atom key
                  (fn [_ _ _ _]
                    (reset! derived (derived-fn)))))
     derived)))
