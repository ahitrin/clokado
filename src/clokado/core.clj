(ns clokado.core)

;; mikado rules:
;; 1. there's one goal at start, it's called 'mikado goal'
;; 2. any new goal is considered 'open' by default
;; 4. a goal is considered 'top' when there are no open goals it depends on
;; 5. we can choose any top goal to solve
;; 6. when current goal cannot be solved, the new goal is created. current goal now depends on it
;; 7. when we achieve current goal, it becomes closed. now we can choose next goal to solve
;; 8. when we close mikado goal, the total problem is solved

(defrecord goal [name open depends id])

(defn mikado [name]
  "Creates initial mikado goal"
  [(->goal name true [] 1)])

(defn add
  ([goals name]
    "Adds new goal to existing ones, which blocks mikado goal"
    (add goals name 1))
  ([goals name id]
    "Add new goal to existing ones, which blocks goal identified by id"
    (let [max-id (apply max (map :id goals))]
      (conj goals (->goal name true [id] (inc max-id))))))

(defn top [goals]
  "Returns a list of goals which no one goal depends on"
  (let [blocked-goals (set (reduce concat (map :depends goals)))]
    (vec (filter #(not (contains? blocked-goals (:id %))) goals))))
