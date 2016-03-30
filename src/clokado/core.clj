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

(defn- missing? [goals id]
  "Return true iff goal with given id wasn't created yet or already deleted"
  (or (<= (count goals) id) (empty? (nth goals id))))

(defn add
  ([goals name]
    "Adds new goal to existing ones, which blocks mikado goal"
    (add goals name 0))
  ([goals name id]
    "Add new goal to existing ones, which blocks goal identified by id"
    (if (missing? goals id)
      goals
      (conj goals {:id (count goals) :name name :open true :depends #{id}}))))

(defn top [goals]
  "Returns a list of open goals which no one goal depends on"
  (let [only-open (filterv :open goals)
        blocked (set (mapcat :depends only-open))]
    (remove #(contains? blocked (:id %)) only-open)))

(defn rename [goals id new-name]
  "Change name of the given existing goal"
  (if (missing? goals id)
    goals
    (assoc-in goals [id :name] new-name)))

(defn close [goals id]
  "Mark existing goal with given id as closed"
  (if (missing? goals id)
    goals
    (assoc-in goals [id :open] false)))

(defn reopen [goals id]
  "Mark existing goal with given id as open again"
  (if (missing? goals id)
    goals
    (assoc-in goals [id :open] true)))

(defn- is-loop? [goals from to]
  "Return true if link from→to creates loop in goals, false otherwise"
  (let [deps (map :depends goals)]
    (loop [parnts (nth deps from)]
      (let [new-parnts (reduce clojure.set/union (map (partial nth deps) parnts))]
        (cond
          (contains? parnts to) true
          (= parnts new-parnts) false
          :else (recur new-parnts))))))

(defn link [goals a b]
  "Creates a new link. Goal b now blocks goal a. Both goals must exist.
  Attempts to create circular links are ignored."
  (if (or (= a b)
          (zero? b)
          (missing? goals a)
          (missing? goals b)
          (is-loop? goals a b))
     goals
     (update-in goals [b :depends] conj a)))

(defn unlink [goals a b]
  "Removes existing link between goals a and b. Both goals must exist.
  The last link cannot be removed"
  (if (or (missing? goals a) (missing? goals b))
    goals
    (let [old-deps (:depends (nth goals b))]
      (if (= 1 (count old-deps))
        goals
        (update-in goals [b :depends] disj a)))))

(defn insert [goals name a b]
  "Insert new nodes between two different existing ones"
  (if (= a b)
    goals
    (let [next-id (count goals)]
        (-> goals
            (add name a)
            (link next-id b)
            (unlink a b)))))

(defn delete [goals id]
  "Recursively removes existing goal from the tree by id, together with goals that block it.
   Goals that depends also on some other goals, stay alive.
   Mikado goal cannot be deleted."
  (if (missing? goals id)
    goals
    (loop [gs goals ids (list id)]
          (if (or (empty? ids) (zero? id))
            gs
            (let [next-goals (->> ids
                                  (reduce #(assoc %1 %2 {}) gs)
                                  (mapv #(if (nil? (:depends %)) {} (update-in % [:depends] difference ids))))
                  next-id (->> next-goals
                               (filter #(and (seq %)
                                             (pos? (:id %))
                                             (empty? (:depends %))))
                               (map :id)
                               (into #{}))]
              (recur next-goals next-id))))))
