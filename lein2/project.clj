(defproject lein2 "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/test.check "0.7.0"]]
  :profiles {:dev {:dependencies [;[circleci/circleci.test "0.3.1"]
                                  ]}}
  ;; Makes sure you've commented out whatever version of
  ;; lein-test-refresh you are using from profiles.clj while working
  ;; on lein-test-refresh
  :plugins [[com.jakemccrary/lein-test-refresh #=(eval (nth (read-string (slurp "../test-refresh/project.clj")) 2))]]
  :test-selectors {:integration :integration
                   :ns-metadata :ns-metadata
                   :unit (complement :integration)}
  :test-refresh {:notify-command ["terminal-notifier" "-title" "Tests" "-message"]
                 ;; :notify-command ["say" "-v" "Agnes"]
                 ;; :notify-on-success false
                 ;; :stack-trace-depth nil
                 ;; :quiet true
                 :focus-flag :f
                 :changes-only true
                 ;; :watch-dirs ["src" "test"]
                 :refresh-dirs ["src" "test"]
;;                 :report lein2.sample-report/my-report
                 })
