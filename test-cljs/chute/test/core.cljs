(ns chute.test.core
    (:use [chute.core :only [core-func using-common]]
          [chute.crossover.foo :only [common-func]]
          ;[clojure.algo.monads :only [with-monad cont-m m-bind m-result]]
          ))

(defn run []
    (assert (= (+ 2 2) 4))
    (assert (= (+ 1 2 3) 6))
    (assert (= (+ 4 5 6) 15))
    (assert (= (core-func) "core func"))
    (assert (= (using-common) "using common func"))
    (assert (= (common-func) "common func"))
  )

