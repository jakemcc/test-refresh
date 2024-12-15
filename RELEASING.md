1. Update version number in `version.edn`
1. Git commit, tag, push (git tag -a 0.26.0 -m "0.26.0 release")
2. `cd test-refresh && lein deploy clojars && cd ..` (look up clojars auth token for clojars step, use as password)
3. `cd lein-test-refresh && lein deploy clojars && cd ..`
4. Update version number in `version.edn` and commit
