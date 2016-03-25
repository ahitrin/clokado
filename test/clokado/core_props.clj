(ns clokado.core-props
  (:use clokado.core)
  (:require [clojure.test :refer (run-tests)]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :as ct :refer (defspec)]))

(def actions
  (gen/not-empty (gen/vector
    (gen/tuple
      (gen/elements [:add :add-to :rename :close :reopen :link :unlink :insert :delete])
      (gen/not-empty gen/string-ascii)
      gen/nat
      gen/nat))))

(defn to-real-action [[fn-name s i1 i2]]
  (fn-name {:add (fn [g] (add g s))
            :add-to (fn [g] (add g s i1))
            :rename (fn [g] (rename g i1 s))
            :close (fn [g] (close g i1))
            :reopen (fn [g] (reopen g i1))
            :link (fn [g] (link g i1 i2))
            :unlink (fn [g] (unlink g i1 i2))
            :insert (fn [g] (insert g s i1 i2))
            :delete (fn [g] (delete g i1))}))

(defn apply-all [as]
  (reduce #(%2 %1) (mikado "start") (map to-real-action as)))

(defspec always-have-nonzero-size
  1000
  (prop/for-all [a actions]
                (< 0 (count (apply-all a)))))


(comment
  (map #(ns-unmap *ns* %) (keys (ns-interns *ns*)))
  (run-tests))
