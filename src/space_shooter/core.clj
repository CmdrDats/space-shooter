
(ns space-shooter.core
  (:require [space-shooter.db :as db])
  (:require [space-shooter.loop :as loop])
  (:require [space-shooter.ui :as ui])
  (:gen-class))

(def paused (atom false))

(defn pause []
  (reset! paused true))

(defn unpause []
  (reset! paused false))

(defn run-game []
  (if-not @paused
    (do
      (db/tx (second (loop/game-loop 0.5 (db/db))))
      (Thread/sleep 10))
    (do (Thread/sleep 500)))
  (recur))

(defn -main [& args]
  (db/setup-db)
  (ui/start-sketch)
  (doto (Thread. run-game)
    (.setDaemon true)
    (.start)))
