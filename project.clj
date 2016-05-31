(defproject eye-boof/eye-boof "1.2.0-SNAPSHOT"
  :description "Clojure image processing library using BoofCV."
  :url "https://github.com/boechat107/eye-boof"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.boofcv/ip "0.16"]
                 [seesaw "1.4.3"]
                 [net.mikera/clojure-utils "0.5.0"]
                 [prismatic/hiphip "0.2.0"]
                 [org.clojure/algo.generic "0.1.2"]]
  :profiles {:dev {:plugins [[lein-codox "0.9.4"]]}}
  :java-source-paths ["java"]
  :aliases {"test" ["test" ":only" "eye-boof.test.io"]})
