(ns eye-boof.vin-test
  (:require 
    [eye-boof 
     [core :as c]
     [processing :as p]
     [binary-ops :as bi]
     [helpers :as h]]
    )
  )

(def imgs 
  (->> ["test/chassi1.jpg" "test/chassi2.jpg" "test/chassi3.jpg"] 
       (map h/load-file-image)
       (map #(p/scale % 0.2))))

(defn pre 
  [img]
  (-> (p/to-gray img)
      (p/canny-edge 2 0.05 0.3)
      ))
