(ns geni.script
  (:require
    [zero-one.geni.core :as g]))

(comment

  (time
    (-> (g/read-parquet! "/data/performance-benchmark-data")
    ;(-> (g/read-parquet! "/data/performance-benchmark-data/part-00000-0cf99dad-6d07-4025-a5e9-f425bb9532b9-c000.snappy.parquet")
       (g/with-column :sales (g/* :price :quantity))
       (g/group-by :member-id)
       (g/agg {:total-spend     (g/sum :sales)
               :avg-basket-size (g/mean :sales)
               :avg-price       (g/mean :price)
               :n-transactions  (g/count "*")
               :n-visits        (g/count-distinct :date)
               :n-brands        (g/count-distinct :brand-id)
               :n-styles        (g/count-distinct :style-id)})
       (g/write-parquet! "target/geni-matrix.parquet" {:mode "overwrite"})))

  (g/shape (g/read-parquet! "target/geni-matrix.parquet"))

  true)

; Elapsed time: 7684.372175 msecs => 7.7 secs
; Elapsed time: 39247.311383 msecs => 39 secs
