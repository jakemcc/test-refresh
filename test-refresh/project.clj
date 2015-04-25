(defproject com.jakemccrary/lein-test-refresh "0.9.1-SNAPSHOT"
  :description "Automatically reload code and run clojure.test tests when files change"
  :url "https://github.com/jakemcc/lein-test-refresh"
  :developer "Jake McCrary"
  :min-lein-version "2.4"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/tools.namespace "0.2.10" :exclusions [org.clojure/clojure]]
                 [leinjacker "0.4.2" :exclusions [org.clojure/clojure]]
                 [jakemcc/clojure-gntp "0.1.1" :exclusions [org.clojure/clojure]]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :gpg :password :gpg}]
                        ["releases" {:url "https://clojars.org/repo"
                                     :username :gpg :password :gpg}]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :scm {:name "git"
        :url "git@github.com:jakemcc/lein-test-refresh.git"})
