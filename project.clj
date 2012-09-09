(defproject chute/chute "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.2.5"]]
  ; Enable the lein hooks for: clean, compile, test, and jar.
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
              :test-commands
              ; Test command for running the unit tests in "test-cljs" (see below).
              ;     $ lein cljsbuild test
              {"unit" ["phantomjs"
                       "resources/private/js/unit-test.js"]}
              :builds {:test {:source-path "test"
                              :compiler {:optimizations :simple
                                         :pretty-print true
                                         :static-fns true
                                         :output-to "resources/private/js/unit-test.js"}}
                       :test-adv {:source-path "test"
                                  :compiler {:optimizations :advanced
                                             :pretty-print true
                                             :output-to "resources/private/js/unit-test-adv.js"}}}})
