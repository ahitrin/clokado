(ns clokado.graphviz-test
  (:use clojure.test
        clokado.core
        clokado.graphviz))

;; example trees

(def simplest
  (mikado "Just do it"))

(def kitty
  (add (add (mikado "Feed the kitty") "Find the food") "Go to the store" 1))

(deftest simplest-case
  (testing "transformation of the smallest mikado tree"
    (is (= (to-graph simplest)
           '("digraph g {"
             "0 [label=\"0: Just do it\", color=\"red\", shape=\"box\"];"
             "}")))))

(deftest kitty-case
  (testing "transformation of more complex tree"
    (is (= (to-graph kitty)
           '("digraph g {"
             "0 [label=\"0: Feed the kitty\", color=\"red\", shape=\"box\"];"
             "1 [label=\"1: Find the food\", color=\"red\", shape=\"box\"];"
             "2 [label=\"2: Go to the store\", color=\"red\", shape=\"box\"];"
             "1 -> 0;"
             "2 -> 1;"
             "}")))))

(deftest kitty-case2
  (testing "closed goals should be colored red"
    (is (= (to-graph (close kitty 2))
           '("digraph g {"
             "0 [label=\"0: Feed the kitty\", color=\"red\", shape=\"box\"];"
             "1 [label=\"1: Find the food\", color=\"red\", shape=\"box\"];"
             "2 [label=\"2: Go to the store\", color=\"green\", shape=\"box\"];"
             "1 -> 0;"
             "2 -> 1;"
             "}")))))

(deftest kitty-case3
  (testing "deleted goals should not be displayed"
    (is (= (to-graph (delete kitty 2))
           '("digraph g {"
             "0 [label=\"0: Feed the kitty\", color=\"red\", shape=\"box\"];"
             "1 [label=\"1: Find the food\", color=\"red\", shape=\"box\"];"
             "1 -> 0;"
             "}")))))

(comment
  (map #(ns-unmap *ns* %) (keys (ns-interns *ns*)))
  (run-tests))
