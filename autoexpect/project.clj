(defproject lein-autoexpect "0.2.1-SNAPSHOT"
  :description "Automatically run expecations when a source file changes"
  :url "https://github.com/jakemcc/lein-autoexpect"
  :developer "Jake McCrary"
  :dependencies [[org.clojure/tools.namespace "0.2.1"]
                 [jakemcc/clojure-gntp "0.1.1" :exclusions [org.clojure/clojure]]]
  :plugins [[lein-autoexpect "0.2.1-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[expectations "1.4.10"]]}}
  :lein-release {:scm :git
                 :deploy-via :clojars})
