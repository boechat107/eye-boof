(ns eye-boof.math
  "Functions to apply arithmetical operations on images."
  (:refer-clojure :exclude [* /])
  (:require 
    [eye-boof.core :refer [width height new-image]])
  (:import 
    [boofcv.alg.misc PixelMath]
    [boofcv.struct.image ImageUInt8]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn *
  "Multiplies each pixel value by a scalar value."
  [^ImageUInt8 img ^double v]
  (let [^ImageUInt8 output (new-image (width img) (height img))]
    (PixelMath/multiply img v output)
    output))

(defn /
  "Divides each pixel value by a scalar."
  [^ImageUInt8 img ^double v]
  (let [^ImageUInt8 output (new-image (width img) (height img))]
    (PixelMath/divide img v output)
    output))
