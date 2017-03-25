(def project 'transducers-deep-dive)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.9.0-alpha15"]
                            [net.cgrand/xforms "0.8.3"]
                            [com.rpl/specter "1.0.0"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [criterium "0.4.4"]])

(require '[adzerk.boot-test :refer [test]])
