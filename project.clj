(defproject com.nedap.staffing-solutions/utils.spec "0.6.3"
  :description "clojure.spec utilities"

  :url "https://github.com/nedap/utils.spec"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :signing {:gpg-key "servicedesk-PEP@nedap.com"}

  :repositories {"releases" {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                             :username :env/artifactory_user
                             :password :env/artifactory_pass}}

  :deploy-repositories [["releases" {:url "https://nedap.jfrog.io/nedap/staffing-solutions/"}]]

  :repository-auth {#"https://nedap.jfrog\.io/nedap/staffing-solutions/"
                    {:username :env/artifactory_user
                     :password :env/artifactory_pass}}

  :dependencies [[expound "0.7.2"]
                 [org.clojure/clojure "1.10.1-beta2"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [spec-coerce "1.0.0-alpha9"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :hooks [leiningen.cljsbuild]

  :cljsbuild {:builds
              {"dev" {:source-paths ["src" "test"]
                      :compiler     {:main          nedap.utils.spec.test-runner
                                     :output-to     "target/out/tests.js"
                                     :output-dir    "target/out"
                                     :target        :nodejs
                                     :optimizations :none}}}
              :test-commands
              {"dev" ["node" "target/out/tests.js"]}}

  :profiles {:dev      {:plugins [[lein-cloverage "1.0.13"]]}

             :provided {:dependencies [[org.clojure/clojurescript "1.10.520"]]}})
