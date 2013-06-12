(ns eye-boof.core-test
  (:use clojure.test)
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p]))

(defn time-test
  []
  (let [img (time (h/load-file-image "test/cnh.png"))
        gray (time (p/rgb-to-gray img))
        bin (time (p/binarize gray 100))
        blur (time (p/mean-blur img 1))
        ]
    ;(time (h/view img blur))
    nil
    )
  )
