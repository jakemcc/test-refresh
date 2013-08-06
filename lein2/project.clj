(defproject lein2 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[lein-autoexpect ~(nth (read-string (slurp "../autoexpect/project.clj")) 2)]]
  :profiles {:dev {:dependencies [[expectations "1.4.52"]]}})
