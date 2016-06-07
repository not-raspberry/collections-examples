(ns collections-examples.core
  (:gen-class))

(defn -main
  [& args]
  (println "Example project!"))


; INTRODUCTION

; Syntax

; Function call
(println 1 2 3 "a")
(map inc (range 20))


; Binding special form
(let [a (+ 1 2 3)
      b (inc a)]
  [a b])

; Conditional special form
(if (= 1 1)
  "equal"
  "not equal")

; Expression-based syntax
(let [a 11, b 12]
  (println "The numbers" a "and" b "are"
           (if (= a b)
             "equal"
             "not equal")))

; Functions
(defn cube [x] (* x x x))

(require '[clojure.string :as s])
(defn needlessly-verbose-comparision [a b]
  (s/join " " ["The values" a "and" b "are"
               (if (= a b) "equal" "not equal")]))


; DATA STRUCTURES

; Vectors
[1 2 3 4 5]
(vector 1 2 3 4 5)

; Maps
{:a 1 :b 2 :c 3 :d 4}
(hash-map :a 1 :b 2 :c 3 :d 4)
(sorted-map :a 1 :b 2 :c 3 :d 4)

; Sets
#{1 2 3 4}
(hash-set 1 2 3 4)
(sorted-set 1 2 3 4)

; Lists
'(1 2 3 4)
(list 1 2 3 4)


; Polymorphic collection functions:

; assoc - works on associative collections
(assoc {:a 1} :b 12)  ; {:a 1 :b 12}
(assoc [1 2 3] 0 :aa)  ; [:aa 2 3]

;(assoc '(1 2 3) 0 :aa)  ; error - linked lists are not associative (no constant time index addressing)
(associative? '()) ; false

; conj - conjoining to a collection (basically anything)
(conj [1 2 3] :a) ; [1 2 3 :a]  - vector - conjoining to the end
(conj '(1 2 3) :a) ; '(:a 1 2 3)  - list - conjoining to the front - a source of quality bugs
(conj {:a 1} [:b 2]) ; {:a 1, :b 2}  - map - glueing
(conj #{:a} :b) ; {:a 1, :b 2}  - set

; Collections are functions
({:a 1 :b 3} :a) ; 1
([:a :b :c :d] 2) ; :c
(#{:a :b :c :d} :c) ; :c

; get
(get {:a 1 :b 3} :a) ; 1
(get [:a :b :c :d] 2) ; :c
(get #{:a :b :c :d} :c) ; :c

; keywords are dispatch functions
(:a {:a 1 :b 3}) ; 1
(:c #{:a :b :c :d}) ; :c

; nil punning - when passed to a function that expects a collection, nil works
; more or less as an empty variant of it
(:port {})  ; nil
(conj nil 1)  ; (1)
(map inc nil)  ; ()



; PERSISTENCE


; Integers are immutable - they are boxed Java Longs
(let [a 12
      b (inc a)]
  [a b])
; -> [12 13]

; Strings are immutable - they are Java strings
(let [a "aaaaa"
      b (str a "bbbbbb")]
  [a b])
; -> ["aaaaa" "aaaaabbbbbb"]

; The exciting thing - Clojure data structures are immutable too.
(let [a [1 2 3]
      b (conj a :q)]
  [a b])
; -> [[1 2 3] [1 2 3 :q]]

(let [a {:a 1, :b 2}
      b (conj a [:c 21])]
  [a b])
; -> [{:a 1, :b 2} {:a 1, :b 2, :c 21}]


; Collections are hashable - since they are immutable, they can safely serve
; as keys
({#{1 2 3} :q} #{1 2 3})  ; :q


; Nested trouble
(def sth {:data {:blah {:k 1}}})

; Obtaining :k - more than 1 tedious way to do this.
(:k (:blah (:data sth)))
(((sth :data) :blah) :k)
(->> sth :data :blah :k)  ; Almost good but using a threading macro for deep dispatch

(get-in sth [:data :blah :k])

; 'Updating':
(assoc (get-in sth [:data :blah]) :k 2) ; {:k 2} - retuns only the 'modified' nested map
(assoc-in sth [:data :blah :k] 2)  ; {:data {:blah {:k 2}}}  - handles nesting
(update-in sth [:data :blah :k] inc) ; same as above but 'updates' the value by applying
; a function to it


; CHEATING - transients

; Given a big sequence, feed it into a collection
(loop [coll #{}
       nums (range 100000)]
  (if (empty? nums)
    coll
    (recur (conj coll (first nums)) (next nums))))

(loop [coll (transient #{})
       nums (range 100000)]
  (if (empty? nums)
    (persistent! coll)
    (recur (conj! coll (first nums)) (next nums))))

; Same as above
(reduce conj #{} (range 100000))
(persistent! (reduce conj! (transient #{}) (range 100000)))
