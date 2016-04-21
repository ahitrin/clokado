(defproject clokado "0.3.8"
  :description "Simple Clojure library for Mikado Method"
  :url "https://github.com/ahitrin/clokado"
  :scm {:name "git"
        :url "https://github.com/ahitrin/clokado"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.taoensso/nippy "2.9.0"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
