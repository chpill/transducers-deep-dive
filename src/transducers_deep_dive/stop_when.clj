(ns transducers-deep-dive.stop-when
  (:require [net.cgrand.xforms :as x]))


(defn stop-when
  "Transducer that aborts"
  [pred]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result comment] (if (pred comment)
                          (ensure-reduced result)
                          (rf result comment))))))

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
                   (x/transjuxt {:total (comp (stop-when #(contains? visited (:type %)))
                                              ;;spy-xform
                                              x/count)
                                 :comments (x/into [])}))
         comments)))
