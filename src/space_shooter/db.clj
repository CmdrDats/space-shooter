
(ns space-shooter.db
  (:use [datomic-schema.schema :only [defpart defschema fields]])
  (:require [datomic.api :as d])
  (:require [datomic-schema.schema :as s]))

(defonce db-url "datomic:mem://testdb")

(defn db [] (d/db (d/connect db-url)))

(defn tx [t] (d/transact (d/connect db-url) t))

(def e (comp d/touch #(d/entity (db) %) first))

(def tx-functions
  [{:db/id #db/id [:db.part/user]
    :db/ident :inc
    :db/doc "Data function that increments value of attribute a by amount."
    :db/fn #db/fn
    {:lang "clojure"
     :params [db e a amount]
     :code [[:db/add e a
             (-> (d/entity db e) a (+ amount))]]}}])

(defschema entity
  (fields
   [uuid :uuid]
   [type :keyword "The type of game entity"]))

(defschema thing
  (fields
   [owner :ref]
   [posx :double]
   [posy :double]
   [velx :double]
   [vely :double]
   [width :double]
   [height :double]
   [health :long]))

(defschema player
  (fields
   [name :string]
   [score :long]))

(defn setup-db [& args]
  (d/create-database db-url)
  (tx (concat tx-functions (s/build-schema d/tempid))))

(defn new-ent [type e]
  (assoc e
    :db/id (d/tempid :db.part/user)
    :entity/uuid (d/squuid)
    :entity/type type))

(defn spawn [type health owner [posx posy] [width height] [velx vely]]
  (let [e {:thing/posx posx :thing/posy posy
           :thing/velx velx :thing/vely vely
           :thing/width width :thing/height height
           :thing/health health}
        e (if owner (assoc e :thing/owner owner) e)]
    (new-ent type e)))
