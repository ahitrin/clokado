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
