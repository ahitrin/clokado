(ns clokado.graphviz (:use clokado.core))

"This file contains functions that could be used to transform mikado trees to graphviz trees"

(def color {:open "green" :closed "red"})
(def shape "box")

(defn goal-to-node [{id :id name :name op :open}]
  (str id " [label=\"" id ": " name "\", color=\"" (color op) "\", shape=\"" shape "\"];"))

(defn dependencies-to-links [{id :id deps :depends}]
  (map #(str id " -> " % ";") deps))

(defn to-graph [goals]
  (flatten (list "digraph g {"
             (map goal-to-node goals)
             (map dependencies-to-links goals)
             "}")))

;; untested (yes, I'm too lazy)

(defn write-dot [goals filename]
  (with-open [w (java.io.FileWriter. filename)]
    (.write w (clojure.string/join "\n" (to-graph goals)))))
