(defproject com.nedap.staffing-solutions/utils.spec "0.4.2"
  :description "clojure.spec utilities"
  :url "https://github.com/nedap/utils.spec"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"releases" {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                             :username :env/artifactory_user
                             :password :env/artifactory_pass}}
  :deploy-repositories [["releases" {:url           "https://nedap.jfrog.io/nedap/staffing-solutions/"
                                     :sign-releases false}]]
  :repository-auth {#"https://nedap.jfrog\.io/nedap/staffing-solutions/"
                    {:username :env/artifactory_user
                     :password :env/artifactory_pass}}
  :dependencies [[expound "0.7.2"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [spec-coerce "1.0.0-alpha9"]])
