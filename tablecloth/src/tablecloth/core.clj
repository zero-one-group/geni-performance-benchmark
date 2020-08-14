(ns tablecloth.core
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
                          :n-visits        #(count (distinct (% "date")))
                          :n-brands        #(count (distinct (% "brand-id")))
                          :n-styles        #(count (distinct (% "style-id")))}
                         {:parallel? true})
          (api/write-nippy! "target/dataset-matrix.nippy.gz"))))

  ; 1 Part
  ; Elapsed time: 47566.025084 msecs

  ; 12 Parts
  ; Elapsed time: 152630.228187 msecs

  (def result (api/read-nippy "target/dataset-matrix.nippy.gz"))
  (println result)
  (api/shape result)

  true)
