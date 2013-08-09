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

(defn rename! [id new-name]
  (dosync (alter tree rename id new-name) (redraw!)))

(defn close! [id]
  (dosync (alter tree close id) (redraw!)))

(defn reopen! [id]
  (dosync (alter tree reopen id) (redraw!)))

(defn delete! [id]
  (dosync (alter tree delete id) (redraw!)))

(defn link! [a b]
  (dosync (alter tree link a b) (redraw!)))

(defn unlink! [a b]
  (dosync (alter tree unlink a b) (redraw!)))
