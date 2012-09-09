(defproject chute/chute "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :cljsbuild {:builds {:test-simp {:source-path "test"
                                   :compiler {:optimizations :simple
                                              :pretty-print true
                                              :static-fns true
                                              :output-to "tests.js"}}
                       :test-adv {:source-path "test"
                                  :compiler {:optimizations :advanced
                                             :pretty-print true
                                             :output-to "tests.js"}}}})
