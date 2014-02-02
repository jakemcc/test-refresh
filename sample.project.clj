(defproject sample "1.2.3"
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.3.4"]]}}
  :test-refresh {:notify-command ["say"]})
