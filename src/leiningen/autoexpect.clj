(ns leiningen.autoexpect)

(defn eval-in-project
  "Support eval-in-project in both Leiningen 1.x and 2.x."
  [project form init]
  (let [[eip two?] (or (try (require 'leiningen.core.eval)
                            [(resolve 'leiningen.core.eval/eval-in-project)
                             true]
                            (catch java.io.FileNotFoundException _))
                       (try (require 'leiningen.compile)
                            [(resolve 'leiningen.compile/eval-in-project)]
                            (catch java.io.FileNotFoundException _)))]
    (if two?
      (eip project form init)
      (eip project form nil nil init))))

(def dep ['org.clojure/tools.namespace "0.2.0"])

(defn- add-fresh-dep [project]
  (if-let [conj-dependency (resolve 'leiningen.core.project/conj-dependency)]
    (conj-dependency project dep)
    (update-in project [:dependencies] conj dep)))

(defn autoexpect
  "Autoruns expecations on source change"
  [project & args]
  (eval-in-project
   (add-fresh-dep project)
   `(do
      (reset! expectations/run-tests-on-shutdown false)
      (let [top-stars#  (apply str (repeat 45 "*"))
            side-stars# (apply str (repeat 15 "*"))]
        (loop [tracker# (clojure.tools.namespace.track/tracker)]
          (let [new-tracker# (clojure.tools.namespace.dir/scan tracker#)]
            (try
              (when (not= new-tracker# tracker#)
                (let [result# (binding [*out* (java.io.StringWriter.)]
                                (clojure.tools.namespace.repl/refresh))]
                  (if (= :ok result#)
                    (do
                      (println top-stars#)
                      (println side-stars# "Running tests" side-stars#)
                      (expectations/run-all-tests))
                    (println "Error refreshing environment:" clojure.core/*e))))
              (Thread/sleep 500)
              (catch Exception ex# (.printStackTrace ex#)))
            (recur new-tracker#)))))
   `(require '(clojure.tools.namespace track repl)
             'expectations)))
