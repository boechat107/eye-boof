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
  (^double [img] (mean img 0))
  (^double [img ch]
   (ImageStatistics/mean (c/get-channel img ch))))

(defn histogram* 
  "Like histogram, but computs its output directly from a channel matrix."
  ([ch] (histogram* ch 255))
  ([ch nl]
   (let [array (int-array nl)]
     (ImageStatistics/histogram ch array)
     (vec array))))

(defn histogram
  "Returns a vector of nl elements (default 255) corresponding to the number of
  occurences of a specific intensity value."
  ([img] (histogram img 0 255))
  ([img ch] (histogram img ch 255))
  ([img ch nl]
   (histogram* (c/get-channel img ch) nl)))

;; vertical-histogram
;; horizontal-histogram

