(defproject com.jakemccrary/lein-test-refresh "0.5.0-SNAPSHOT"
  :description "Automatically reload code and run clojure.test tests when files change"
  :url "https://github.com/jakemcc/lein-test-refresh"
  :developer "Jake McCrary"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4" :exclusions [org.clojure/clojure]]
                 [leinjacker "0.4.1" :exclusions [org.clojure/clojure]]
                 [jakemcc/clojure-gntp "0.1.1" :exclusions [org.clojure/clojure]]]
  :scm {:name "git"
        :url "git@github.com:jakemcc/lein-test-refresh.git"})
