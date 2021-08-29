(defproject com.jakemccrary/lein-test-refresh #=(eval (read-string (slurp "../version.edn")))
  :description "Leiningen plugin for automatically reload code and run clojure.test tests when files change"
  :url "https://github.com/jakemcc/lein-test-refresh"
  :developer "Jake McCrary"
  :min-lein-version "2.4"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.jakemccrary/test-refresh #=(eval (read-string (slurp "../version.edn")))]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :gpg :password :gpg}]
                        ["releases" {:url "https://clojars.org/repo"
                                     :username :gpg :password :gpg}]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :scm {:name "git"
        :url "git@github.com:jakemcc/lein-test-refresh.git"})
