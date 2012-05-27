; TODO
; set up git repo
; figure out the right way to use cont-m, don't use (:m-bind cont-m), it's bad to use internal functionality
; find an overview of the Rx.net api (without going through their legal crap)
; take a look at FrTime (or whatever that scheme library was called) and compare it to Rx.net.
; consider making (observable) return a function that can just be called with an event. We might need a different function that does what observable does now to do this

(ns chute.core
  (:use [clojure.algo.monads]))

(defn mf-a [x]
  (println "start mf-a; x = " x)
  (fn [c]
    (println "inside mf-a; c = " c)
    (c (inc x))))

(defn mf-b [x]
  (println "start mf-b; x = " x)
  (fn [c]
    (println "inside mf-b; c = " c)
    (c (* 2 x))))

(defn mf-c [x]
  (println "start mf-c; x = " x)
  (fn [c]
    (println "inside mf-c; c = " c)
    (c (dec x))))

;(((domonad cont-m
;           [a mf-a]
;           a) 5) identity)

;(((fn [x] (domonad cont-m
;                   [a (mf-a x)
;                    b (mf-b a)]
;                   b)) 21) identity)

(defn observable []
  ;(m-result (fn [event] nil)))
  ((:m-result cont-m) (fn [event] nil)))

(defn event [m e]
  ((m identity) e))

(defn subscribe [m observer]
  (let [f (fn [value]
            (fn [cont]
              (fn [e]
                (observer e)
                ((cont value) e))))]
    ((:m-bind cont-m) m f)))

; FIXME
;(defn keep [m pred]
;  (let [f (fn [value]
;            (fn [cont]
;              (fn [e]
;                (if (pred e)
;                  ((cont value) e)))))]
;    ((:m-bind cont-m) m f)))

(defn subscribe [observable observer]
  ((:m-bind observable) observer))

(defn on-event [observer event]
  ((:m-result observer) event))

;(defprotocol Observable
;  (subscribe [observer]))
;(defprotocol Observer
;  (on-event [observable event])
;  (on-done [observable]))
;
;; C<T> :: (T -> ()) -> ()
;(extend-type Observable
;  BasicObservable
;  ; Observable -> IPersistentCollection?
;  ; Observable -> Observer -> ()
;  ; C<T> -> O<T> -> ()
;  ; \o: (T -> ()) -> ()
;  ; \t2n: (T -> ())
;  ; subscribe \o \t2n = \o \t2n
;  (subscribe [observable observer]
;    ((:m-bind observable) observer)
;    ))
;
;; O<T> :: T -> ()
;(extend-type Observer
;  BasicObserver
;  ; Observer<T> -> T -> ()
;  ; C<T> -> E -> ()
;  ; ct: (T -> ()) -> ()
;  ; e: E
;  ; on-event ct e = ct (identity e)
;  ;
;  ; Observable<T> -> Observer<T> -> E -> ()
;  ; C<T> -> O<T> -> E -> ()
;  ; \o: (T -> ()) -> ()
;  ; \t2n: T -> ()
;  ; t: T
;  ; on-event \o \t2n t = (t2n t)
;  (on-event [observer event]
;    ;((:m-result observer) (fn [e] nil))
;    ((:m-result observer) event))
;  ; Observer<T> -> ()
;  ;(on-done [observer]
;  ;  )))

;Return :: T -> C<T>
; \t: T
; \o: (T -> ()) -> ()
; \t -> (\o -> o t) -- 'o t' means call o with t
;Bind :: C<T> -> (T -> C<S>) -> C<S>
; mt: (T -> ()) -> ()
; t2ms: T -> (S -> ()) -> ()
; result: (S -> ()) -> ()
; bind mt t2ms = \ s2n -> 
;   mt ((flip t2ms) s2n)

; Observable<T> :: (T -> ()) -> ()
; Observable<T> :: Observable<T> -> (T -> ()) -> Observable<T>
; subscribe :: Observer<T> -> Observable<T>,Disposable
; event :: Observer<T> -> Observable<T>,Disposable
; Observer
; on-event :: Observer<T> -> E -> ()
; on-event :: (T -> C<T>) -> E -> ()
; on-done :: Observer<T> -> E -> ()
;
; delay :: Observable<T> -> TimeSpan -> ???

(def bind (with-monad cont-m m-bind))
(def result (with-monad cont-m m-result))

(defn subscribe [t2n] (fn [e] (bind (result e) (fn [x] (t2n x) (result x)))))
(defn subscribe [t2n] (fn [m] (bind m (fn [x] (t2n x) (result x)))))

(def a (subscribe (fn [e] (print "from a" e "\n"))))
((a (result 5)) identity)
(def b (subscribe (fn [e] (print "from b" e "\n"))))
((b (a (result 5))) identity)

(defn observable
  ([] (observable {}))
  ([overrides] (merge cont-m
                      {:subscribe (fn [m t2n] (bind m (fn [t] (t2n t) ((:m-result cont-m) t))))
                       :event (fn [e] )}
                      overrides)))

; this is the function that would be assigned to something like onclick
(def basic-handler identity)
(defn get-handler []
  basic-handler)
(defn set-handler [f]
  (def basic-handler f))
; appender :: orig -> new -> combined
; appender :: (e -> ()) -> (e -> ()) -> combined
(defn append-handler [f]
  (let [old (get-handler)]
    (def basic-handler (fn [e]
                   (old e)
                   (f e)))))

(defn from-basic-event [] (result (get-handler)))
;(defn subscribe [t2n] (fn [m] (bind m (fn [x] (t2n x) (result x)))))

; from-basic-event
; takes in a function to add a new handler to something
; wraps that into a monadic value
; handler :: e -> ()
; append-handler :: handler -> ()

; from-basic-event :: appender -> M appender
(defn from-basic-event [appender] (result appender))

; subscribe :: M appender -> handler -> ()
; must call (appender handler)
(defn subscribe [m handler]
  ((bind m (fn [appender] (result (appender handler)))) identity))

; fiter :: M appender<a> -> predicate -> M appender<b>
; fiter :: M (handler -> ()) -> predicate -> M (handler-with-pred -> ())
(defn myfilter [m pred]
  (bind m (fn [appender]
            (result (fn [handler]
                      (appender (fn [e]
                                  (if (pred e)
                                    (handler e)))))))))

(defn- set-timeout [func amount]
  (.start (Thread. (fn []
                     (Thread/sleep amount)
                     (func)))))

; delay :: M apender -> amount -> M appender
(defn sleep [m amount]
  (bind m (fn [appender]
            (result (fn [handler]
                      (appender (fn [e]
                                  (set-timeout #(handler e) amount))))))))
;(def o (from-basic-event append-handler))
;(subscribe o #(print "subscribed " % "\n"))
;(basic-handler :foo)
;(def delayed (sleep o 1000))
;(subscribe delayed #(print "delayed " % "\n"))
;(basic-handler :foo)
;(def small-delay (sleep o 500))
;(def double-delay (sleep delayed 500))
;(subscribe small-delay #(println "small-delay " %))
;(subscribe double-delay #(println "double-delay " %))
;(basic-handler :foo) (Thread/sleep 2500)
