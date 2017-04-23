(ns transducers-deep-dive.stop-when
  (:require [net.cgrand.xforms :as x]))


(defn stop-when
  "Transducer that stops a transduction (akin to halt-when) but let the
  transducers downstream of it call there completion arity

  I just realised you can do the very same thing with
  `(take-while (complement))`..."
  [pred]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result comment] (if (pred comment)
                          (ensure-reduced result)
                          (rf result comment))))))

(defn simpler-stop-when [pred]
  (take-while (complement pred)))

(defn spy-xform [rf]
  (fn
    ([] (let [init-value (rf)]
            (println "init produced:" init-value)
            init-value))
    ([result]
     (println "completion received:" result)
     (let [completed-value (rf result)]
       (println "completion produced:" completed-value)
       completed-value))
    ([result input]
     (println "step input:" input)
     (rf result input))))

(defn comments-by-type
  ([comments] (comments-by-type comments #{}))
  ([comments visited]
   (into {}
         (x/by-key :type
                   (x/transjuxt {:total (comp
                                         (simpler-stop-when #(contains? visited (:type %)))
                                         ;; (stop-when #(contains? visited (:type %)))
                                         ;;  spy-xform
                                         x/count)
                                 :comments (x/into [])}))
         comments)))
