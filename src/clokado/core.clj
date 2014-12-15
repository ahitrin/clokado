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
  [{:name name :id 0 :open true :depends #{}}])

(defn add
  ([goals name]
    "Adds new goal to existing ones, which blocks mikado goal"
    (add goals name 0))
  ([goals name id]
    "Add new goal to existing ones, which blocks goal identified by id"
    (conj goals {:name name :id (count goals) :open true :depends #{id}})))

(defn only-open [goals]
  (filter #(true? (:open %)) goals))

(defn top [goals]
  "Returns a list of open goals which no one goal depends on"
  (let [blocked-goals (->> goals only-open (mapcat :depends) set)]
    (->> goals
         (keep-indexed #(when-not (.contains blocked-goals %1) %2))
         only-open
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
  (let [old-deps (:depends (nth goals b))]
    (if (or (= a b) (zero? b) (.contains old-deps a))
      goals
      (assoc-in goals [b :depends] (set (conj old-deps a))))))

(defn unlink [goals a b]
  "Removes existing link between goals a and b"
  (let [old-deps (:depends (nth goals b))]
    (if (= 1 (count old-deps))
      goals
      (assoc-in goals [b :depends] (set (remove #(= % a) old-deps))))))

(defn delete [goals id]
  "Recursevly removes goal from the tree by id, together with goals that block it.
   Goals that depends also on some other goals, stay alive"
  (loop [gs goals ids (list id)]
    (if (empty? ids)
      gs
      (let [i (first ids)
            deps (->> gs
                      (remove empty?)
                      (map #(list (get % :id) (get % :depends)))
                      (filter #(.contains (second %) i))
                      (group-by #(= 1 (count (second %)))))
            to-remove (map first (deps true))
            to-clean (map first (deps false))
            cleaned-gs (loop [gs gs ids to-clean]
                     (if (empty? ids)
                         gs
                         (recur (unlink gs i (first ids)) (rest ids))))]
        (recur (vec (assoc cleaned-gs i {}))
               (concat (rest ids) to-remove))))))
