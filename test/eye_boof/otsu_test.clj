(ns eye-boof.otsu-test
  (:require 
    [clojure.test :refer :all]
    [eye-boof.segmentation.otsu :refer :all]
    [eye-boof.image-statistics :refer [histogram]]
    [eye-boof.io :refer [load-image]]
    ))

(deftest threshold 
  (let [histogram-url (fn [s] (-> s (java.net.URL.) (load-image) (histogram)))]
    ;; The threshold values were calculated using Octave's graythres function. 
    (is (== 146 (compute-threshold
                  (histogram-url "http://www.labbookpages.co.uk/software/imgProc/files/otsuExamples/harewood.jpg"))))
    (is (== 115 (compute-threshold 
                  (histogram-url "http://www.labbookpages.co.uk/software/imgProc/files/otsuExamples/nutsBolts.jpg"))))))
