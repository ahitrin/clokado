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

(defspec all-nodes-have-id
  1000
  (prop/for-all [a actions]
                (let [result (apply-all a)]
                  (not-any? nil? (map :id (remove empty? result))))))

(defspec always-have-nonzero-size
  1000
  (prop/for-all [a actions]
                (< 0 (count (apply-all a)))))

(defspec grow-from-mikado-node
  1000
  (prop/for-all [a actions]
                (let [result (apply-all a)
                      ids (replace {nil #{0}} (assoc-in (mapv :depends result) [0] #{0}))]
                  (= (repeat (count ids) #{0})
                     (loop [ids ids x (count ids)]
                       (if (zero? x)
                         ids
                         (recur (map #(reduce clojure.set/union (map (partial nth ids) %)) ids)
                                (dec x))))))))

(defspec closed-goals-must-not-have-open-leaves
  1000
  (prop/for-all [a actions]
                (let [result (apply-all a)
                      closed-ids (->> result
                                      (filter #(false? (:open %)))
                                      (map :id)
                                      (into #{}))
                      child-openess (->> result
                                         (filter #(not (empty? (clojure.set/intersection
                                                                 closed-ids
                                                                 (:depends %)))))
                                         (map :open)
                                         (into #{}))]
                  (contains? #{#{} #{false}} child-openess))))


(comment
  (map #(ns-unmap *ns* %) (keys (ns-interns *ns*)))
  (run-tests))
