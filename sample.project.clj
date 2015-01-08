(defproject sample "1.2.3"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.5.5"]]}}

  :test-refresh {
                 ;; Specifies a command to run on test
                 ;; failure/success. Short message is passed as the
                 ;; last argument to the command.
                 :notify-command ["say"]
                 :growl false
                 ;; only growl and use the notify command if there are failures
                 :notify-on-success false 
                 })
