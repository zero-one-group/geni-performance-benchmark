(ns dataset.optimised-by-chris
  (:require
    [tech.io :as io]
    [tech.v2.datatype.functional :as dfn]
    [tech.v2.datatype :as dtype]
    [tech.ml.dataset :as ds]
    [tech.ml.dataset.reductions :as ds-reduce]
    [tech.libs.arrow :as arrow]
    [primitive-math :as pmath]
    [tech.ml.dataset.utils :as utils])
  (:import [java.util List]
           [java.util.function Function])
  (:gen-class))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(utils/set-slf4j-log-level :info)

(defn reduction->entry
  [member-id data]
  ;;manual destructuring saves a bit of time.
  (let [summations (:summations data)
        unique (:unique data)
        n-elems (long (:n-elems summations))
        sums (:sums summations)
        sales (double (get sums "sales"))
        price (double (get sums "price"))
        date (get unique "date")
        brand-id (get unique "brand-id")
        style-id (get unique "style-id")]
    {:member-id member-id
     :total-spend sales
     :avg-basket-size (pmath// sales (double n-elems))
     :avg-price (pmath// price (double n-elems))
     :n-transactions n-elems
     :n-visits (count date)
     :n-brands (count brand-id)
     :n-styles (count style-id)}))


(defn process-dataset-seq
  [ds-seq]
  (let [data (java.util.HashMap.)]
    (as-> ds-seq ds-seq
      ;;Add the sales column to all datasets
      (map #(assoc % "sales"
                   (dfn/* (% "price")
                          (% "quantity"))) ds-seq)
      ;;aggregate-group-by returns a java Stream because that is the only
      ;;way to parallelize items coming out of a java.util.Map.
      (ds-reduce/aggregate-group-by-column-reduce
       "member-id"
       (ds-reduce/aggregate-reducer
        {:summations (ds-reduce/dsum-reducer ["sales" "price"])
         :unique (ds-reduce/unique-reducer ["date" "brand-id" "style-id"])})
       ds-seq)
      ;;So annoyingly, we switch to stream maps
      (.map ds-seq (reify Function
                     (apply [this [k v]]
                       (reduction->entry k v))))
      ;;And then build a sink that builds a map of colname->data
      (.forEach ds-seq (reify java.util.function.Consumer
                         (accept [this item]
                           (locking data
                             (if (.isEmpty data)
                               (doseq [[k v] item]
                                 (.put data k (dtype/make-container
                                               :list
                                               (dtype/get-datatype v)
                                               [v])))
                               (doseq [[k v] item]
                                 (.add ^List (.get data k) v))))))))

    ;;Maps of colname->data convert in constant time to a dataset.
    (->> (ds/->dataset (into {} data))
         (io/put-nippy! "dataset-matrix.nippy")))
  :ok)


(def files (->> (io/file "/data/performance-benchmark-data")
                file-seq
                (filter #(.endsWith ^String (.toString ^Object %) "parquet"))
                (map #(.toString ^Object %))))


(comment

  ;;Have to write the arrow.  We write all parquest files into one arrow file to side
  ;;step the fact that current parquest support is lacking
  (def written-seq (->> files
                        (map (fn [fname]
                               (println (format "loading file %s" fname))
                               (ds/->dataset fname)))
                        (#(arrow/write-dataset-seq-to-stream! % "bigtest.arrow"))))


  ;;reading large files in-place is more or less free and instant when mmaping.
  ;;This can help if you want to find particular items in the big arrow file
  ;;but do not want to process everything.
  (time (->> (arrow/stream->dataset-seq-inplace "bigtest.arrow")
             (take 1)
             (process-dataset-seq)))
  ;; Elapsed time: 9326.420899 msecs

  (def df-one (io/get-nippy "dataset-matrix.nippy"))
  (println df-one)
  (println (ds/column-names df-one))
  (println (ds/row-count df-one))


  ;;After that we do a large group-wise reduction to an in-memory datastructure
  ;;and copy the result into java arrays that t.m.d can sit on without any
  ;;further copying.
  (time (->> (arrow/stream->dataset-seq-inplace "bigtest.arrow")
             (process-dataset-seq)))
  ;; Elapsed time: 36358.34466 msecs


  (def df-all (io/get-nippy "dataset-matrix.nippy"))
  (println df-all)
  (println (ds/column-names df-all))
  (println (ds/row-count df-all))

  true)
