(defproject sample "1.2.3"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.12.0"]]}}

  :test-refresh {;; Specifies a command to run on test
                 ;; failure/success. Short message is passed as the
                 ;; last argument to the command.
                 ;; Defaults to no command.
                 :notify-command ["terminal-notifier" "-title" "Tests" "-message"]

                 ;; set to true to send notifications to growl
                 ;; Defaults to false.
                 :growl false

                 ;; only growl and use the notify command if there are
                 ;; failures.
                 ;; Defaults to true.
                 :notify-on-success false

                 ;; Stop clojure.test from printing
                 ;; "Testing namespace.being.tested". Very useful on
                 ;; codebases with many test namespaces.
                 ;; Defaults to false.
                 :quiet true

                 ;; If this is specified then only tests in namespaces
                 ;; that were just reloaded by tools.namespace
                 ;; (namespaces where a change was detected in it or a
                 ;; dependent namespace) are run. This can also be
                 ;; passed as a command line option: lein test-refresh :changes-only.
                 :changes-only true

                 ;; specifiy a custom clojure.test report method
                 ;; Specify the namespace and multimethod that will handle reporting
                 ;; from test-refresh.  The namespace must be available to the project dependencies.
                 ;; Defaults to no custom reporter
                 :report  myreport.namespace/my-report

                 ;; If set to a truthy value, then lein test-refresh
                 ;; will only run your tests once. Also supported as a
                 ;; command line option. Reasoning for feature can be
                 ;; found in PR:
                 ;; https://github.com/jakemcc/lein-test-refresh/pull/48
                 :run-once true

                 ;; If given, watch for changes only in the given
                 ;; folders. By default, watches for changes on entire
                 ;; classpath.
                 :watch-dirs ["src" "test"]})
