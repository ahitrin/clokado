(ns clokado.graphviz (:use clokado.core))

"This file contains functions that could be used to transform mikado trees to graphviz trees"

(def color {true "red" false "green"})
(def link-color {true "black" false "grey"})
(def shape "box")

(defn goal-to-node [goals]
  (for [{:keys [id name open]} goals]
    (str id " [label=\"" id ": " name
         "\", color=\"" (color open) "\", shape=\"" shape "\"];")))

(defn dependencies-to-links [goals]
  (for [{:keys [id depends open]} goals]
    (map #(str id " -> " % " [color=\"" (link-color open) "\"];") depends)))

(defn to-graph [goals]
  (let [prepared-goals (remove empty? goals)]
    (flatten (list "digraph g {"
               (goal-to-node prepared-goals)
               (dependencies-to-links prepared-goals)
               "}"))))

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
