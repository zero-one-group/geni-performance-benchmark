(ns dataset.optimised
  (:require
    [tech.v3.io :as io]
    [tech.v3.dataset :as ds]
    [tech.v3.dataset.column :as ds-col]
    [tech.v3.datatype :as dtype]
    [tech.v3.datatype.functional :as dfn]
    ;;parquet support
    [tech.v3.libs.parquet]
    [tech.v3.libs.arrow :as arrow]))


(def required-columns ["member-id" "price" "quantity" "date" "brand-id" "style-id"])


(def files (->> (io/file "/data/performance-benchmark-data")
                         file-seq
                         (filter #(.endsWith ^String (.toString ^Object %) "parquet"))
                         (map #(.toString ^Object %))))


(defn load-combined-parquet
  []
  (->> files
       (pmap #(ds/->dataset % {:column-whitelist required-columns}))))


(defn write-parquet-onefile!
  []
  (ds/write! (apply ds/concat-copying (load-combined-parquet))
             "../combined.parquet"))


(defn process-dataset
  [src-ds]
  (let [src-ds (assoc src-ds "sales" (dfn/* (src-ds "price")
                                            (src-ds "quantity")))]
    (as-> (ds/group-by-column->indexes src-ds "member-id") ds
      (pmap (fn [[k idx-list]]
              (let [sales (ds-col/select (src-ds "sales") idx-list)]
                {:member-id       k
                 :total-spend     (dfn/sum sales)
                 :avg-basket-size (dfn/mean sales)
                 :avg-price       (dfn/mean (ds-col/select (src-ds "price") idx-list))
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
    (process-dataset (ds/->dataset "../combined.parquet" )))

  (let [final (io/get-nippy "final.nippy")]
    (println final)
    (println (count (ds/column-names final)))
    (ds/row-count final))

  ; 1 Part
  ; Elapsed time: 17993.008065 msecs

  ; 12 Parts
  ; Elapsed time: 133388.686526 msecs

  true)
