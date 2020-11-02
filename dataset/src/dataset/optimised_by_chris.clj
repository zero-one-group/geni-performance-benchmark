(ns dataset.optmised-by-chris
  (:require
    [tech.v3.io :as io]
    [tech.v3.datatype.functional :as dfn]
    [tech.v3.datatype :as dtype]
    [tech.v3.datatype.casting :as casting]
    [tech.v3.dataset :as ds]
    [tech.v3.dataset.reductions :as ds-reduce]
    [tech.v3.dataset.utils :as utils]
    [tech.v3.libs.arrow :as arrow]
    [tech.v3.libs.parquet :as parquet]
    [primitive-math :as pmath]
    [clojure.tools.logging :as log])
  (:import [java.util Map HashMap Set HashSet Map$Entry List
            ArrayList]
           [java.util.concurrent ConcurrentHashMap]
           [java.util.function Function Consumer BiConsumer]
           [java.util.stream Stream]
           [tech.v3.datatype PrimitiveList IndexReduction])
  (:gen-class))


(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)


(utils/set-slf4j-log-level :info)


(def files (->> (io/file "/data/performance-benchmark-data")
                         file-seq
                         (filter #(.endsWith ^String (.toString ^Object %) "parquet"))
                         (map #(.toString ^Object %))))

;;This is a thing that geni gets you - spark dataframe systems detect the dependencies
;;and don't load data they do not need to load.
(def required-columns ["member-id" "price" "quantity" "date" "brand-id" "style-id"])


;;Because we are only loading the columns we need, it is easy to load the entire dataset
;;into memory.
(defn load-combined-parquet
  []
  (->> files
       (pmap #(ds/->dataset % {:column-whitelist required-columns}))))


;;Writing the dataset out to a store where we can access everything even if it is
;;larger than available memory is also possible.
(defn write-combined-arrow!
  []
  (arrow/write-dataset-seq-to-stream! (load-combined-parquet)
                                      "../benchmark-data.arrow"))


;;Loading this entire dataset then takes milliseconds regardless of size.
(defn load-combined-arrow
  []
  (arrow/stream->dataset-seq-inplace "../benchmark-data.arrow"))


(defn dataset-seq->group-by-aggregate
  [ds-seq]
  (-> (->> (map #(assoc % "sales" (dfn/* (% "price") (% "quantity"))) ds-seq)
           (ds-reduce/group-by-column-agg
            "member-id"
            {:total-spend (ds-reduce/sum "sales")
             :avg-basket-size (ds-reduce/mean "sales")
             :avg-price (ds-reduce/mean "price")
             :n-transactions (ds-reduce/row-count)
             :n-visits (ds-reduce/count-distinct "date" :int32)
             :n-brands (ds-reduce/count-distinct "brand-id" :int32)
             :n-styles (ds-reduce/count-distinct "style-id" :int32)}))
      (ds/write! "output.nippy")))


(comment

  (time (def ignored (dataset-seq->group-by-aggregate
                      (load-combined-parquet))))

  (time (def ignored (dataset-seq->group-by-aggregate
                      (take 1 (load-combined-parquet)))))

  (time (def ignored (dataset-seq->group-by-aggregate
                      (load-combined-arrow))))

  ;;32

  (time (def ignored (dataset-seq->group-by-aggregate
                      (take 1 (load-combined-arrow)))))

  )
