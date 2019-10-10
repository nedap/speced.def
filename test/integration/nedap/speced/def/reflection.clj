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

(speced/defn class-inference-sample1 [^string? s]
  (-> s (.codePointBefore 42)))

(speced/defn class-inference-sample2 [^::speced/nilable ^string? s]
  (-> s (.codePointBefore 42)))

(speced/defn class-inference-sample3 [{::keys [^string? s]}]
  (-> s (.codePointBefore 42)))

(speced/defn class-inference-sample4 [{::keys [^::speced/nilable ^string? s]}]
  (-> s (.codePointBefore 42)))

(defn class-inference-sample5 [x]
  (speced/let [^string? s x]
    (-> s (.codePointBefore 42))))

(defn class-inference-sample6 [x]
  (speced/let [^::speced/nilable ^string? s x]
    (-> s (.codePointBefore 42))))

(defn class-inference-sample7 [x]
  (speced/let [{:keys [^string? s]} x]
    (-> s (.codePointBefore 42))))

(defn class-inference-sample8 [x]
  (speced/let [{:keys [^::speced/nilable ^string? s]} x]
    (-> s (.codePointBefore 42))))

(defn class-inference-sample9 [x]
  (speced/letfn [(foo [^string? s]
                   (-> s (.codePointBefore 42)))]
    (foo x)))

(defn class-inference-sample10 [x]
  (speced/letfn [(foo [^::speced/nilable ^string? s]
                   (-> s (.codePointBefore 42)))]
    (foo x)))

(defn class-inference-sample11 [x]
  (speced/letfn [(foo [{::keys [^string? s]}]
                   (-> s (.codePointBefore 42)))]
    (foo x)))

(defn class-inference-sample12 [x]
  (speced/letfn [(foo [{::keys [^::speced/nilable ^string? s]}]
                   (-> s (.codePointBefore 42)))]
    (foo x)))
