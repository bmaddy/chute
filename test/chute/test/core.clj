(ns chute.test.core
  (:use [chute.core])
  (:use [clojure.test]))

(deftest basic
  (def changed false)
  ; observable returns nothing when not being watched
  (let [observable (from-basic-event append-handler)]
    (basic-handler :foo)
    (is (not changed))))

(deftest subscribe-test
  (def changed false)
  (print "test print")
  (let [subj (subscribe (from-basic-event append-handler)
                        (fn [e] (print "CHANGED") (def changed e)))]
    (is (not changed))
    (basic-handler :foo)
    (is (= :foo changed))
    (basic-handler :bar)
    (is (= :bar changed))))

(deftest multiple-subscribers-test
  (def changed-first false)
  (def changed-second false)
  (def o (from-basic-event append-handler))
  (subscribe o (fn [e] (def changed-first e)))
  (subscribe o (fn [e] (def changed-second e)))
  (is (not changed-first))
  (is (not changed-second))
  (basic-handler :foo)
  (is (= :foo changed-first))
  (is (= :foo changed-second))
  (basic-handler :bar)
  (is (= :bar changed-first))
  (is (= :bar changed-second)))

(deftest filter-test
  (def changed false)
  (def filtered (myfilter (from-basic-event append-handler)
                                   (fn [e] (= e :foo))))
  (subscribe filtered (fn [e] (def changed e)))
  (is (not changed))
  (basic-handler :foo)
  (is (= :foo changed))
  (basic-handler :bar)
  (is (= :foo changed)))

(deftest filter-with-mult-subscribers-test
  (def changed-first false)
  (def changed-second false)
  (def calls 0)
  (def filtered (myfilter (from-basic-event append-handler)
                                   (fn [e]
                                     (def calls (inc calls))
                                     (= e :foo))))
  (subscribe filtered (fn [e] (def changed-first e)))
  (subscribe filtered (fn [e] (def changed-second e)))
  (is (not changed-first))
  (is (not changed-second))
  ;(is (= 0 calls))
  (basic-handler :foo)
  (is (= :foo changed-first))
  (is (= :foo changed-second))
  ;(is (= 1 calls))
  (basic-handler :bar)
  (is (= :bar changed-first))
  (is (= :bar changed-second)))

(deftest filter-some-test
  (def changed-first false)
  (def changed-second false)
  (def calls 0)
  (def orig (from-basic-event append-handler))
  (def filtered (myfilter orig
                          (fn [e]
                            (def calls (inc calls))
                            (= e :foo))))
  (subscribe filtered (fn [e] (def changed-first e)))
  (subscribe orig (fn [e] (def changed-second e)))
  (is (not changed-first))
  (is (not changed-second))
  (is (= 0 calls))
  (basic-handler :foo)
  (is (= :foo changed-first))
  (is (= :foo changed-second))
  (is (= 1 calls))
  (basic-handler :bar)
  (is (= :foo changed-first))
  (is (= :bar changed-second)))


;(deftest basic
;  ; observable returns nothing when not being watched
;  (is (nil? (((observable) identity) :foo)))
;  (is (nil? (event (observable) :foo))))
;
;(deftest subscribe-test
;  (def changed false)
;  (let [subj (subscribe (observable)
;                        (fn [e] (def changed e)))]
;    (is (= false changed))
;    (event subj :foo)
;    (is (= :foo changed))
;    (event subj :bar)
;    (is (= :bar changed))))
;
;(deftest multiple-subscribers-test
;  (def changed-first false)
;  (def changed-second false)
;  (def subj (-> (observable)
;              (subscribe (fn [e] (def changed-first e)))
;              (subscribe (fn [e] (def changed-second e)))))
;  (is (not changed-first))
;  (is (not changed-second))
;  (event subj :foo)
;  (is (= :foo changed-first))
;  (is (= :foo changed-second))
;  (event subj :bar)
;  (is (= :bar changed-first))
;  (is (= :bar changed-second)))
;
;(deftest select-test
;  (def changed false)
;  (def calls 0)
;
