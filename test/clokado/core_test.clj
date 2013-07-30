(ns clokado.core-test
  (:use clojure.test
        clokado.core))

(defn slice-should-be [goals field expected]
  (is (= (map field goals) expected)))

(deftest start-test
  (testing "there is one goal at start"
    (let [goals (mikado "Example goal")]
      (slice-should-be goals :name '("Example goal"))
      (slice-should-be goals :open '(true))
      (slice-should-be goals :depends '([]))
      (slice-should-be goals :id '(1)))))

(deftest add-one-node
  (testing "what happens when we add one goal to the mikado"
    (let [first-goal (mikado "Eat icecream")
          goals (add first-goal "Buy icecream")]
      (slice-should-be goals :name '("Eat icecream" "Buy icecream"))
      (slice-should-be goals :open '(true true))
      (slice-should-be goals :depends '([] [1]))
      (slice-should-be goals :id '(1 2)))))

(deftest add-two-nodes
  (testing "what happens when we add more goals to the mikado"
    (let [first-goal (mikado "Kill the beast")
          next-goal (add first-goal "Prepare weapon")
          goals (add next-goal "Find the beast")]
      (slice-should-be goals :name '("Kill the beast" "Prepare weapon" "Find the beast"))
      (slice-should-be goals :open '(true true true))
      (slice-should-be goals :depends '([] [1] [1]))
      (slice-should-be goals :id '(1 2 3)))))

(deftest add-two-nodes-in-a-chain
  (testing "we should be able to add chains of goals"
    (let [kitty (mikado "Feed the kitty")
          food (add kitty "Find the food")
          goals (add food "Go to the store" 2)]
      (slice-should-be goals :name '("Feed the kitty" "Find the food" "Go to the store"))
      (slice-should-be goals :open '(true true true))
      (slice-should-be goals :depends '([] [1] [2]))
      (slice-should-be goals :id '(1 2 3)))))
