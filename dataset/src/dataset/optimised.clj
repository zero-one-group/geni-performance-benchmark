(ns dataset.optimised
  (:require
    [clojure.java.io]
    [primitive-math :as pmath]
    [tech.io :as io]
    [tech.ml.dataset :as ds]
    [tech.ml.dataset.column :as ds-col]
    [tech.parallel.for :as parallel-for]
    [tech.v2.datatype :as dtype]
    [tech.v2.datatype.functional :as dfn]
    [tech.v2.datatype.typecast :as typecast]))

(defn dsum
  ^double [rdr]
  (let [drdr (typecast/datatype->reader :float64 rdr)
        n-elems (dtype/ecount drdr)]
    (parallel-for/indexed-map-reduce
     n-elems
     (fn [^long start-idx ^long group-len]
       (let [end-idx (long (+ start-idx group-len))]
         (loop [idx (long start-idx)
                sum 0.0]
           (if (< idx end-idx)
             (recur (unchecked-inc idx)
                    (pmath/+ sum (.read drdr idx)))
             sum))))
     (partial reduce +))))

(defn dmean
  ^double [rdr]
  (pmath// (dsum rdr)
           (double (dtype/ecount rdr))))

(defn process-dataset
  [src-ds]
  (let [src-ds (assoc src-ds "sales" (dtype/clone (dfn/* (src-ds "price")
                                                         (src-ds "quantity"))))]
    (as-> (ds/group-by-column->indexes "member-id" src-ds) ds
      (pmap (fn [[k idx-list]]
              (let [sales (ds-col/select (src-ds "sales") idx-list)]
                {:member-id       k
                 :total-spend     (dsum sales)
                 :avg-basket-size (dmean sales)
                 :avg-price       (dmean (ds-col/select (src-ds "price") idx-list))
                 :n-transactions  (dtype/ecount idx-list)
                 :n-visits        (count (ds-col/unique (ds-col/select
                                                         (src-ds "date")
                                                         idx-list)))
                 :n-brands        (count (ds-col/unique (ds-col/select
                                                         (src-ds "brand-id")
                                                         idx-list)))
                 :n-styles        (count (ds-col/unique (ds-col/select
                                                         (src-ds "style-id")
                                                         idx-list)))}))
            ds)
      (ds/->>dataset ds)
      (io/put-nippy! "final.nippy" ds)
      :data-written)))

(comment

  (time
    (process-dataset (ds/->dataset "/data/performance-benchmark-data/part-00000-0cf99dad-6d07-4025-a5e9-f425bb9532b9-c000.snappy.parquet")))

  (time
    (process-dataset (ds/->dataset "all-transactions.parquet")))

  (let [final (io/get-nippy "final.nippy")]
    (println final)
    (println (count (ds/column-names final)))
    (ds/row-count final))

  ; 1 Part
  ; Elapsed time: 17993.008065 msecs

  ; 12 Parts
  ; Elapsed time: 133388.686526 msecs

  true)
