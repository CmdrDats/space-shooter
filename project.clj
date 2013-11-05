
(defproject space-shooter "0.1.0-SNAPSHOT"
  :description "A simple space shooter implementation"
  :url "http://github.com/CmdrDats/space-shooter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.5.1"]
   [com.datomic/datomic-free "0.8.4254"]
   [datomic-schema "1.0.2"]
   [quil "1.6.0"]]
  :main space-shooter.core)
