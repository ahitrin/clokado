(ns clokado.core
  (:use [clojure.set :only [difference]]))

;; mikado rules:
;; 1. there's one goal at start, it's called 'mikado goal'
;; 2. any new goal is considered 'open' by default
;; 4. a goal is considered 'top' when there are no open goals it depends on
;; 5. we can choose any top goal to solve
;; 6. when current goal cannot be solved, the new goal is created. current goal now depends on it
;; 7. when we achieve current goal, it becomes closed. now we can choose next goal to solve
;; 8. when we close mikado goal, the total problem is solved

(defn mikado [name]
  "Creates initial mikado goal"
  [{:id 0 :name name :open true :depends #{}}])

(defn add
  ([goals name]
    "Adds new goal to existing ones, which blocks mikado goal"
    (add goals name 0))
  ([goals name id]
    "Add new goal to existing ones, which blocks goal identified by id"
    (conj goals {:id (count goals) :name name :open true :depends #{id}})))

(defn top [goals]
  "Returns a list of open goals which no one goal depends on"
  (let [blocked-goals (->> goals (filter :open) (mapcat :depends) set)]
    (->> goals
         (remove #(contains? blocked-goals (:id %)))
         (filter :open)
         vec)))

(defn rename [goals id new-name]
  "Change name of the given goal"
  (assoc-in goals [id :name] new-name))

(defn close [goals id]
  "Mark goal with given id as closed"
  (assoc-in goals [id :open] false))

(defn reopen [goals id]
  "Mark goal with given id as open again"
  (assoc-in goals [id :open] true))

(defn link [goals a b]
  "Creates a new link. Goal b now blocks goal a"
  (if (or (= a b) (zero? b))
    goals
    (update-in goals [b :depends] conj a)))

(defn unlink [goals a b]
  "Removes existing link between goals a and b"
  (let [old-deps (:depends (nth goals b))]
    (if (= 1 (count old-deps))
      goals
      (assoc-in goals [b :depends] (set (remove #(= % a) old-deps))))))

(defn delete [goals id]
  "Recursively removes goal from the tree by id, together with goals that block it.
   Goals that depends also on some other goals, stay alive"
  (loop [gs goals ids (list id)]
    (if (empty? ids)
      gs
      (let [next-goals (->> (eval (macroexpand `(assoc ~gs ~@(mapcat #(list % {}) ids))))
                            (map #(if (nil? (:depends %)) {} (update-in % [:depends] difference ids)))
                            vec)
            next-id (->> next-goals
                         (map-indexed #(list %1 (if (empty? %2) -1 (count (:depends %2)))))
                         (filter #(zero? (second %)))
                         (map first)
                         (filter #(and (pos? %) (not (.contains ids %))))
                         set)]
        (recur next-goals next-id)))))
