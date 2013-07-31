(ns clokado.global (:use clokado.core))

"Simpler functions, seems more useful for REPL evaluation"

(def tree (ref []))

(defn mikado! [name]
  (dosync (ref-set tree (mikado name))))

(defn add!
  ([name] (dosync (alter tree add name)))
  ([name id] (dosync (alter tree add name id))))

(defn top! []
  (top @tree))

(defn close! [id]
  (dosync (alter tree close id)))
