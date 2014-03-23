(defproject lein2 "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]]
  :plugins [[com.jakemccrary/lein-test-refresh ~(nth (read-string (slurp "../test-refresh/project.clj")) 2)]]
  :test-selectors {:default (constantly nil)
                   :integration :integration
                   :unit (complement :integration)
                   :fast (complement :slow)}
  :test-refresh {:notify-command ["say" "-v" "Whisper"]
                 :notify-on-success false})
