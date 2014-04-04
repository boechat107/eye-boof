(ns eye-boof.geometric-transformations
  "Functions to perform geometrical transformations of 2D images. They don't change 
  the image content but deform the pixel grid, mapping it to the output image."
  (:require 
    [eye-boof.core :refer [nbands new-image]]
    [clojure.string :as s :only [upper-case replace]])
  (:import 
    [boofcv.alg.distort DistortImageOps]
    [boofcv.alg.interpolate TypeInterpolate]
    [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol PGeometricTransformations
  (resize [img new-width new-height interpolation]
          "Returns a resized version of the given input image.
          The possible interpolation algorithms are:
          :bicubic
          :bilinear 
          :nearest-neighbor
          :polynomial4"))

(let [resize-fn (fn [out-fn img nw nh ialg]
                  ;; Function that can be used for both types, ImageUInt8 and
                  ;; Multispectral. 
                  (let [^ImageBase img img
                        ;; out-fn is an output-typed function that returns a new
                        ;; image.
                        out-img (out-fn nw nh (nbands img))]
                    (DistortImageOps/scale
                      img out-img
                      (TypeInterpolate/valueOf (-> (name ialg)
                                                   (s/replace \- \_)
                                                   (s/upper-case))))
                    out-img))]
  (extend ImageUInt8 
    PGeometricTransformations 
    (let [get-single (fn ^ImageUInt8 [w h _] (new-image w h))]
      {:resize (partial resize-fn get-single)}))
  
  (extend MultiSpectral
    PGeometricTransformations 
    (let [get-mult (fn ^MultiSpectral [w h nb] (new-image w h nb))]
      {:resize (partial resize-fn get-mult)})))
