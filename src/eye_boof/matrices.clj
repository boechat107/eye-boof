(ns eye-boof.matrices
  (:require [eye-boof.core :as eyec])
  (:import [boofcv.struct.image ImageSInt8 ImageSInt16 ImageSInt32 ImageSInt64]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(def image-data-type
  {'ImageSInt8  bytes
   'ImageSInt16 shorts
   'ImageSInt32 ints
   'ImageSInt64 longs
   'ImageUInt8  bytes
   'ImageUInt16 shorts})

(defmacro mget
  "Returns a primitive integer value from a matrix's array mat. If coordinates [x, y],
  the array is handled as 2D matrix.
  Warning: idx is relative to the original or parent image, so it is dangerous to use
  it for sub-images, give preference to x and y indexing.
  e.g.
    => (get-pixel ImageSInt8 img 2)"
  ([type mat idx]
     (if-let [internal-type (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag type)]
          (eyec/mult-aget ~internal-type (.data mat#) ~idx))
       (throw (Exception. (str "Type" type " not recognized")))))
  ([type mat x y]
     (if-let [internal-type (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag type)]
          (eyec/mult-aget ~internal-type (.data mat#) (+ (.startIndex mat#) (+ ~x (* ~y (.stride mat#))))))
       (throw (Exception. (str "Type " type "not recognized"))))))

;; For compatibility.
(defmacro get-pixel
  ([type mat idx] `(mget ~type ~mat ~idx))
  ([type mat x y] `(mget ~type ~mat ~x ~y)))

(defmacro mset! 
  "Sets the value of a matrix's element. If coordinates [x, y] are provided, the
  array is handled as 2D matrix.
   e.g.
      (set-pixel! ImageSInt8 asd 2 2 4)"
  ([type mat idx val]
     (if-let [internal-type (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag type)]
          (eyec/mult-aset ~internal-type (.data mat#) ~idx (unchecked-byte ~val)))
       (throw (Exception. (str "Type " type "not recognized")))))
  ([type mat x y val]
     (if-let [internal-type (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag type)]
          (eyec/mult-aset ~internal-type (.data mat#) (+ (.startIndex ~mat) (+ (* ~y (.stride ~mat)) ~x)) (unchecked-byte ~val)))
       (throw (Exception. (str "Type " type "not recognized"))))))

;; For compatibility.
(defmacro set-pixel!
  ([type mat idx val] `(mset! ~type ~mat ~idx ~val))
  ([type mat x y val] `(mset! ~type ~mat ~x ~y ~val)))
