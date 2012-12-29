(defproject lein17 "1.0.0-SNAPSHOT"
  :description "dummy project for testing autoexpect with older lein"
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :dev-dependencies [[expectations "1.4.10"]]
  :plugins [[lein-autoexpect ~(nth (read-string (slurp "../autoexpect/project.clj")) 2)]])