(ns eye-boof.core
  (:require 
    [incanter.core :as ic])
  (:import 
    [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defrecord Image [mat type])

(def color-dimensions
  {:rgb 3
   :argb 4
   :gray 1})

(defn image?
  [obj]
  (instance? Image obj))

(defn valid-type?
  [type]
  (some #(= type %) [:argb :rgb :gray]))

(defn doubles?
  [x]
  (= (type x) (Class/forName "[D")))

(defn ints?
  [x]
  (= (type x) (Class/forName "[I")))

(defn mat?
  [m]
  (instance? ImageBase m))

(defn color-type?
  [img]
  (some #(= % (:type img)) [:argb :rgb]))

(defn gray-type?
  [img]
  (= :gray (:type img)))

(defn argb-type?
  [img]
  (= :argb (:type img)))

(defn nrows
  "Returns the number of rows of an Image."
  ^long [img]
  (let [b ^ImageBase (:mat img)] (.getHeight b)))

(defn ncols
  "Returns the number of rows of an Image."
  ^long [img]
  (let [b ^ImageBase (:mat img)] (.getWidth b)))

(defn dimension 
  "Returns the number of the dimensions of the image's color space."
  ^long [img]
  ((:type img) color-dimensions))

(defn new-channel-matrix 
  "Returns a matrix used to represent a color channel data."
  [nrows ncols dim] 
  (if (> dim 1)
    (ImageUInt8. ncols nrows)
    (MultiSpectral. (.getClass ImageUInt8) ncols nrows dim)))

(defn make-image
  "Returns an instance of Image for a given image data, its number of columns of
  pixels and the color space of the image. 
  The image data is stored as different channels, each one as a clojure.matrix, and
  the value of each pixel a double value."
  ([data-chs type]
   {:pre [(valid-type? type) (mat? data-chs)]}
   (Image. data-chs type)))

(defn new-image
  "Returns an empty image with the given dimension and color type."
  [nrows ncols type]
  {:pre [(contains? color-dimensions type)]}
  (-> (new-channel-matrix nrows ncols (type color-dimensions))
      (make-image type)))

;(defn copy-image
;  "Returns a copy of a given image."
;  [img]
;  (->> (mapv #(aclone ^ints %) (:mat img)) 
;       (assoc img :mat)))

(defn get-channel
  "Returns a channel data structure of a given image. If the channel number is not
  specified, all channels are returned as a vector."
  ([img] 
   (:mat img))
  ([img ch]
   ((:mat img) ch)))

(defmacro mult-aget
  "Returns the value of an element of multiple dimensional arrays. Uses type hints to 
  improve the performance of aget.
  Reference:
  http://clj-me.cgrand.net/2009/10/15/multidim-arrays/"
  ([hint array idx]
   `(aget ~(vary-meta array assoc :tag hint) ~idx))
  ([hint array idx & idxs]
   `(let [a# (aget ~(vary-meta array assoc :tag 'objects) ~idx)]
      (mult-aget ~hint a# ~@idxs))))

(defmacro get-pixel
  "Returns a primitive integer value from a channel's array ach. If coordinates 
  [x, y] and ncols are provided, the array is handled as 2D matrix."
  ([ach idx]
   `(mult-aget ~'ints ~ach ~idx))
  ([ach x y nc]
   `(mult-aget ~'ints ~ach (+ ~x (* ~y ~nc)))))

(defn get-pixel*
  "Returns the value of the pixel [x, y]. If no channel is specified, a vector is
  returned; otherwise, a scalar is returned.
  For a better performance, use the macro get-pixel."
  (^long [img x y ch]
   (mult-aget ints ((:mat img) ch) (+ (* y (ncols img)) x)))
  (^long [img idx ch]
   (mult-aget ints ((:mat img) ch) idx)))

(defmacro mult-aset
  "Sets the value of an element of a multiple dimensional array. Uses type hints to 
  improve the performance of aset. (Only for double and int arrays for now)
  Reference:
  http://clj-me.cgrand.net/2009/10/15/multidim-arrays/"
  [hint array & idxsv]
  (let [hints '{doubles double ints int bytes byte longs long}
        [v idx & sxdi] (reverse idxsv)
        idxs (reverse sxdi)
        v (if-let [h (hints hint)] (list h v) v)
        nested-array (if (seq idxs)
                       `(mult-aget ~'objects ~array ~@idxs)
                       array)
        a-sym (with-meta (gensym "a") {:tag hint})]
    `(let [~a-sym ~nested-array]
       (aset ~a-sym ~idx ~v))))

(defmacro set-pixel! 
  "Sets the value of a pixel for a given channel's array. If coordinates [x, y] and
  ncols are provided, the array is handled as 2D matrix."
  ([ach idx val]
   `(mult-aset ~'ints ~ach ~idx ~val))
  ([ach x y ncols val]
   `(mult-aset ~'ints ~ach (+ ~x (* ~y ~ncols)))))

(defn set-pixel!*
  "Sets the value of the [x, y] pixel. For a better performance, use the macro
  set-pixel!."
  ([img x y ch val]
   (mult-aset ints ((:mat img) ch)
                 (+ x (* y (ncols img))) 
                 val))
  ([img idx ch val]
   (mult-aset ints ((:mat img) ch)
                 idx 
                 val)))

(defmacro for-idx
  "Iterates over all pixels of img, binding the pixel's index to idx.
  Ex.:
  (for-idx [idx img]
    body)"
  ;; Make a single one macro for-img which embodies for-idx and for-xy
  [[idx img] & body]
  `(let [nr# (nrows ~img)
         nc# (ncols ~img)]
     (dotimes [x# nc#]
       (dotimes [y# nr#]
         (let [~idx (+ x# (* y# nc#))]
           ~@body)))))

(defmacro for-xy
  "Iterates over all pixels of an image, binding the pixels' index to [x, y]."
  [[x y img] & body]
  `(let [nr# (nrows ~img)
         nc# (ncols ~img)]
     (dotimes [~x nc#]
       (dotimes [~y nr#]
         ~@body))))

(defmacro for-chs
  "Binds the channels of images to a local variable.
  Ex.:
  (for-chs [img-ch img, res-ch res]
    body)"
  [chs-imgs & body]
  `(let [])
  )
