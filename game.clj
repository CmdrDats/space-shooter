
=> (new-ent {:player/name "Deon" :player/score 0} :player)
  
{:entity/type :player, :entity/uuid #uuid "527740ce-962f-49dd-9978-36e385980f4c", :db/id #db/id[:db.part/user -1000000], :player/score 0, :player/name "Deon"}

=> (tx [*1])
ExceptionInfo :db.error/db-not-found Could not find testdb in catalog  datomic.error/raise (error.clj:46)

=> (setup-db)
#<promise$settable_future$reify__4424@5220c1b: {:db-before datomic.db.Db@4f97ab72, :db-after datomic.db.Db@df8e05ff, :tx-data [...], :tempids {...}}

=> (new-ent {:player/name "Deon" :player/score 0} :player)
{:entity/type :player, :entity/uuid #uuid "527740ce-962f-49dd-9978-36e385980f4c", :db/id #db/id[:db.part/user -1000000], :player/score 0, :player/name "Deon"}
=> (def p *1)
#'space-shooter.db/p
=> (tx [p])
#<promise$settable_future$reify__4424@3f901572: {:db-before datomic.db.Db@df8e05ff, :db-after datomic.db.Db@ee7763e0, :tx-data [...], :tempids {...}}>

=> (d/q '[:find ?e :where [?e :entity/type :player]] (db))
#{[17592186045418]}
=> (e (first *1))
{:entity/type :player, :entity/uuid #uuid "527740ce-962f-49dd-9978-36e385980f4c", :player/score 0, :player/name "Deon", :db/id 17592186045418}

(defn spawn-bullet [elapsed db]
  [(spawn :bullet 10 nil [100.0 100.0] [5.0 5.0] [0.0 -5.0])])

=> (db/tx [(db/spawn :ship 10 nil [100.0 100.0] [10.0 10.0] [1.0 2.0])])
#<promise$settable_future$reify__4424@3cde8a82: {:db-before datomic.db.Db@eebfe950, :db-after datomic.db.Db@c77c1b7f, :tx-data [...], :tempids {...}}>

=> (movements 100 (db/db))
([:inc 17592186045422 :thing/posx 100.0]
 [:inc 17592186045422 :thing/posy 200.0])
=> (db/tx *1)
#<promise$settable_future$reify__4424@4d036908: {:db-before datomic.db.Db@8f7ff0d8, :db-after datomic.db.Db@8ee561ed, :tx-data [...], :tempids {}}

=> (map db/e (d/q '[:find ?e :where [?e :entity/type :ship]] (db/db)))
({:entity/type :ship,
  :entity/uuid #uuid "5278077d-3497-4fdb-94fe-a032633d15f1",
  :thing/posx 200.0, :thing/posy 300.0,
  :thing/height 10.0, :thing/vely 2.0,
  :thing/width 10.0, :thing/velx 1.0,
  :thing/health 10, :db/id 17592186045422})

=> (db/tx
 [(db/spawn :ship 10 nil [200.0 200.0] [10.0 10.0] [1.0 2.0])
  (db/spawn :ship 6  nil [205.0 205.0] [10.0 10.0] [1.0 2.0])])
#<promise$settable_future$reify__4424@45d017d4:....

(defn collides [ep es op os]
  (or (and (> (+ ep es) op) (< ep (+ op os)))
      (and (> (+ op os) ep) (< op (+ ep es)))))

=> (d/q
    '[:find ?e ?o :where
      [?e :thing/posx ?epx]
      [?e :thing/width ?ew]
      [?o :thing/posx ?opx]
      [?o :thing/width ?ow]
      [(space-shooter.loop/collides ?epx ?ew ?opx ?ow)]
      [?e :thing/posy ?epy]
      [?e :thing/height ?eh]
      [?o :thing/posy ?opy]
      [?o :thing/height ?oh]
      [(space-shooter.loop/collides ?epy ?eh ?opy ?oh)]
      [(!= ?e ?o)]] (db/db))
#{[17592186045419 17592186045420] [17592186045420 17592186045419]}

=> (db/tx [(db/spawn :ship 100 nil [100.0 100.0] [10.0 10.0] [1.0 2.0])])
#<promise$settable_future$reify__4424@6596f6ef:...

=> (d/q
    '[:find ?e ?o :where [?e :thing/posx] [?o :thing/posx]
      [(space-shooter.loop/collides $ ?e ?o :thing/posx :thing/width)]
      [(space-shooter.loop/collides $ ?e ?o :thing/posy :thing/height)]
      [(!= ?e ?o)]] (db/db))
#{[17592186045419 17592186045420] [17592186045420 17592186045419]}

=> (collisions 0 (db/db))
([:inc 17592186045419 :thing/health -6] [:inc 17592186045420 :thing/health -10])
=> (db/tx *1)
#<promise$settable_future$reify__4424@23f23303:...
=> (map (comp (juxt :db/id :thing/posx :thing/health) db/e) (d/q '[:find ?e :where [?e :entity/type :ship]] (db/db)))
([17592186045420 205.0 -4]
 [17592186045419 200.0 4]
 [17592186045422 100.0 100])

=> (d/q '[:find ?e :where [?e :thing/health ?h] [(<= ?h 0)]] (db/db))
#{[17592186045420]}

=> (remove-chaff 0 (db/db))
([:db.fn/retractEntity 17592186045420])
=> (db/tx *1)
#<promise$settable_future$reify__4424@7a2fc0ff:...
=> (def t *1)
#'space-shooter.loop/t

=> (remove-chaff 0 (db/db))
()

=> (remove-chaff 0 (:db-before @t))
([:db.fn/retractEntity 17592186045420])

=> (game-loop 5 (db/db))
[datomic.db.Db@78eee823 ([:inc 17592186045419 :thing/posx 5.0] [:inc 17592186045419 :thing/posy 10.0] [:inc 17592186045422 :thing/posx 5.0] [:inc 17592186045422 :thing/posy 10.0])]

=> (db/tx (second *1))
#<promise$settable_future$reify__4424@2035938e:...

(defn start-sketch []
  (sketch
   :title "Simple Space Shooter"
   :setup #'setup
   :draw #'draw
   :size [800 600]))

=> (ns space-shooter.loop)
nil
=> (db/tx (second (game-loop 1 (db/db))))
#<promise$settable_future$reify__4424@775dfb9d:...

=> (doseq [_ (range 50)]
     (db/tx (second (game-loop 0.3 (db/db))))
     (Thread/sleep 50))
nil

=> (start-sketch)
...
=> (doseq [_ (range 10000)] (db/tx (second (game-loop 1 (db/db)))) (Thread/sleep 10))
