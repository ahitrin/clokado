(ns clokado.graphviz (:use clokado.core))

"This file contains functions that could be used to transform mikado trees to graphviz trees"

(def color {true "red" false "green"})
(def shape "box")

(defn prepared [goals]
  (->> goals
       (interleave (range))
       (partition 2)
       (remove #(empty? (nth % 1)))))

(defn goal-to-node [[id goal]]
  (let [name (:name goal) op (:open goal)]
    (str id " [label=\"" id ": " name
         "\", color=\"" (color op) "\", shape=\"" shape "\"];")))

(defn dependencies-to-links [[id goal]]
  (map #(str id " -> " % ";") (:depends goal)))

(defn to-graph [goals]
  (flatten (list "digraph g {"
             (->> goals prepared (map goal-to-node))
             (->> goals prepared (map dependencies-to-links))
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
