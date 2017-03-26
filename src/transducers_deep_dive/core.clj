(ns transducers-deep-dive.core
  (:require [net.cgrand.xforms :as x]))

;; Dealing with chunks
;; Original code from datomic training video
;; http://www.datomic.com/day-of-datomic-2016-part-4.html

(defn error? [x] (:error x))

(defn original [chunks]
  (transduce (comp
              (halt-when error?)
              (map #(frequencies (map :a %))))
             (completing (partial merge-with +))
             {}
             chunks))

(defn original-inspect [chunks]
  (transduce (comp
              (halt-when error?)
              (map (fn [datoms]
                     (println "creating a frequency map")
                     (frequencies (map :a datoms)))))
             (fn [& args]
               (println "aggegating frequency tables. Number of arguments?" (count args))
               (partial merge-with +))
             {}
             chunks))


;; Broken when there are errors!
;; naive implementation
(defn x-version-1 [chunks]
  (into {}
        (comp (halt-when error?)
              cat
              (map :a)
              (x/by-key identity x/count))
        chunks))

;; Broken when there are errors!
;; Better use of x/by-key
(defn x-version-2 [chunks]
  (into {}
        (comp (halt-when error?)
              cat
              (x/by-key :a x/count))
        chunks))

;; Ugly but works
;; Seems like into does not play well with halt-when
;; This is very brittle, xducers are meant to be independent of their contexts.
;; This introduces a great deal of coupling to the tranducing context
(defn x-version-3-cheat [chunks]
  (into {}
        (comp (halt-when error?
                         (fn [so-far faulty-val]
                           (transient faulty-val)))
              cat
              (x/by-key :a x/count))
        chunks))

(defn x-version-4 [chunks]
  (transduce (comp (halt-when error?)
                   cat
                   (x/by-key :a x/count)
                   (x/into {}))
             conj
             {}
             chunks))

(defn spy-conj [& args]
  (println "Conj here. Number of arguments:" (count args))
  (apply conj args))

(defn x-version-4-inspect [chunks]
  (transduce (comp (halt-when error?)
                   cat
                   (x/by-key :a x/count)
                   (x/into {}))
             spy-conj
             {}
             chunks))

(defn x-version-4-simpler [chunks]
  (transduce (comp (halt-when error?)
                   cat
                   (x/by-key :a x/count))
             conj
             {}
             chunks))

(defn single-return-transducer
  "Dummy transducer that simply that will simply return the value given to it,
  ignoring any accumulated value"
  ([] :initial-value-to-be-ignored)
  ([final-value] final-value)
  ([initial-value v] v))

(defn spy-single-return-transducer
  "This special transducer will return a single value"
  ([] :initial-value-to-be-ignored)
  ([final-value]
   (println "completion step with final value:" final-value)
   final-value)
  ([initial-value v]
   (println "initial value (ignored):" initial-value)
   (println"v:" v)
   v))

(defn x-version-4-dummy-transducer [chunks]
  (transduce (comp (halt-when error?)
                   cat
                   (x/by-key :a x/count)
                   (x/into {}))
             single-return-transducer
             chunks))
