(ns eye-boof.images
  (:require [eye-boof.core :as eyec])
  (:import [boofcv.struct.image ImageSInt8 ImageSInt16 ImageSInt32 ImageSInt64]))



(def image-data-type
  {'ImageSInt8  bytes
   'ImageSInt16 shorts
   'ImageSInt32 ints
   'ImageSInt64 longs})

(defmacro get-pixel
  "Returns a primitive integer value from a channel's array ach. If coordinates 
  [x, y] and ncols are provided, the array is handled as 2D matrix.
  Warning: idx is relative to the original or parent image, so it is dangerous to use it
  for sub-images, give preference to x and y indexing.
  e.g.
    => (get-pixel ImageSInt8 img 2)"
  ([type ch idx]
     (if-let [internal-type (get image-data-type type)]
       `(let [ch# ~(vary-meta ch assoc :tag type)]
          (eyec/mult-aget ~internal-type (.data ch#) ~idx))
       (throw (Exception. "Type not recognized"))))
  ([type ch x y]
     (if-let [internal-type (get image-data-type type)]
       `(let [ch# ~(vary-meta ch assoc :tag type)]
          (eyec/mult-aget ~internal-type (.data ch#) (+ (.startIndex ch#) (+ ~x (* ~y (.stride ch#))))))
       (throw (Exception. "Type not recognized")))))


(defmacro set-pixel! 
  "Sets the value of a pixel for a given channel's array. If coordinates [x, y] and
  ncols are provided, the array is handled as 2D matrix.
   e.g.
      (set-pixel! ImageSInt8 asd 2 2 4)"
  ([type ch idx val]
     (if-let [internal-type (get image-data-type type)]
       `(let [ch# ~(vary-meta ch assoc :tag type)]
          (eyec/mult-aset ~internal-type (.data ch#) ~idx (unchecked-byte ~val)))
       (throw (Exception. "Type not recognized"))))
  ([type ch x y val]
     (if-let [internal-type (get image-data-type type)]
       `(let [ch# ~(vary-meta ch assoc :tag type)]
          (eyec/mult-aset ~internal-type (.data ch#) (+ (.startIndex ~ch) (+ (* ~y (.stride ~ch)) ~x)) (unchecked-byte ~val)))
       (throw (Exception. "Type not recognized")))))