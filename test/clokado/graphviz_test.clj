(ns clokado.graphviz-test
  (:use clojure.test
        clokado.core
        clokado.graphviz))

;; example trees

(def simplest
  (mikado "Just do it"))

(def kitty
  (add (add (mikado "Feed the kitty") "Find the food") "Go to the store" 2))

(deftest simplest-case
  (testing "transformation of the smallest mikado tree"
    (is (= (to-graph simplest)
           '("digraph g {"
             "1 [label=\"1: Just do it\", color=\"green\", shape=\"box\"];"
             "}")))))

(deftest kitty-case
  (testing "transformation of more complex tree"
    (is (= (to-graph kitty)
           '("digraph g {"
             "1 [label=\"1: Feed the kitty\", color=\"green\", shape=\"box\"];"
             "2 [label=\"2: Find the food\", color=\"green\", shape=\"box\"];"
             "3 [label=\"3: Go to the store\", color=\"green\", shape=\"box\"];"
             "2 -> 1;"
             "3 -> 2;"
             "}")))))

(deftest kitty-case2
  (testing "closed goals should be colored red"
    (is (= (to-graph (close kitty 3))
           '("digraph g {"
             "1 [label=\"1: Feed the kitty\", color=\"green\", shape=\"box\"];"
             "2 [label=\"2: Find the food\", color=\"green\", shape=\"box\"];"
             "3 [label=\"3: Go to the store\", color=\"red\", shape=\"box\"];"
             "2 -> 1;"
             "3 -> 2;"
             "}")))))
