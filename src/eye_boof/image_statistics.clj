(ns eye-boof.image-statistics
  (:refer-clojure :exclude [min max])
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

(defmacro def-noargs-function
  [fname doc f]
  `(defn ~fname 
     ~doc
     ([~'img] (~fname ~'img 0))
     ([~'img ~'ch]
      (~f (c/get-channel ~'img ~'ch)))))

(def-noargs-function 
  min 
  "Returns the minimum pixel intensity of the channel ch (default 0)."
  ImageStatistics/min)

(def-noargs-function
  mean
  "Returns the mean pixel intensity of the channel ch (default 0) of the given
  image."
  ImageStatistics/mean)

(def-noargs-function 
  max
  "Returns the maximum pixel intensity of the channel ch (default 0)."
  ImageStatistics/max)

(def-noargs-function 
  sum
  "Returns the sum of the pixels' intensity of the channel ch (default 0)."
  ImageStatistics/sum)

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

