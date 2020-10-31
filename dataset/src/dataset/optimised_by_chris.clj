(ns dataset.core
  (:require
    [tech.v3.io :as io]
    [tech.v3.datatype.functional :as dfn]
    [tech.v3.datatype :as dtype]
    [tech.v3.datatype.casting :as casting]
    [tech.v3.datatype.datetime :as dtype-dt]
    [tech.v3.dataset :as ds]
    [tech.v3.dataset.reductions :as ds-reduce]
    [tech.v3.dataset.column :as ds-col]
    [tech.v3.libs.arrow :as arrow]
    [tech.v3.libs.parquet :as parquet]
    [tech.v3.parallel.for :as parallel-for]
    [primitive-math :as pmath]
    [clojure.tools.logging :as log]
    [tech.v3.dataset.utils :as utils])
  (:import [java.util Map HashMap Set HashSet Map$Entry List]
           [java.util.function Function Consumer]
           [java.util.stream Stream]
           [tech.v3.datatype PrimitiveList])
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


;;Tried a record..this had no effect...
(defrecord Result [^long member-id
                   ^double total-spend
                   ^double avg-basket-size
                   ^double avg-price
                   ^long n-transactions
                   ^long n-visits
                   ^long n-brands
                   ^long n-styles])


(defn dataset-seq->group-by-aggregate
  [ds-seq]
  (let [columns (HashMap.)
        _ (log/info "group-by")
        ^Stream agg-stream
        (as-> ds-seq ds-seq
          ;;Add the sales column to all datasets
          (map #(assoc % "sales"
                       (dfn/* (% "price")
                              (% "quantity"))) ds-seq)
          ;;aggregate-group-by returns a java Stream because that is the only
          ;;way to parallelize items coming out of a java.util.Map.
          (ds-reduce/group-by-column-aggregate
           "member-id"
           (ds-reduce/aggregate-reducer
            {:summations (ds-reduce/double-reducer ["sales" "price"]
                                                   ds-reduce/sum-consumer)
             ;;Since date, brand-id, and style ID are all int32 quantities, it is safe
             ;;to use a bitmap reducer
             :unique (ds-reduce/long-reducer ["date" "brand-id" "style-id"]
                                             ds-reduce/bitmap-consumer)})
           ;;This results in a parallel java stream over the entry-set.
           ds-seq))
        ;;Partial reduction
        _ (log/info "Aggregate to array of records")
        ary-data
        (-> agg-stream
            (.map
             (reify Function
               (apply [this data]
                 (let [member-id (first data)
                       reduce-data (second data)
                       summations (get reduce-data :summations)
                       ;;Summations get saved to map with :n-elems and :value
                       sales (get summations "sales")
                       price (get summations "price")
                       ;;bitmaps are just bitmaps
                       bitmaps (get reduce-data :unique)
                       date (get bitmaps "date")
                       brand-id (get bitmaps "brand-id")
                       style-id (get bitmaps "style-id")
                       n-sales (double (:value sales))
                       n-price (double (:value price))
                       n-elems (long (:n-elems sales))]
                   {:member-id member-id
                    :total-spend n-sales
                    :avg-basket-size (pmath// n-sales (double n-elems))
                    :avg-price (pmath// n-price (double n-elems))
                    :n-transactions n-elems
                    :n-visits (dtype/ecount date)      ;;roaring bitmap
                    :n-brands (dtype/ecount brand-id)  ;;roaring bitmap
                    :n-styles (dtype/ecount style-id)
                    }))))
            ;;Collect to an array of results
            (.toArray))
        ;;Transpose result
        _ (log/info "Typed Transpose results")
        n-results (alength ary-data)
        first-record (first ary-data)
        colmap (->> first-record
                    (map (fn [[k v]]
                           [k (case (casting/simple-operation-space (dtype/datatype v))
                                :int64 (dtype/make-reader
                                        :int64 n-results
                                        (unchecked-long (get (aget ary-data idx) k)))
                                :float64 (dtype/make-reader
                                          :float64 n-results
                                          (unchecked-double (get (aget ary-data idx) k))))]))
                    (into {}))
        final-ds (ds/->dataset colmap)]
    (log/info "Writing result")
    (ds/write! final-ds "output.nippy")
    (log/info "finished!! :-)")
    final-ds))


(comment

  (time (def ignored (dataset-seq->group-by-aggregate
                      (load-combined-parquet))))

  (time (def ignored (dataset-seq->group-by-aggregate
                      (take 1 (load-combined-parquet)))))

  (time (def ignored (dataset-seq->group-by-aggregate
                      (load-combined-arrow))))

  (time (def ignored (dataset-seq->group-by-aggregate
                      (take 1 (load-combined-arrow)))))

  )
