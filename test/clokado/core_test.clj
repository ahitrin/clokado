(ns clokado.core-test
  (:use clojure.test
        clokado.core))

(defn slice-should-be [goals field expected]
  (is (= (map field goals) expected)))

;; tests on tree creation

(deftest start-test
  (testing "there is one goal at start"
    (let [goals (mikado "Example goal")]
      (slice-should-be goals :name '("Example goal"))
      (slice-should-be goals :open '(:open))
      (slice-should-be goals :depends '([]))
      (slice-should-be goals :id '(0)))))

(deftest add-one-goal
  (testing "what happens when we add one goal to the mikado"
    (let [first-goal (mikado "Eat icecream")
          goals (add first-goal "Buy icecream")]
      (slice-should-be goals :name '("Eat icecream" "Buy icecream"))
      (slice-should-be goals :open '(:open :open))
      (slice-should-be goals :depends '([] [0]))
      (slice-should-be goals :id '(0 1)))))

(deftest add-two-goals
  (testing "what happens when we add more goals to the mikado"
    (let [first-goal (mikado "Kill the beast")
          next-goal (add first-goal "Prepare weapon")
          goals (add next-goal "Find the beast")]
      (slice-should-be goals :name '("Kill the beast" "Prepare weapon" "Find the beast"))
      (slice-should-be goals :open '(:open :open :open))
      (slice-should-be goals :depends '([] [0] [0]))
      (slice-should-be goals :id '(0 1 2)))))

(deftest add-two-goals-in-a-chain
  (testing "we should be able to add chains of goals"
    (let [kitty (mikado "Feed the kitty")
          food (add kitty "Find the food")
          goals (add food "Go to the store" 1)]
      (slice-should-be goals :name '("Feed the kitty" "Find the food" "Go to the store"))
      (slice-should-be goals :open '(:open :open :open))
      (slice-should-be goals :depends '([] [0] [1]))
      (slice-should-be goals :id '(0 1 2)))))

(deftest rename-goal
  (testing "any goal can be renamed"
    (let [goals (add (mikado "first") "cauybfasdfa")]
      (slice-should-be (rename goals 1 "second") :name '("first" "second")))))

;; tests on tree info

(def simplest
  (mikado "Just do it"))

(def kitty
  (add (add (mikado "Feed the kitty") "Find the food") "Go to the store" 1))

(def beast
  (add (add (mikado "Kill the beast") "Prepare weapon") "Find the beast"))

(deftest list-top-goals
  (testing "goal is considered top when no other goal depends on it"
    (slice-should-be (top simplest) :name '("Just do it"))
    (slice-should-be (top kitty) :name '("Go to the store"))
    (slice-should-be (top beast) :name '("Prepare weapon" "Find the beast"))))

;; tests on goal closing

(deftest close-simlpe-goal
  (testing "what happens when simple goal is being closed"
    (let [goals (close kitty 2)]
      (slice-should-be goals :name (map :name kitty))
      (slice-should-be goals :open '(:open :open :closed))
      (slice-should-be (top goals) :name '("Find the food")))
    (let [goals (close beast 1)]
      (slice-should-be goals :open '(:open :closed :open))
      (slice-should-be (top goals) :name '("Find the beast")))))

(deftest reopen-goal
  (testing "we can close goal, and then reopen it again"
    (let [goals (reopen (close kitty 2) 2)]
      (slice-should-be goals :open '(:open :open :open)))))

;; tests on goal removing

(deftest delete-single-goal
  (testing "comletely remove goal from tree"
    (let [goals (delete (add (mikado "Go for a walk") "Do homework") 1)]
      (slice-should-be goals :name '("Go for a walk")))))

(deftest delete-goal-and-close-another-one
  (testing "removal of goals doesn't break goal closing"
    (let [goals (close (delete beast 1) 2)]
      (slice-should-be goals :name '("Kill the beast" "Find the beast"))
      (slice-should-be goals :open '(:open :closed)))))

(deftest remove-goal-chain
  (testing "when some goals block deleted goal, they should be removed too"
    (let [goals (delete (add (add kitty "dummy" 1) "dummy" 1) 1)]
      (slice-should-be goals :name '("Feed the kitty")))))

(deftest remove-goal-in-the-middle
  (testing "do not remove blocking goal when it depends on another goal"
    (let [goals (delete (link kitty 0 2) 1)]
      (slice-should-be goals :name '("Feed the kitty" "Go to the store"))
      (slice-should-be goals :depends '([] [0])))))

;; tests on additional links

(deftest add-link-between-goals
  (testing "we can add more links between goals"
    (slice-should-be (link beast 1 2) :depends '([] [0] [0 1]))))

(deftest add-link-restrictions
  (testing "link addition must not break mikado tree goal order"
    (slice-should-be (link simplest 0 0) :depends '([]))
    (slice-should-be (link kitty 1 2) :depends '([] [0] [1]))
    (slice-should-be (link kitty 1 0) :depends '([] [0] [1]))))

(deftest remove-link-between-goals
  (testing "we can remove added links"
    (slice-should-be (unlink (link kitty 2 1) 2 1) :depends '([] [0] [1]))))

(deftest remove-link-restrictions
  (testing "link removals must not break mikado tree goal order"
    (slice-should-be (unlink kitty 0 1) :depends '([] [0] [1]))))

(comment
  (map #(ns-unmap *ns* %) (keys (ns-interns *ns*)))
  (run-tests))
