(ns clokado.global (:use clokado.core))

"Simpler functions, seems more useful for REPL evaluation"

(def tree (ref []))

(defn mikado! [name]
  (dosync (ref-set tree (mikado name))))

(defn add!
  ([name] (dosync (ref-set tree (add @tree name))))
  ([name id] (dosync (ref-set tree (add @tree name id)))))

(defn top! []
  (top @tree))

(defn close! [id]
  (dosync (ref-set tree (close @tree id))))
