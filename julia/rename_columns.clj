(require '[clojure.java.io :as io])
(require '[zero-one.geni.core :as g])

(defn rename-df [index path]
  (let [df (g/read-parquet! path)]
    (println (g/count df) index)
    (-> df
        (g/rename-columns {:member-id :member_id
                           :style-id :style_id
                           :trx-id :trx_id
                           :brand-id :brand_id})
        (g/coalesce 1)
        (g/write-parquet! (str index ".parquet")))))

(->> (io/file "/data/performance-benchmark-data")
     file-seq
     (map #(.toString %))
     (filter #(.endsWith % ".snappy.parquet"))
     (mapv rename-df (range)))
