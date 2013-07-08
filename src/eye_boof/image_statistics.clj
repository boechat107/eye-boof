(ns eye-boof.image-statistics
  (:require [eye-boof.core :as eyec])
  (:import [boofcv.alg.misc ImageStatistics]))


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
  "Returns the mean pixel intensity"
  [img]
  (if (eyec/one-dim? img)
    (ImageStatistics/mean (eyec/get-channel img))
    (throw (Exception. "Not implemented yet"))))


;; vertical-histogram
;; horizontal-histogram

