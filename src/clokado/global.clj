(ns clokado.global
  (:use clokado.core
        clokado.graphviz))

"Simpler functions, seems more useful for REPL evaluation & instant redraw"

(def tree (ref []))
(def filename (ref ""))

(defn redraw! []
  (to-png @tree @filename))

(defn mikado! [name file]
  (dosync (ref-set tree (mikado name))
          (ref-set filename file))
  (redraw!))

(defn add!
  ([name] (dosync (alter tree add name)) (redraw!))
  ([name id] (dosync (alter tree add name id) (redraw!))))

(defn top! []
  (top @tree))

(defn close! [id]
  (dosync (alter tree close id) (redraw!)))
