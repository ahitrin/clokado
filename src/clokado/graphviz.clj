(ns clokado.graphviz (:use clokado.core))

"This file contains functions that could be used to transform mikado trees to graphviz trees"

(def color {true "red" false "green"})
(def link-color {true "grey" false "black"})
(def shape "box")

(defn enumerate-and-drop-empty [goals]
  (keep-indexed (fn [i v] (when (seq v) (list i v)))
                goals))

(defn goal-to-node [goals]
  (for [[id goal] goals]
    (let [name (:name goal) op (:open goal)]
      (str id " [label=\"" id ": " name
           "\", color=\"" (color op) "\", shape=\"" shape "\"];"))))

(defn dependencies-to-links [goals closed-ids]
  (for [[id goal] goals]
    (let [closed? (contains? closed-ids id)
          col (link-color closed?)]
      (map #(str id " -> " % " [color=\"" col "\"];")
           (:depends goal)))))

(defn to-graph [goals]
  (let [prepared-goals (enumerate-and-drop-empty goals)
        closed (set (map first (remove (fn [[i v]] (:open v)) prepared-goals)))]
    (flatten (list "digraph g {"
               (goal-to-node prepared-goals)
               (dependencies-to-links prepared-goals closed)
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
