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

                 ;; If specified, binds value to clojure.test/*stack-trace-depth*
                 :stack-trace-depth nil

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
                 :watch-dirs ["src" "test"]

                 ;; If given, only refresh code in the given
                 ;; directories. By default every directory on the
                 ;; classpath is refreshed. Value is passed through to clojure.tools.namespace.repl/set-refresh-dirs
                 ;; https://github.com/clojure/tools.namespace/blob/f3f5b29689c2bda53b4977cf97f5588f82c9bd00/src/main/clojure/clojure/tools/namespace/repl.clj#L164
                 :refresh-dirs ["src" "test"]


                 ;; Use this flag to specify your own flag to add to
                 ;; cause test-refresh to focus. Intended to be used
                 ;; to let you specify a shorter flag than the default
                 ;; :test-refresh/focus.
                 :focus-flag :test-refresh/focus

                 ;; When set to true, don't actually run tests. Just reload changes.
                 ;; See https://github.com/jakemcc/test-refresh/pull/91
                 :debug false

                 ;; Specify to override the default banner that prints with ever new reload.
                 ;; See https://github.com/jakemcc/test-refresh/pull/91
                 :banner "🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥"

                 ;; Change to true to clear the console between reloads
                 ;; See https://github.com/jakemcc/test-refresh/pull/91 for an example
                 :clear false})
