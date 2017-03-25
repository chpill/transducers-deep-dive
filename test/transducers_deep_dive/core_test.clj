(ns transducers-deep-dive.core-test
  (:require [clojure.test :refer :all]
            [com.rpl.specter :as specter]
            [criterium.core :as criterium]
            [net.cgrand.xforms :as x]
            [transducers-deep-dive.core :refer :all]))

(defn make-chunks [chunk-count chunk-size]
  (for [i (range chunk-count)]
    (for [j (range chunk-size)]
         {:a (mod j 3)})))

(def chunks (make-chunks 10 999))
(def mini-chunks (make-chunks 3 5))

(defn make-faulty-chunks [navigator chunk-count chunk-size]
  (->> ;;(make-chunks chunk-count chunk-size)
       (make-chunks 3 3)
       (specter/setval [navigator] {:error "something terrible happened here!"})))

(def mini-tail-faulty-chunks (make-faulty-chunks specter/LAST 10 9))
(def tail-faulty-chunks (make-faulty-chunks specter/LAST 10 999))

(def mini-head-faulty-chunks (make-faulty-chunks specter/FIRST 10 9))
(def head-faulty-chunks (make-faulty-chunks specter/FIRST 10 999))

(deftest good-chunks
  (is (= {0 3330 1 3330 2 3330}
         (original chunks)
         (x-version-1 chunks)
         (x-version-2 chunks)
         (x-version-3-cheat chunks)
         (x-version-4 chunks))))

(deftest error-chunks
  (testing "error in the last chunk"
    (is (= {:error "something terrible happened here!"}
           (original tail-faulty-chunks)
           (x-version-3-cheat tail-faulty-chunks)
           (x-version-4 tail-faulty-chunks)))))

(comment
  "
Some benchmarks of different chunk processing functions
=======================================================

Benching when all batches are good
----------------------------------
"
  ;; Note that the standard deviation is huge here
  (criterium/quick-bench (original chunks))
  "
Evaluation count              : 294 in 6 samples of 49 calls.
Execution time mean           : 10.778696 ms
Execution time std-deviation  : 8.818884 ms
Execution time lower quantile : 1.680341 ms ( 2.5%)
Execution time upper quantile : 22.618172 ms (97.5%)
Overhead used                 : 61.555210 ns
"
  ;; Let's do a more thorough bench
  (criterium/bench (original chunks))
  "
Evaluation count              : 46800 in 60 samples of 780 calls.
Execution time mean           : 1.314476 ms
Execution time std-deviation  : 34.540276 µs
Execution time lower quantile : 1.275610 ms ( 2.5%)
Execution time upper quantile : 1.405845 ms (97.5%)
Overhead used                 : 1.976022 ns

Found 3 outliers in 60 samples (5.0000 %)
low-severe 1 (1.6667 %)
low-mild	 2 (3.3333 %)
Variance from outliers : 14.1514 % Variance is moderately inflated by outliers
"

  (criterium/quick-bench (x-version-4 chunks))
  "
Evaluation count              : 1320 in 6 samples of 220 calls.
Execution time mean           : 464.794079 µs
Execution time std-deviation  : 8.634318 µs
Execution time lower quantile : 453.641818 µs ( 2.5%)
Execution time upper quantile : 475.406465 µs (97.5%)
Overhead used                 : 1.976022 ns
"
  (criterium/quick-bench (x-version-3-cheat chunks))
  "
Evaluation count              : 1260 in 6 samples of 210 calls.
Execution time mean           : 476.083571 µs
Execution time std-deviation  : 12.284516 µs
Execution time lower quantile : 463.180057 µs ( 2.5%)
Execution time upper quantile : 488.899937 µs (97.5%)
Overhead used                 : 1.976022 ns
"

  "
Now with an error at the end of the batch!
------------------------------------------
"
  (criterium/quick-bench (original tail-faulty-chunks))
  "
Evaluation count              : 147966 in 6 samples of 24661 calls.
Execution time mean           : 4.104593 µs
Execution time std-deviation  : 70.391249 ns
Execution time lower quantile : 4.029229 µs ( 2.5%)
Execution time upper quantile : 4.181367 µs (97.5%)
Overhead used                 : 1.976022 ns
"
  (criterium/quick-bench (x-version-4 tail-faulty-chunks))
  "
Evaluation count              : 296832 in 6 samples of 49472 calls.
Execution time mean           : 2.038262 µs
Execution time std-deviation  : 82.861443 ns
Execution time lower quantile : 1.977813 µs ( 2.5%)
Execution time upper quantile : 2.175440 µs (97.5%)
Overhead used                 : 1.976022 ns

Found 1 outliers in 6 samples (16.6667 %)
low-severe	 1 (16.6667 %)
Variance from outliers : 13.8889 % Variance is moderately inflated by outliers
"

"
What about at the beginning of the batch?
-----------------------------------------
"
(criterium/quick-bench (original head-faulty-chunks))
"
Evaluation count              : 2399988 in 6 samples of 399998 calls.
Execution time mean           : 255.638779 ns
Execution time std-deviation  : 8.311185 ns
Execution time lower quantile : 247.410880 ns ( 2.5%)
Execution time upper quantile : 268.966240 ns (97.5%)
Overhead used                 : 1.976022 ns

Found 1 outliers in 6 samples (16.6667 %)
	low-severe	 1 (16.6667 %)
Variance from outliers : 13.8889 % Variance is moderately inflated by outliers
"
(criterium/quick-bench (x-version-4 head-faulty-chunks))
"
Evaluation count              : 954786 in 6 samples of 159131 calls.
Execution time mean           : 636.765298 ns
Execution time std-deviation  : 10.266764 ns
Execution time lower quantile : 621.753272 ns ( 2.5%)
Execution time upper quantile : 647.583049 ns (97.5%)
Overhead used                 : 1.976022 ns
"
  )
