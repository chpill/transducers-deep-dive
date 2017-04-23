(ns transducers-deep-dive.stop-when-test
  (:require [transducers-deep-dive.stop-when :refer :all]
            [clojure.test :refer :all]))

(defn make-comments [n]
  (for [i (range n)]
    {:content (str "plop " i) :type (mod i 3)}))


(deftest comments-by-type-examples
  (let [comments (make-comments 9)]
    (is (= {0 {:total 3
               :comments
               [{:content "plop 0", :type 0}
                {:content "plop 3", :type 0}
                {:content "plop 6", :type 0}]}
            1 {:total 3
               :comments
               [{:content "plop 1", :type 1}
                {:content "plop 4", :type 1}
                {:content "plop 7", :type 1}]}
            2 {:total 3
               :comments
               [{:content "plop 2", :type 2}
                {:content "plop 5", :type 2}
                {:content "plop 8", :type 2}]}}
           (comments-by-type comments)))
    (is (= {0 {:total 3
               :comments
               [{:content "plop 0", :type 0}
                {:content "plop 3", :type 0}
                {:content "plop 6", :type 0}]}
            1 {:total 0
               :comments
               [{:content "plop 1", :type 1}
                {:content "plop 4", :type 1}
                {:content "plop 7", :type 1}]}
            2 {:total 0
               :comments
               [{:content "plop 2", :type 2}
                {:content "plop 5", :type 2}
                {:content "plop 8", :type 2}]}}
           (comments-by-type comments #{1 2})))))
