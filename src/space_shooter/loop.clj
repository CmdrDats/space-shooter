
(ns space-shooter.loop
  (:require
   [datomic.api :as d]
   [space-shooter.db :as db]))

(defn add-velocity [elapsed thing]
  [[:inc (:db/id thing) :thing/posx (* elapsed (:thing/velx thing))]
   [:inc (:db/id thing) :thing/posy (* elapsed (:thing/vely thing))]])

(defn movements [elapsed db]
  (->>
   (d/q '[:find ?e :where [?e :thing/posx]] db)
   (mapcat (comp (partial add-velocity elapsed)
                 (partial d/entity db) first))))

(defn collides [db e o pos size]
  (let [ent (d/entity db e)
        oth (d/entity db o)
        ep (pos ent) es (size ent)
        op (pos oth) os (size oth)]
    (or (and (> (+ ep es) op) (< ep (+ op os)))
        (and (> (+ op os) ep) (< op (+ ep es))))))

(defn collision-damage [[thing other]]
  [[:inc (:db/id thing) :thing/health (- (:thing/health other))]])

(defn collisions [elapsed db]
  (->>
   (d/q
    '[:find ?e ?o :where [?e :thing/posx] [?o :thing/posx]
      [(space-shooter.loop/collides $ ?e ?o :thing/posx :thing/width)]
      [(space-shooter.loop/collides $ ?e ?o :thing/posy :thing/height)]
      [(!= ?e ?o)]] db)
   (mapcat (comp collision-damage (fn [t] (map (partial d/entity db) t))))))

(defn remove-chaff [elapsed db]
  (->>
   (concat
    (d/q '[:find ?e :where [?e :thing/health ?h] [(<= ?h 0)]] db)
    (d/q '[:find ?e :where [?e :thing/posx ?x] [(<= ?x -100)]] db)
    (d/q '[:find ?e :where [?e :thing/posy ?y] [(<= ?y -100)]] db)
    (d/q '[:find ?e :where [?e :thing/posx ?x] [(>= ?x 1000)]] db)
    (d/q '[:find ?e :where [?e :thing/posy ?y] [(>= ?y 1000)]] db))
   (map (fn [i] [:db.fn/retractEntity (first i)]))))

(defn game-loop [elapsed db]
  (reduce
   (fn [[db tx] f]
     (let [txes (f elapsed db)]
       [(:db-after (d/with db txes)) (concat tx txes)]))
   [db []]
   [remove-chaff movements collisions]))
