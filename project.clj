(defproject eye-boof/eye-boof "2.0.0-SNAPSHOT"
  :description "Clojure image processing library using BoofCV."
  :url "https://github.com/boechat107/eye-boof"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.boofcv/core "0.26"]
                 [prismatic/hiphip "0.2.1"]
                 [org.clojure/algo.generic "0.1.2"]]
  :profiles {:dev {:plugins [[lein-codox "0.10.3"]]}})
