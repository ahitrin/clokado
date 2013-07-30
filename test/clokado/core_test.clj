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
      (slice-should-be goals :id '(1)))))

(deftest add-one-goal
  (testing "what happens when we add one goal to the mikado"
    (let [first-goal (mikado "Eat icecream")
          goals (add first-goal "Buy icecream")]
      (slice-should-be goals :name '("Eat icecream" "Buy icecream"))
      (slice-should-be goals :open '(:open :open))
      (slice-should-be goals :depends '([] [1]))
      (slice-should-be goals :id '(1 2)))))

(deftest add-two-goals
  (testing "what happens when we add more goals to the mikado"
    (let [first-goal (mikado "Kill the beast")
          next-goal (add first-goal "Prepare weapon")
          goals (add next-goal "Find the beast")]
      (slice-should-be goals :name '("Kill the beast" "Prepare weapon" "Find the beast"))
      (slice-should-be goals :open '(:open :open :open))
      (slice-should-be goals :depends '([] [1] [1]))
      (slice-should-be goals :id '(1 2 3)))))

(deftest add-two-goals-in-a-chain
  (testing "we should be able to add chains of goals"
    (let [kitty (mikado "Feed the kitty")
          food (add kitty "Find the food")
          goals (add food "Go to the store" 2)]
      (slice-should-be goals :name '("Feed the kitty" "Find the food" "Go to the store"))
      (slice-should-be goals :open '(:open :open :open))
      (slice-should-be goals :depends '([] [1] [2]))
      (slice-should-be goals :id '(1 2 3)))))

;; tests on tree info

(def simplest
  (mikado "Just do it"))

(def kitty
  (add (add (mikado "Feed the kitty") "Find the food") "Go to the store" 2))

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
    (let [goals (close kitty 3)]
      (slice-should-be goals :name (map :name kitty))
      (slice-should-be goals :open '(:open :open :closed))
      (slice-should-be (top goals) :name '("Find the food")))
    (let [goals (close beast 2)]
      (slice-should-be goals :open '(:open :closed :open))
      (slice-should-be (top goals) :name '("Find the beast")))))
