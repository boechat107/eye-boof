(ns eye-boof.pixel-math
  (:require
   [eye-boof.image-statistics :refer [def-noargs-function]]
   [eye-boof.core :as c])
  (:import
   [boofcv.alg.misc PixelMath]))


;; abs
;; invert
;; multiply
;; divide
;; plus
;; boundImage
;; diffAbs
;; averageBand

(defn diff-abs [img1 img2]
  (let [result (c/new-gray-image (c/nrows img1) (c/ncols img2))]
    (PixelMath/diffAbs (c/get-channel img1) (c/get-channel img2) (c/get-channel result))
    result))