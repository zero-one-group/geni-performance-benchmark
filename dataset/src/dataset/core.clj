(ns dataset.core
  (:require
    [clojure.java.io]
    [tech.v2.datatype.functional :as dfn]
    [tablecloth.api :as api])
  (:gen-class))

(comment

  (time
    (let [dataframe (->> (clojure.java.io/file "/data/performance-benchmark-data")
                         file-seq
                         (filter #(.endsWith (.toString %) "parquet"))
                         (map #(.toString %))
                         ;(take 1)
                         (map api/dataset)
                         (apply api/concat))]
      (-> dataframe
          (api/add-or-replace-column "sales" #(dfn/* (% "price") (% "quantity")))
          (api/group-by "member-id")
          (api/aggregate {:total-spend     #(dfn/sum (% "sales"))
                          :avg-basket-size #(dfn/mean (% "sales"))
                          :avg-price       #(dfn/mean (% "price"))
                          :n-transactions  api/row-count
                          :n-visits        #(-> % (api/unique-by "date") api/row-count)
                          :n-brands        #(-> % (api/unique-by "brand-id") api/row-count)
                          :n-styles        #(-> % (api/unique-by "style-id") api/row-count)})
          (api/write-nippy! "target/dataset-matrix.nippy.gz"))))

  (api/shape (api/read-nippy "target/dataset-matrix.nippy.gz"))

  true)

; 1 Part
; Elapsed time: 219932.091967 msecs => 220 secs

; 12 Parts
; Elapsed time: 726119.116533 msecs => 726 secs
