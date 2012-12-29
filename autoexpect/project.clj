(defproject lein-autoexpect "0.2.4-SNAPSHOT"
  :description "Automatically run expecations when a source file changes"
  :url "https://github.com/jakemcc/lein-autoexpect"
  :developer "Jake McCrary"
  :dependencies [[org.clojure/tools.namespace "0.2.2" :exclusions [org.clojure/clojure]]
                 [leinjacker "0.4.1" :exclusions [org.clojure/clojure]]
                 [jakemcc/clojure-gntp "0.1.1" :exclusions [org.clojure/clojure]]]
  :profiles {:dev {:dependencies [[expectations "1.4.10"]]}}
  :lein-release {:scm :git
                 :deploy-via :clojars})
