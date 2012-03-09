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

(def dep ['fresh "1.0.2"])

(defn- add-fresh-dep [project]
  (if-let [conj-dependency (resolve 'leiningen.core.project/conj-dependency)]
    (conj-dependency project dep)
    (update-in project [:dependencies] conj dep)))

(defn autoexpect
  "Autoruns expecations on source change"
  [project & args]
  (let [test-path (get project :test-path "test")
        source-path (get project :source-path "src")]
    (eval-in-project
     (add-fresh-dep project)
     `(do
        (reset! expectations/run-tests-on-shutdown false)
        (let [top-stars#  (apply str (repeat 45 "*"))
              side-stars# (apply str (repeat 15 "*"))
              check# (fresh.core/freshener
                      #(fresh.core/clj-files-in
                        (clojure.java.io/file ~test-path)
                        (clojure.java.io/file ~source-path)))]
          (loop [_# nil]
            (try
              (let [report# (check#)]
                (when-let [reloaded# (seq (:reloaded report#))]
                  (println top-stars#)
                  (println side-stars# "Running tests" side-stars#)
                  (expectations/run-all-tests))
                (Thread/sleep 500))
              (catch Exception ex# (.printStackTrace ex#)))
            (recur nil))))
     `(require 'fresh.core
               'expectations
               'clojure.java.io))))