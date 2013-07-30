(ns clokado.core)

;; mikado rules:
;; 1. there's one goal at start, it's called 'mikado goal'
;; 2. a goal is considered 'open' when there are no other open goals it depends on
;; 4. a goal is considered 'top' when there are no goals it depends on
;; 5. we can choose any top goal to solve
;; 6. when current goal cannot be solved, the new open goal is created. current goal now depends on it
;; 7. when we achieve current goal, it becomes closed. now we can choose next goal to solve
;; 8. when we close mikado goal, the total problem is solved

(defrecord node [name open depends id])

(defn mikado [name]
  "Creates initial mikado goal"
  [(->node name true [] 1)])

(defn add [goals name]
  "Adds new goal to existing ones, which blocks mikado goal"
  (let [max-id (apply max (map :id goals))]
    (conj goals (->node name true [1] (inc max-id)))))
