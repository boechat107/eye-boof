(defproject eye-boof/eye-boof "2.1.0"
  :description "Clojure image processing library based on BoofCV."
  :url "https://github.com/boechat107/eye-boof"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.boofcv/core "0.26"]
                 [prismatic/hiphip "0.2.1"]
                 [org.clojure/algo.generic "0.1.2"]
                 ;; Used to expose definitions of other namespaces.
                 [potemkin "0.4.3"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [criterium "0.4.4"]]
                   :plugins [[lein-codox "0.10.3"]
                             [lein-cloverage "1.0.9"]]}})
