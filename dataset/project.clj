(defproject dataset "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [techascent/tech.ml.dataset "5.00-alpha-20"
                  :exclusions [org.apache.commons/commons-compress]]
                 [com.climate/claypoole "1.1.4"]
                 [org.apache.parquet/parquet-hadoop "1.11.0"
                  :exclusions [commons-codec]]
                 ;;Hadoop has an insane amount of dependencies that are
                 ;;pretty far out of date.  Especially in comparison
                 ;;with the apache poi project.
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
                               org.apache.commons/commons-math3]]
                 ;;unsafe works better with graal native
                 [org.apache.arrow/arrow-memory-unsafe "2.0.0"]
                 [org.apache.arrow/arrow-memory-core "2.0.0"]
                 [org.apache.arrow/arrow-vector "2.0.0"
                  :exclusions [commons-codec]]])
