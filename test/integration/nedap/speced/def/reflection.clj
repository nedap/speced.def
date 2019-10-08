(ns integration.nedap.speced.def.reflection
  "Does not contain tests; its lack of emitted reflection warnings is tested through CircleCI."
  (:require
   [nedap.speced.def :as speced])
  (:import
   (clojure.lang IFn)))

(speced/defn sample1 [{::keys [^::speced/nilable ^IFn stop]}]
  (some-> stop .invoke))

(speced/defn sample2 [{::keys [^IFn stop]}]
  (some-> stop .invoke))

(speced/defn sample3 [^IFn stop]
  (some-> stop .invoke))

(defn sample4 [stop]
  (speced/let [^IFn f stop]
    (some-> f .invoke)))

(defn sample5 [stop]
  (speced/let [{::keys [^IFn f]} stop]
    (some-> f .invoke)))

(defn sample6 [stop]
  (speced/letfn [(foo [{::keys [^IFn f]}]
                   (some-> f .invoke))]
    (foo stop)))

(defn sample7 [stop]
  (speced/letfn [(foo [^IFn f]
                   (some-> f .invoke))]
    (foo stop)))
