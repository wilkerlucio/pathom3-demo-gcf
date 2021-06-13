(ns com.wsscode.pathom-server
  (:require
    ; to include the env setup from the Tutorial demo
    [com.wsscode.pathom3.demos.ip-weather :refer [env]]

    [cognitect.transit :as t]
    [com.wsscode.pathom3.connect.operation.transit :as pcot]
    [com.wsscode.pathom3.interface.eql :as p.eql]
    [muuntaja.core :as muuntaja]
    [muuntaja.middleware :as middleware]
    [ring.middleware.cors :refer [wrap-cors]]))

; create a boundary interface
(def interface (p.eql/boundary-interface env))

(defn handler [{:keys [body-params]}]
  {:status 200
   :body   (interface body-params)})

(def muuntaja-options
  (update-in
    muuntaja/default-options
    [:formats "application/transit+json"]
    ; in this part we setup the read and write handlers for Pathom resolvers and mutations
    merge {:decoder-opts {:handlers pcot/read-handlers}
           :encoder-opts {:handlers  pcot/write-handlers
                          ; write-meta is required if you wanna see execution stats on Pathom Viz
                          :transform t/write-meta}}))

(def app
  (-> handler
      (middleware/wrap-format muuntaja-options)
      (wrap-cors
        :access-control-allow-origin [#".+"]
        :access-control-allow-methods [:get :put :post :delete])))
