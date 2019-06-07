(defproject com.nedap.staffing-solutions/utils.spec "0.8.2"
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

  :dependencies [[com.nedap.staffing-solutions/utils.test "1.3.0"]
                 [expound "0.7.2"]
                 [org.clojure/clojure "1.10.1-beta2"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [spec-coerce "1.0.0-alpha9"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  ;; Please don't add `:hooks [leiningen.cljsbuild]`. It can silently skip running the JS suite on `lein test`.
  ;; It also interferes with Cloverage.

  :cljsbuild {:builds        {"test" {:source-paths ["src" "test"]
                                      :compiler     {:main          nedap.utils.spec.test-runner
                                                     :output-to     "target/out/tests.js"
                                                     :output-dir    "target/out"
                                                     :target        :nodejs
                                                     :optimizations :none}}}
              :test-commands {"test" ["node" "target/out/tests.js"]}}

  :profiles {:dev      {:plugins [[lein-cloverage "1.0.13"]]}

             :provided {:dependencies [[org.clojure/clojurescript "1.10.520"]]}})
