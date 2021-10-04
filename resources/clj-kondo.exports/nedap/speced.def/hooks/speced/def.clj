(ns hooks.speced.def
  (:require
   [clj-kondo.hooks-api :as api]))

(defn defprotocol-hook [{{:keys [children] :as node} :node}]
  (let [[_macro protocol-name docstring & methods] children]
    {:node (api/list-node ;; defprotocol with renamed fn names
            (list*
             (api/token-node 'clojure.core/defprotocol)
             protocol-name
             docstring
             (concat
              methods
              (->> methods
                   (map (fn [{[fn-token & xs] :children}]
                          (api/list-node
                           (list* (api/token-node (symbol (str "--" (:string-value fn-token)))) xs))))))))}))
