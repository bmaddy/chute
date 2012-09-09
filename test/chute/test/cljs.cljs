(ns chute.test.cljs)
  ;(:require [chute.cljs :as chute]))

(defn exit [v]
  (.exit js/phantom v))

(defn run-tests []
  (.log js/console "Example test started.")
  (assert (= 0 1)))

(try (run-tests)
  (catch js/Error e
    (do
      (.log js/console (.-message e))
      (.log js/console "\n****ERROR: TESTS FAILED****\n")
      (exit 1))))
(exit 0)
