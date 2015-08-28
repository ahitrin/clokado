(ns clokado.graphviz (:use clokado.core))

"This file contains functions that could be used to transform mikado trees to graphviz trees"

(def color {true "red" false "green"})
(def link-color {true "black" false "grey"})
(def shape "box")

(defn goal-to-node [{id :id name :name open :open ontop :ontop}]
  (format "%d [%s];"
          id
          (->> [(format "label=\"%d: %s\"" id name)
                (format "color=\"%s\"" (color open))
                (format "shape=\"%s\"" shape)
                (when ontop "style=\"bold\"")]
               (remove nil?)
               (clojure.string/join ", "))))

(defn dependencies-to-links [{id :id depends :depends open :open}]
  (map #(format "%d -> %d [color=\"%s\"];" id % (link-color open)) depends))

(defn to-graph [goals]
  (let [tops (->> goals top (map :id) set)
        graph-info (->> goals
                        (remove empty?)
                        (map #(assoc % :ontop
                                     (contains? tops (:id %)))))]
    (flatten (list "digraph g {"
               (map goal-to-node graph-info)
               (map dependencies-to-links graph-info)
               "}"))))

;; untested (yes, I'm too lazy)

(use '[clojure.java.shell :only [sh]])

(defn write-dot [goals filename]
  (with-open [w (java.io.FileWriter. (str filename ".dot"))]
    (.write w (clojure.string/join "\n" (to-graph goals)))))

(defn compile-dot [filename]
  (sh "dot" "-Tpng" "-o" (str filename ".png") (str filename ".dot")))

(defn to-png [goals filename]
  (write-dot goals filename)
  (compile-dot filename))
