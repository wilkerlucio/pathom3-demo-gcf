(ns com.wsscode.pathom3.demos.ip-weather
  (:require
    [cheshire.core :as json]
    [com.wsscode.pathom3.connect.indexes :as pci]
    [com.wsscode.pathom3.connect.operation :as pco]
    [com.wsscode.pathom3.connect.planner :as pcp]))

(pco/defresolver ip->lat-long
  [{:keys [ip]}]
  {::pco/output [:latitude :longitude]}
  (-> (slurp (str "https://get.geojs.io/v1/ip/geo/" ip ".json"))
      (json/parse-string keyword)
      (select-keys [:latitude :longitude])))

(pco/defresolver latlong->woeid
  [{:keys [latitude longitude]}]
  {:woeid
   (-> (slurp
         (str "https://www.metaweather.com/api/location/search/?lattlong="
           latitude "," longitude))
       (json/parse-string keyword)
       first
       :woeid)})

(pco/defresolver woeid->temperature
  [{:keys [woeid]}]
  {:temperature
   (-> (slurp (str "https://www.metaweather.com/api/location/" woeid))
       (json/parse-string keyword)
       :consolidated_weather
       first
       :the_temp)})

(defonce plan-cache* (atom {}))

(def todo-db
  {1 {:todo/id    1
      :todo/title "Write foreign docs"
      :todo/done? true}
   2 {:todo/id    2
      :todo/title "Integrate the whole internet"
      :todo/done? false}})

(pco/defresolver todo-items []
  {::pco/output
   [{:app/all-todos
     [:todo/id]}]}
  ; export only the ids to force the usage of another resolver for
  ; the details
  {:app/all-todos
   [{:todo/id 1}
    {:todo/id 2}]})

(pco/defresolver todo-by-id [{:todo/keys [id]}]
  {::pco/output
   [:todo/id
    :todo/title
    :todo/done?]}
  (get todo-db id))

(def env
  ; persistent plan cache
  (-> {::pcp/plan-cache* plan-cache*}
      (pci/register
        [ip->lat-long
         latlong->woeid
         woeid->temperature
         todo-items
         todo-by-id])))
