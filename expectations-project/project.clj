(defproject expectations-project "0.1.0-SNAPSHOT"
  :description "Sample project using expectations"
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :plugins [[com.jakemccrary/lein-test-refresh  ~(nth (read-string (slurp "../test-refresh/project.clj")) 2)]]
  :profiles {:dev {:dependencies [[expectations "2.2.0-beta1"]]}})
