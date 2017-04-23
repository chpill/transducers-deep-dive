(ns transducers-deep-dive.async-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [transducers-deep-dive.async :refer :all]
            [net.cgrand.xforms :as x]))

(deftest get-page-example
  (let [c (async/chan)]
    (get-page 3 #(async/put! c %))
    (is (= [15 16 17 18 19]
           (async/<!! c)))))

(deftest pages-examples
  (let [results-chan (async/chan 10 (partition-all 2))
        send-result (partial async/put! results-chan)]
    (pages batch-filter-sampler
           conj
           send-result)
    (pages batch-faulty-filter-sampler
           conj
           send-result)
    (is (= #{"caught: lol, keep your filthy 12"
             [11 12 13 14 15 16 17 18 19 20]}
           (set (async/<!! results-chan))))))


;; Dataflow

(deftest ingest-all-example
  (ingest-all ))

(deftest syncronous-dataflow
  (is (= #{1 3 4}
         (-> (graph)
             (add-node :split (split-node #(if (odd? %) :odd :even)))
             (add-node :square (transduce-node (map #(* % %))))
             (connect :split :even :square :in)
             (connect :split :odd :graph-exit)
             (connect :square :out :graph-exit)
             (ingest-all (range 1 4))))))
