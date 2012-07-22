(ns chute.test
    (:require [chute.test.core :as core])
  )

(def success 0)

(defn ^:export run []
    (.log js/console "Example test started.")
    (core/run)
    success)

