(ns geni.coalesce
  (:require
    [zero-one.geni.core :as g]))

(comment

  (time
    (-> (g/read-parquet! "/data/performance-benchmark-data")
        (g/coalesce 1)
        (g/write-parquet! "coalesced.parquet" {:mode "overwrite"})))

  true)

; Elapsed time: 7684.372175 msecs => 7.7 secs
; Elapsed time: 39247.311383 msecs => 39 secs
