(ns clokado.core-test
  (:use clojure.test
        clokado.core))

(deftest start-test
  (testing "there is one goal at start"
    (let [goals (mikado "Example goal")]
      (is (= (count goals) 1))
      (is (= (map :name goals) '("Example goal")))
      (is (= (map :open goals) '(true)))
      (is (= (map :depends goals) '([])))
      (is (= (map :id goals) '(1))))))

(deftest add-one-node
  (testing "what happens when we add one goal to the mikado"
    (let [first-goal (mikado "Eat icecream")
          goals (add first-goal "Buy icecream")]
      (is (= (map :name goals) '("Eat icecream" "Buy icecream")))
      (is (= (map :open goals) '(true true)))
      (is (= (map :depends goals) '([] [1])))
      (is (= (map :id goals) '(1 2))))))

(deftest add-two-nodes
  (testing "what happens when we add more goals to the mikado"
    (let [first-goal (mikado "Kill the beast")
          next-goal (add first-goal "Prepare weapon")
          goals (add next-goal "Find the beast")]
      (is (= (map :name goals) '("Kill the beast" "Prepare weapon" "Find the beast")))
      (is (= (map :open goals) '(true true true)))
      (is (= (map :depends goals) '([] [1] [1])))
      (is (= (map :id goals) '(1 2 3))))))
