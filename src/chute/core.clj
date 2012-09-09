(ns chute.core)

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

(defn bind
  "continuation monad - bind"
  [mv f]
  (fn [c]
    (mv (fn [v] ((f v) c)))))
(defn result
  "continuation monad - result"
  [v]
  (fn [c] (c v)))

; this is the function that would be assigned to something like onclick
(def basic-handler (atom identity))
(defn get-handler []
  @basic-handler)
(defn set-handler [f]
  (reset! basic-handler f))
; appender :: orig -> new -> combined
; appender :: (e -> ()) -> (e -> ()) -> combined
(defn append-handler [f]
  (let [old (get-handler)]
    (reset! basic-handler (fn [e]
                   (old e)
                   (f e)))))

; the handler in here will eventually be something like an onclick handler
; make something like (build-js-event-handler onclick)
(defn build-handler []
  "Builds a map that provides event and append functions"
  (let [handler (atom identity)
        event #(@handler %)
        ; append :: orig -> new -> combined
        ; append :: (e -> ()) -> (e -> ()) -> combined
        append (fn [f]
                 (let [old @handler]
                   (reset! handler (fn [e]
                                     (old e)
                                     (f e)))))]
    {:event event :append append}))

; from-basic-event
; takes in a function to add a new handler to something
; wraps that into a monadic value
; handler :: e -> ()
; append-handler :: handler -> ()

; from-basic-event :: appender -> M appender
(defn from-basic-event [appender]
  (result appender))

; subscribe :: M appender -> handler -> ()
; must call (appender handler)
(defn subscribe [m handler]
  ((bind m (fn [appender]
             (result (appender handler))))
     identity))

; fiter :: M appender<a> -> predicate -> M appender<b>
; fiter :: M (handler -> ()) -> predicate -> M (handler-with-pred -> ())
(defn filter* [m pred]
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
;(subscribe o #(println "subscribed " %))
;(@basic-handler :foo)
;(def delayed (sleep o 1000))
;(subscribe delayed #(print "delayed " % "\n"))
;(@basic-handler :foo)
;(def small-delay (sleep o 500))
;(def double-delay (sleep delayed 500))
;(subscribe small-delay #(println "small-delay " %))
;(subscribe double-delay #(println "double-delay " %))
;(@basic-handler :foo) (Thread/sleep 2500)

;(def handler (build-handler))
;(def o (from-basic-event (:append handler)))
;(subscribe o #(println "subscribed " %))
;((:event handler) :foo)
;(def delayed (sleep o 1000))
;(subscribe delayed #(print "delayed " % "\n"))
;FIXME these aren't showing up at the right times anymore (and add tests)
; does it work with the old append-handler code? no
; does it work with the non-ref based code?
; did it even work before?
;((:event handler) :foo) (Thread/sleep 1500)
;(def small-delay (sleep o 500))
;(def double-delay (sleep delayed 500))
;(subscribe small-delay #(println "small-delay " %))
;(subscribe double-delay #(println "double-delay " %))
;((:event handler) :foo) (Thread/sleep 2500)
