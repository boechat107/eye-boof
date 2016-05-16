(ns eye-boof.core
  "Namespace that contains some basic functions to help to handle the BoofCV data 
  structures.
  An image is considered to be an ImageUInt8 or a MultiSpectral with n ImageUInt8
  bands."
  (:require [clojure.algo.generic.arithmetic :refer [* /]])
  (:refer-clojure :exclude [* /])
  (:import 
    [boofcv.struct.image
     ImageBase
     ImageInteger
     ImageUInt8
     ImageFloat32
     ImageSInt16
     ImageSingleBand
     MultiSpectral]
    [boofcv.alg.misc PixelMath]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(def types-map
  (let [m {:float32 ImageFloat32
           :sint16 ImageSInt16
           :uint8 ImageUInt8}]
    (merge m (clojure.set/map-invert m))))

(defn new-image
  "Returns an SingleBand or MultiSpectral images with the given width, height and
  type. MultiSpectral images are created with nb bands greater than 1. 
  Available image types:
  * :float32
  * :sint16
  * :uint8"
  ([w h itype] (new-image w h itype 1))
  ([w h itype nb]
   (let [img-class (types-map itype)]
     (if (> nb 1)
       (MultiSpectral. img-class w h nb)
       (clojure.lang.Reflector/invokeConstructor img-class (into-array Object [w h]))))))

(defn image?
  "Returns true if the given object is a BoofCV data structure."
  [obj]
  (instance? ImageBase obj))

(defmacro pixel*
  "Returns the intensity of the pixel [x, y]. Only ImageUInt8 images are supported.
  This macro is intended for high performance code."
  [img x y]
  `(.unsafe_get ~(vary-meta img assoc :tag 'ImageUInt8) ~x ~y))

(defmacro set-pixel!* 
  "Sets the intensity of the pixel [x, y] to v. Only ImageUInt8 images are supported.
  This macro is intended for high performance code."
  [img x y v]
  `(.unsafe_set ~(vary-meta img assoc :tag 'ImageUInt8) ~x ~y ~v))

(defn band 
  "Returns a specific image band from a MultiSpectral image.
  idx starts from 0 until (nbands - 1)." 
  [^MultiSpectral img idx]
  (.getBand img idx))

(defprotocol PImage
  (parent-origin [img] 
                 "Returns the coordinates [x, y] of the first pixel of img in its
                 parent image.")
  (height [img] "Returns the number of rows of an Image.")
  (width [img] "Returns the number of columns of an Image.")
  (sub-image [img x0 y0 x1 y1]
             "Returns a sub-image from the given image, both sharing the same
             internal data-array. The coordinates [x1, y1] are not inclusive.")
  (sub-image? [img])
  (pixel [img x y]
         "Returns the intensity of the pixel [x, y]. If the given image has more than
         one band, a vector of the values for each band is returned.  This function
         is not intended for high performance.")
  (set-pixel! [img x y v] [img x y v & vs]
              "Sets the intensity of the pixel [x, y] to v, for an ImageUInt8 image.
              For a MultiSpectral image, the pixel of each band is set for the values
              vs." )
  (nbands [img] "Returns the number of bands of the image."))

;; Protocol's implementations using maps to reuse code for inheritance.
(let [base
      {:width (fn [^ImageBase img] (.getWidth img))
       :height (fn [^ImageBase img] (.getHeight img))
       :parent-origin (fn [^ImageBase img]
                        (let [start-idx (.startIndex img)
                              stride (.stride img)]
                          [(rem start-idx stride) (quot start-idx stride)]))
       :sub-image? (fn [^ImageBase img] (.isSubimage img))}
      ;; Implementations for SingleBand images.
      single-band
      {:sub-image (fn [img x0 y0 x1 y1]
                    (.subimage ^ImageSingleBand img x0 y0 x1 y1 nil))
       :nbands (fn [img] 1)}
      ;; Implementations for MultiSpectral images.
      multispectral 
      {:sub-image (fn [img x0 y0 x1 y1]
                    (.subimage ^MultiSpectral img x0 y0 x1 y1 nil))
       :nbands (fn [^MultiSpectral img] (.getNumBands img))}]
  (extend ImageUInt8
    PImage
    (merge base single-band
           {:set-pixel! (fn [^ImageUInt8 img x y v] (set-pixel!* img x y v))
            :pixel (fn [img x y] (pixel* img x y))}))
  (extend ImageSInt16 
    PImage
    (merge base single-band
           {:set-pixel! (fn [^ImageSInt16 img x y v] 
                          (.unsafe_set img x y v))
            :pixel (fn [^ImageSInt16 img x y] 
                     (.unsafe_get img x y))}))
  (extend ImageFloat32
    PImage
    (merge base single-band
           {:set-pixel! (fn [^ImageFloat32 img x y v] 
                          (.unsafe_set img x y v))
            :pixel (fn [^ImageFloat32 img x y] 
                     (.unsafe_get img x y))}))
  (extend MultiSpectral 
    PImage
    (merge base multispectral
           {:pixel (fn [^MultiSpectral img x y]
                     (mapv #(pixel* (band img %) x y) (range (nbands img))))
            :set-pixel! (fn [img x y v & vs]
                          (let [^MultiSpectral img img]
                            (dorun
                              (map #(set-pixel!* (band img %2) x y %1)
                                   (conj vs v)
                                   (range (nbands img))))))})))

(def nrows "Alias to height fn." height)

(def ncols "Alias to width fn." width)

(defn print-matrix!
  "Prints the pixels intensities as a matrix."
  [^ImageInteger img]
  (.print img))

(defn average-band
  "Returns an ImageUInt8 as a result of averaging the values of a pixel across all
  bands of a MultiSpectral image."
  [^MultiSpectral img]
  (let [out-img (new-image (width img) (height img) :uint8)]
    (PixelMath/averageBand img out-img)
    out-img))

;; ===================================================
;; Arithmetic operations with images
;; ===================================================

(defn- g* 
  [^ImageBase img ^double x itype]
  (condp = itype
    :uint8 (let [^ImageUInt8 out (new-image (width img) (height img) :uint8)]
             (PixelMath/multiply ^ImageUInt8 img x out)
             out)
    :float32 (let [^ImageFloat32 out (new-image (width img) (height img) :float32)]
               (PixelMath/multiply ^ImageFloat32 img x out)
               out)
    :sint16 (let [^ImageSInt16 out (new-image (width img) (height img) :sint16)]
               (PixelMath/multiply ^ImageSInt16 img x out)
               out)))

(defmethod * [boofcv.struct.image.ImageUInt8 java.lang.Number]
  [img x]
  (g* img x :uint8))

(defmethod * [java.lang.Number boofcv.struct.image.ImageUInt8]
  [x img]
  (g* img x :uint8))

(defmethod * [boofcv.struct.image.ImageSInt16 java.lang.Number]
  [img x]
  (g* img x :sint16))

(defmethod * [java.lang.Number boofcv.struct.image.ImageSInt16]
  [x img]
  (g* img x :sint16))

(defmethod * [boofcv.struct.image.ImageFloat32 java.lang.Number]
  [img x]
  (g* img x :float32))

(defmethod * [java.lang.Number boofcv.struct.image.ImageFloat32]
  [x img]
  (g* img x :float32))

(defmethod * [boofcv.struct.image.ImageFloat32 boofcv.struct.image.ImageFloat32]
  [^ImageFloat32 imgx ^ImageFloat32 imgy]
  (let [^ImageFloat32 out (new-image (width imgx) (height imgx) :float32)]
    (PixelMath/multiply imgx imgy out)
    out))

(defmethod / [boofcv.struct.image.ImageUInt8 java.lang.Number]
  [^ImageUInt8 img ^double x]
  (let [^ImageUInt8 out (new-image (width img) (height img) :uint8)]
    (PixelMath/divide img x out)
    out))
