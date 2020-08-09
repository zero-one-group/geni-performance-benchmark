(ns dataset.convert-nippy
  (:require
    [clojure.java.io]
    [tech.io :as io]
    [tech.ml.dataset :as ds]))

(comment

  (def one-month-ds
    (->> "/data/performance-benchmark-data/part-00000-0cf99dad-6d07-4025-a5e9-f425bb9532b9-c000.snappy.parquet"
         (ds/->>dataset)))

  (io/put-nippy! "one-month-transactions.nippy" one-month-ds)
  (def ds (io/get-nippy "one-month-transactions.nippy"))
  (println ds)
  (ds/row-count ds)


  ;; Coalesced using Geni, then loaded as one file
  (def all-ds
    (ds/->>dataset "all-transactions.parquet"))

  (io/put-nippy! "all-transactions.nippy" all-ds)

  (def ds-all (io/get-nippy "all-transactions.nippy"))
  (first (ds-all "date"))
  (println ds-all)
  (ds/row-count ds-all)

  true)

