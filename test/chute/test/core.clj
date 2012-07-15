(ns chute.test.core
  (:use [chute.core])
  (:use [clojure.test]))

(deftest subscribe-test
  (let [handler (build-handler)
        changed (atom false)
        subj (subscribe (from-basic-event (:append handler))
                        (fn [e] (reset! changed e)))]
    (is (not @changed))
    ((:event handler) :foo)
    (is (= :foo @changed))
    ((:event handler) :bar)
    (is (= :bar @changed))))

(deftest multiple-subscribers-test
  (let [handler (build-handler)
        changed-first (atom false)
        changed-second (atom false)
        o (from-basic-event (:append handler))]
    (subscribe o (fn [e] (reset! changed-first e)))
    (subscribe o (fn [e] (reset! changed-second e)))
    (is (not @changed-first))
    (is (not @changed-second))
    (dosync
      ((:event handler) :foo)
      (is (= :foo @changed-first))
      (is (= :foo @changed-second))
      ((:event handler) :bar)
      (is (= :bar @changed-first))
      (is (= :bar @changed-second)))))

(deftest filter-test
  (let [handler (build-handler)
        changed (atom false)
        filtered (filter* (from-basic-event (:append handler))
                          (fn [e] (= e :foo)))]
    (subscribe filtered (fn [e] (reset! changed e)))
    (dosync
      (is (not @changed))
      ((:event handler) :foo)
      (is (= :foo @changed))
      ((:event handler) :bar)
      (is (= :foo @changed)))))

(deftest filter-with-mult-subscribers-test
  (let [handler (build-handler)
        changed (atom false)
        changed-first (atom false)
        changed-second (atom false)
        calls (atom 0)
        filtered (filter* (from-basic-event (:append handler))
                          (fn [e]
                            (swap! calls inc)
                            (= e :foo)))]
    (subscribe filtered (fn [e] (reset! changed-first e)))
    (subscribe filtered (fn [e] (reset! changed-second e)))
    (is (not @changed-first))
    (is (not @changed-second))
    (is (= 0 @calls))
    (dosync
      ((:event handler) :foo)
      (is (= :foo @changed-first))
      (is (= :foo @changed-second))
      ; maybe do this in the future
      ;(is (= 1 @calls))
      ((:event handler) :bar)
      (is (= :foo @changed-first))
      (is (= :foo @changed-second)))
      ; maybe do this in the future
      ;(is (= 2 @calls))
    ))

(deftest filter-some-test
  (let [handler (build-handler)
        changed (atom false)
        changed-first (atom false)
        changed-second (atom false)
        calls (atom 0)
        orig (from-basic-event (:append handler))
        filtered (filter* orig
                          (fn [e]
                            (swap! calls inc)
                            (= e :foo)))]
    (subscribe filtered (fn [e] (reset! changed-first e)))
    (subscribe orig (fn [e] (reset! changed-second e)))
    (is (not @changed-first))
    (is (not @changed-second))
    (is (= 0 @calls))
    (dosync
      ((:event handler) :foo)
      (is (= :foo @changed-first))
      (is (= :foo @changed-second))
      (is (= 1 @calls))
      ((:event handler) :bar)
      (is (= :foo @changed-first))
      (is (= :bar @changed-second)))))

(deftest sleep-test
  (let [handler (build-handler)
        calls (atom [])
        o (from-basic-event (:append handler))
        delayed (sleep o 10)]
    (subscribe delayed (fn [x] (swap! calls #(conj % x))))
    ((:event handler) :foo)
    (is (= [] @calls))
    (Thread/sleep 15)
    (is (= [:foo] @calls))))
