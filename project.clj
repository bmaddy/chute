(defproject chute/chute "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/algo.monads "0.1.0"]]
  :cljsbuild {
              :crossovers [chute.crossover]
              :crossover-path "crossover-cljs"
              :crossover-jar false
              :builds {:dev {:source-path "src-cljs"
                             :compiler {:output-to "resources/public/js/main.js"
                                        :optimizations :whitespace
                                        :pretty-print true}}
                       :test {:source-path "test-cljs"
                             :compiler {:output-to "resources/private/js/unit-test.js"
                                        :optimizations :whitespace
                                        :pretty-print true}}}
              :test-commands {"unit" ["phantomjs"
                                      "phantom/unit-test.js"
                                      "resources/private/html/unit-test.html"]}})

