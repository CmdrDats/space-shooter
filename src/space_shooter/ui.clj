
(ns space-shooter.ui
  (:use [quil.core])
  (:require [datomic.api :as d])
  (:require [space-shooter.db :as db]))

(defn setup []
  (smooth)
  (frame-rate 10)
  (background 200))

(defmulti render-thing :entity/type)

(defmethod render-thing :ship [e]
  (ellipse (:thing/posx e) (:thing/posy e) (:thing/width e) (:thing/height e)))

(defmethod render-thing :default [e]
  (ellipse (:thing/posx e) (:thing/posy e) (:thing/width e) (:thing/height e)))

(defn render-velocity [{x :thing/posx y :thing/posy vx :thing/velx vy :thing/vely}]
  (line x y (+ x (* vx 10)) (+ y (* vy 10))))

(defn draw []
  (let [db (db/db)]
    (background 200)
    (doseq [es (d/q '[:find ?e :where [?e :thing/posx]] db)
            :let [ent (d/entity db (first es))]]
      (render-thing ent)
      (render-velocity ent))))

(defn vel-towards [x y {px :thing/posx py :thing/posy e :db/id}]
  (let [vx (- x px) vy (- y py) m (mag vx vy)]
    [[:db/add e :thing/velx (double (norm vx 0 m))]
     [:db/add e :thing/vely (double (norm vy 0 m))]]))

(defn mouse-moved []
  (let [db (db/db)
        [x y] [(mouse-x) (mouse-y)]]
    (->>
     (d/q '[:find ?e :where [?e :entity/type :ship]] db)
     (mapcat (comp (partial vel-towards x y)
                   (partial d/entity db) first))
     (db/tx))))

(defn shoot-bullet [[x y] {px :thing/posx py :thing/posy e :db/id}]
  (let [vx (- x px) vy (- y py) m (mag vx vy)
        vx (* 5 (norm vx 0 m)) vy (* 5 (norm vy 0 m))]
    [(db/spawn :bullet 2 nil [(+ px (* 4 vx)) (+ py (* 4 vy))] [2.0 2.0] [vx vy])]))

(defn mouse-click []
  (let [db (db/db)
        pos [(double (mouse-x)) (double (mouse-y))]]
    (case (mouse-button)
      :left
      (->>
       (d/q '[:find ?e :where [?e :entity/type :ship]] db)
       (mapcat
        (comp (partial shoot-bullet pos)
              (partial d/entity db) first))
       (db/tx))
      :right (db/tx [(db/spawn :ship 2 nil pos [10.0 10.0] [0.0 0.0])])
      nil)))

(defn start-sketch []
  (sketch
   :title "Simple Space Shooter"
   :setup #'setup
   :draw #'draw
   :size [800 600]
   :mouse-moved #'mouse-moved
   :mouse-clicked #'mouse-click))
