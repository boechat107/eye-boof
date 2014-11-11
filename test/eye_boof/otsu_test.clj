(ns eye-boof.otsu-test
  (:require [eye-boof.binary-ops :refer [invert-pixels]]
            [eye-boof.helpers :refer [load-file-image]]
            [eye-boof.processing :refer [adaptive-square otsu-threshold]]
            [eye-boof.visualize :refer [view*]]
            [taoensso.timbre.profiling :as p :only [profile p]]
            ))

#_(deftest threshold 
  (let [histogram-url (fn [s] (-> s (java.net.URL.) (load-image) (histogram)))]
    ;; The threshold values were calculated using Octave's graythres function. 
    (is (== 146 (compute-threshold
                  (histogram-url "http://www.labbookpages.co.uk/software/imgProc/files/otsuExamples/harewood.jpg"))))
    (is (== 115 (compute-threshold 
                  (histogram-url "http://www.labbookpages.co.uk/software/imgProc/files/otsuExamples/nutsBolts.jpg"))))))

(defn local-test
  []
  (let [img (load-file-image "test/son1.gif")]
    ;(p/profile :info :otsu (otsu-threshold img :local 10))
    ;(view* (time (otsu-threshold img :local 20)))
    ;(view* (time (adaptive-square img 20 -8)))
    (view* (time (otsu-threshold img :pix-win 20)))
    #_(time (adaptive-square img 15 -8))
    #_(view* (otsu-threshold img :local 20) (adaptive-square img 15 -8))
    )
  )
