(defproject dataset "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [techascent/tech.ml.dataset "4.00"]
                 [scicloj/tablecloth "1.0.0-pre-alpha8"]
                 [org.apache.parquet/parquet-hadoop "1.10.1"]
                 [org.apache.hadoop/hadoop-common "3.1.1"]]
  :jvm-opts ["-Xmx16g"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
