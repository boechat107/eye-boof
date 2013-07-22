(ns eye-boof.image-statistics
  (:require [eye-boof.core :as c])
  (:import 
    [boofcv.alg.misc ImageStatistics]
    [boofcv.struct.image ImageUInt8 ImageSInt16 ImageFloat32]))


;;(TODO) implement the remaining functions
;; min
;; max
;; max-abs
;; sum
;; mean
;; variance
;; mean-diff-square
;; mean-diff-abs
;; histogram

(defn mean
  "Returns the mean pixel intensity of the channel ch (default 0) of the given
  image."
  ([img] (mean img 0))
  ([img ch]
   (ImageStatistics/mean (c/get-channel img ch))))


;; vertical-histogram
;; horizontal-histogram

