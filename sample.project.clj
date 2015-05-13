(defproject sample "1.2.3"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.9.0"]]}}

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
                 :quiet true})
