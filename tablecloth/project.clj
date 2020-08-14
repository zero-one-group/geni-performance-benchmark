(defproject tablecloth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [scicloj/tablecloth "1.0.0-pre-alpha9"]
                 [org.apache.parquet/parquet-hadoop "1.10.1"
                  :exclusions [commons-codec]]
                 [org.apache.hadoop/hadoop-common
                  "3.1.1"
                  ;;We use logback-classic.
                  :exclusions [org.slf4j/slf4j-log4j12
                               log4j
                               com.google.guava/guava
                               commons-codec
                               commons-logging
                               com.google.code.findbugs/jsr305
                               com.fasterxml.jackson.core/jackson-databind
                               org.apache.commons/commons-math3]]]
  :jvm-opts ["-Xmx16g"]
  :main ^:skip-aot tablecloth.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
