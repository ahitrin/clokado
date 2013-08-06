(ns clokado.core)

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
  [{:name name :id 1 :open :open :depends []}])

(defn add
  ([goals name]
    "Adds new goal to existing ones, which blocks mikado goal"
    (add goals name 1))
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
  (let [blocked-goals (set (reduce concat (map :depends (only-open goals))))]
    (vec (only-open (filter #(not (contains? blocked-goals (:id %))) goals)))))

(defn close [goals id]
  "Mark goal with given id as closed"
  (let [index (idx goals id)]
    (assoc-in goals [index :open] :closed)))

(defn reopen [goals id]
  "Mark goal with given id as open again"
  (let [index (idx goals id)]
    (assoc-in goals [index :open] :open)))

(defn delete [goals id]
  "Removes goal from the tree by id"
  (let [goals-without-id (vec (remove #(= id (:id %)) goals))
        blocking-goals (filter #(.contains (:depends %) id) goals)
        goals-to-delete (map :id (filter #(= 1 (count (:depends %))) blocking-goals))
        goals-to-cleanup (map :id (filter #(> 1 (count (:depends %))) blocking-goals))]
        ;; TODO: cast unlink upon each goal in goals-to-cleanup
    (if (empty? goals-to-delete)
      goals-without-id
      (recur goals-without-id (first goals-to-delete)))))

(defn link [goals a b]
  "Creates a new link. Goal b now blocks goal a"
  (let [b-index (idx goals b)
        old-deps (:depends (nth goals b-index))]
    (if (or (= a b) (= b 1) (.contains old-deps a))
      goals
      (assoc-in goals [b-index :depends] (conj old-deps a)))))

(defn unlink [goals a b]
  "Removes existing link between goals a and b"
  (let [b-index (idx goals b)
        old-deps (:depends (nth goals b-index))]
    (if (= 1 (count old-deps))
      goals
      (assoc-in goals [b-index :depends] (vec (remove #(= % a) old-deps))))))
