(ns clokado.core
  (:use [clojure.set :only [intersection]]))

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
  [{:name name :id 0 :open :open :depends []}])

(defn add
  ([goals name]
    "Adds new goal to existing ones, which blocks mikado goal"
    (add goals name 0))
  ([goals name id]
    "Add new goal to existing ones, which blocks goal identified by id"
    (let [max-id (apply max (map :id goals))]
      (conj goals {:name name :id (inc max-id) :open :open :depends [id]}))))

(defn only-open [goals]
  (filter #(= :open (:open %)) goals))

(defn idx [goals id]
  (.indexOf (map #(= id (:id %)) goals) true))

(defn top [goals]
  "Returns a list of open goals which no one goal depends on"
  (let [blocked-goals (->> goals only-open (mapcat :depends) set)]
    (vec (only-open (filter #(not (contains? blocked-goals (:id %))) goals)))))

(defn rename [goals id new-name]
  "Change name of the given goal"
  (let [index (idx goals id)]
    (assoc-in goals [index :name] new-name)))

(defn close [goals id]
  "Mark goal with given id as closed"
  (let [index (idx goals id)]
    (assoc-in goals [index :open] :closed)))

(defn reopen [goals id]
  "Mark goal with given id as open again"
  (let [index (idx goals id)]
    (assoc-in goals [index :open] :open)))

(defn link [goals a b]
  "Creates a new link. Goal b now blocks goal a"
  (let [b-index (idx goals b)
        old-deps (:depends (nth goals b-index))]
    (if (or (= a b) (zero? b) (.contains old-deps a))
      goals
      (assoc-in goals [b-index :depends] (conj old-deps a)))))

(defn unlink [goals a b]
  "Removes existing link between goals a and b"
  (let [b-index (idx goals b)
        old-deps (:depends (nth goals b-index))]
    (if (= 1 (count old-deps))
      goals
      (assoc-in goals [b-index :depends] (vec (remove #(= % a) old-deps))))))

(defn delete [goals id]
  "Recursevly removes goal from the tree by id, together with goals that block it.
   Goals that depends also on some other goals, stay alive"
  (loop [gs goals ids (list id)]
    (if (empty? ids)
      gs
      (let [i (first ids)
            deps (group-by #(= 1 (count (:depends %)))
                           (filter #(.contains (:depends %) i) gs))
            to-remove (deps true)
            to-clean (deps false)
            cleaned-gs (loop [gs gs ids (map :id to-clean)]
                     (if (empty? ids)
                         gs
                         (recur (unlink gs i (first ids)) (rest ids))))]
        (recur (vec (remove #(= i (:id %)) cleaned-gs))
               (concat (rest ids) (map :id to-remove)))))))
