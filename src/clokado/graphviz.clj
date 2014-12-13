(ns clokado.graphviz (:use clokado.core))

"This file contains functions that could be used to transform mikado trees to graphviz trees"

(def color {true "red" false "green"})
(def shape "box")

(defn goal-to-node [{id :id name :name op :open}]
  (str id " [label=\"" id ": " name "\", color=\"" (color op) "\", shape=\"" shape "\"];"))

(defn dependencies-to-links [{id :id deps :depends}]
  (map #(str id " -> " % ";") deps))

(defn to-graph [goals]
  (flatten (list "digraph g {"
             (->> goals (remove empty?) (map goal-to-node))
             (map dependencies-to-links goals)
             "}")))

;; untested (yes, I'm too lazy)

(use '[clojure.java.shell :only [sh]])

(defn write-dot [goals filename]
  (with-open [w (java.io.FileWriter. filename)]
    (.write w (clojure.string/join "\n" (to-graph goals)))))

(defn compile-dot [filename]
  (sh "dot" "-Tpng" "-o" (str filename ".png") filename))

(defn to-png [goals filename]
  (write-dot goals filename)
  (compile-dot filename))
