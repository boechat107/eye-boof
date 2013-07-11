(ns eye-boof.resources
  (:require [eye-boof.helpers :as h]
            [eye-boof.core :as eyec]))


(def boofcv-logo
  (delay
   (h/load-file-image "resources/boofcv.jpg")))

(def img-small-connected
  "Sample binary Image with the following pixels set on"
  (eyec/into-bw  7
                 [0 0 0 0 1 1 1
                  1 1 1 0 1 1 1
                  1 0 1 0 1 1 0
                  1 1 1 0 0 0 0
                  0 0 0 0 1 1 0
                  0 1 1 0 1 1 0
                  0 1 1 0 0 0 1]))