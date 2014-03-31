(ns eye-boof.statistics-test 
  (:refer-clojure :exclude [min max])
  (:require 
    [clojure.test :refer :all]
    [eye-boof
     [core :refer [width height]]
     [image-statistics :refer :all]
     [io :refer [load-image]]]))

(deftest statistics 
  (let [img (-> "http://www.labbookpages.co.uk/software/imgProc/files/otsuExamples/harewood.jpg"
                (java.net.URL.)
                (load-image))]
    (is (== (min img) 2))
    (is (== (max img) 255))
    (is (== (* (width img) (height img))
            (reduce + (histogram img))))))
