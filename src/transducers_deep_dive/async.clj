(ns transducers-deep-dive.async)

;; Examples of code from Timothy Baldridge's Clojure West presentation
;; https://www.youtube.com/watch?v=096pIlA3GDo

(defn get-page
  "Simulates an async page request"
  [idx cb]
  (println "Fetching page" idx)
  ;; Simulates network call
  (Thread/sleep 100)
  (future
    ;;Dispatch on a different thread
    (cb (vec (range (* idx 5)
                    (* (inc idx) 5))))))

(defn pages [xf rf cb]
  (let [f (xf rf)
        start (fn process-page [idx acc page]
                (try
                  (let [result (f acc page)]
                    (if (reduced? result)
                      (cb @result)
                      (get-page (inc idx)
                                (partial process-page
                                         (inc idx)
                                         result))))
                  (catch Exception e
                    (cb (str "caught: " (.getMessage e))))))]
    (get-page 0 (partial start 0 (f)))))

(def batch-filter-sampler
  (comp cat
        (filter #(> % 10))
        (take 10)))

(def batch-faulty-filter-sampler
  (comp cat
        (filter (fn i-fail [x]
                  (if (< x 12)
                    x
                    (throw (Exception. (str "lol, keep your filthy " x))))))
        (filter odd?)
        (take 10)))


;; Dataflow - Incomplete
;; Maybe some implementation details in naiad?
;; https://github.com/halgari/naiad


(defn emit [state port value]
  (update-in state [:outputs port] conj value))

(defn filter-emitter [pred]
  (fn [state msg]
    (if (pred msg)
      (emit state :out msg)
      state)))

(defn split-emitter [key-fn state msg]
  (emit state (key-fn msg) msg))

(defn distinct-emitter [state msg]
  (if (contains? (:seen state) msg)
    state
    (-> (emit state :out msg)
        (update-in :seen (fnil conj #{}) msg))))

(defn transduce-emitter [xf]
  (let [rf (fn [state msg] (emit state :out msg))]
    (xf rf)))

(def filter-node {:inputs {:in (transduce-emitter (filter pos?))}
                  :outputs {:out []}})

(defn ingest-all [graph input-sequence])

(defn connect
  ([graph from-node from-port to-node])
  ([graph from-node from-port to-node to-port]
   ()))

(defn add-node [graph node-name node]
  (assoc graph node-name node))

(defn split-node [key-fn]
  {:inputs {:in (partial split-emitter key-fn)}})

(defn transduce-node [xf]
  {:inputs {:in (transduce-emitter xf)}
   :outputs {:out []}})

(defn graph []
  {:graph-exit {:out #{}}})
