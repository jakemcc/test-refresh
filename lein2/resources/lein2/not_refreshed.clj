(ns lein2.not-refreshed)

(throw (Exception. "Code was loaded and shouldn't have been"))
