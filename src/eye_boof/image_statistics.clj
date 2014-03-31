(ns eye-boof.image-statistics
  "Statistical functions that returns a value for a whole Image."
  (:refer-clojure :exclude [min max])
  (:require
    [eye-boof.utils :refer [def-noargs-function]])
  (:import 
    [boofcv.alg.misc ImageStatistics]
    [boofcv.struct.image ImageUInt8]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(def-noargs-function 
  min 
  "Returns the minimum pixel intensity of an Image."
  ImageStatistics/min)

(def-noargs-function
  mean
  "Returns the mean pixel intensity of an Image."
  ImageStatistics/mean)

(def-noargs-function 
  max
  "Returns the maximum pixel intensity of an Image."
  ImageStatistics/max)

(def-noargs-function 
  sum
  "Returns the sum of the pixels' intensity of an Image."
  ImageStatistics/sum)

(defn histogram 
  "Computes the histogram of an Image and returns the values as a vector. The number
  of bins can be specified (default 256)."
  ([img] (histogram img 256))
  ([^ImageUInt8 img nbins]
   (let [array (int-array nbins)]
     (ImageStatistics/histogram img array)
     (vec array))))
