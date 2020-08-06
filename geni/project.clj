(defproject geni "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [zero.one/geni "0.0.21"]
                 ;; REPL
                 [nrepl "0.7.0"]
                 [reply "0.4.4"]
                 ;; Spark
                 [org.apache.spark/spark-core_2.12 "3.0.0"]
                 [org.apache.spark/spark-hive_2.12 "3.0.0"]
                 [org.apache.spark/spark-mllib_2.12 "3.0.0"]
                 [org.apache.spark/spark-sql_2.12 "3.0.0"]
                 [org.apache.spark/spark-streaming_2.12 "3.0.0"]
                 [org.apache.spark/spark-yarn_2.12 "3.0.0"]
                 [com.github.fommil.netlib/all "1.1.2" :extension "pom"]
                 ;; Databases
                 [mysql/mysql-connector-java "8.0.21"]
                 [org.postgresql/postgresql "42.2.14"]
                 [org.xerial/sqlite-jdbc "3.32.3.1"]]
  :jvm-opts ["-Xmx16g"]
  :profiles {:uberjar {:aot :all}}
  :main ^:skip-aot geni.core
  :target-path "target/%s")
