(ns chute.core
  (:use [chute.foo :only [common-func]])
  )

(js/alert "Hello from ClojureScript!")

(defn core-func []
  "core func")

(defn using-common []
  (str "using " (common-func)))

