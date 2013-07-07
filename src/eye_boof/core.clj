(ns eye-boof.core
  (:require 
    [incanter.core :as ic])
  (:import 
    [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageFloat32 MultiSpectral]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defrecord Image [mat type origin])

(def color-dimensions
  {:rgb 3
   :argb 4
   :gray 1
   :bw 1})

(defn image?
  [obj]
  (instance? Image obj))

(defn type [img]
  (:type img))

(defn sub-image?
  [obj]
  (and (instance? Image obj) (:origin obj)))

(defn valid-type?
  [type]
  (some #(= type %) (keys color-dimensions)))

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

(defn argb-type?
  [img]
  (= :argb (:type img)))

(defn rgb-type?
  [img]
  (= :argb (:type img)))

(defn gray-type?
  [img]
  (= :gray (:type img)))

(defn bw-type?
  [img]
  (= :bw (:type img)))

(defn one-dim?
  "Returns true if the image has just one channel."
  [img]
  (instance? ImageUInt8 (:mat img)))

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
  (let [dim ((:type img) color-dimensions)]
;   (assert (== dim ))
   dim ))

(defn new-channel-matrix 
  "Returns a matrix used to represent a color channel data."
  ^ImageUInt8 [nrows ncols dim] 
  (if (> dim 1)
    (MultiSpectral. ImageUInt8 ncols nrows dim)
    (ImageUInt8. ncols nrows)))

(defn new-float-channel
  ^ImageFloat32 [nrows ncols dim]
  (if (> dim 1)
    (MultiSpectral. ImageFloat32 ncols nrows dim)
    (ImageFloat32. ncols nrows)))

(defn make-image
  "Returns an instance of Image for a given image data, its number of columns of
  pixels and the color space of the image. 
  The image data is stored as different channels, each one as a clojure.matrix, and
  the value of each pixel a double value."
  ([data-chs type]
   {:pre [(valid-type? type) (mat? data-chs)]}
   (Image. data-chs type nil)))

(defn new-image
  "Returns an empty image with the given dimension and color type."
  [nrows ncols type]
  {:pre [(contains? color-dimensions type)]}
  (-> (new-channel-matrix nrows ncols (type color-dimensions))
      (make-image type)))

(defn new-gray-image
  "Returns an empty grayscale image (single channel) with the given dimension."
  [nr nc]
  (-> (new-channel-matrix nr nc (:gray color-dimensions))
      (make-image :gray)))

;(defn copy-image
;  "Returns a copy of a given image."
;  [img]
;  (->> (mapv #(aclone ^ints %) (:mat img)) 
;       (assoc img :mat)))

(defn get-channel
  "Returns a channel data structure of a given image. If the channel number is not
  specified, all channels are returned as a vector."
  ([img] 
   (let [chs ^MultiSpectral (:mat img)]
     (if (one-dim? img)
     (:mat img)
     (mapv #(.getBand chs %) (range (.getNumBands chs))))))
  (^ImageUInt8 [img ch]
   (if (one-dim? img) 
     (:mat img)
     (.getBand ^MultiSpectral (:mat img) ch))))

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
  [x, y] and ncols are provided, the array is handled as 2D matrix.
  Warning: idx is relative to the original or parent image, so it is dangerous to use it
  for sub-images, give preference to x and y indexing."
  ([ch idx]
  `(let [ch# ~(vary-meta ch assoc :tag 'boofcv.struct.image.ImageUInt8)]
     (-> (mult-aget ~'bytes (.data ch#) ~idx)
         (bit-and ~0xff))))
  ([ch x y]
   `(let [ch# ~(vary-meta ch assoc :tag 'boofcv.struct.image.ImageUInt8)]
     (-> (mult-aget ~'bytes (.data ch#)
                    (+ (.startIndex ch#) (+ ~x (* ~y (.stride ch#)))))
         (bit-and ~0xff)))))

(defn get-pixel*
  "Returns the value of the pixel [x, y] for a given image channel."
  (^long [^ImageUInt8 ch x y]
   (-> (mult-aget bytes (.data ch) (+ (.startIndex ch) (+ (* y (.stride ch)) x)))
       (bit-and 0xff)
       ))
  (^long [^ImageUInt8 ch idx]
   (-> (mult-aget bytes (.data ch) idx)
       (bit-and 0xff))))

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
  ([ch idx val]
   `(let [ch# ~(vary-meta ch assoc :tag 'boofcv.struct.image.ImageUInt8)]
      (mult-aset ~'bytes (.data ch#) ~idx (unchecked-byte ~val))))
  ([ch x y val]
   (set-pixel! ~ch (+ (.startIndex ~ch) (+ (* ~y (.stride ~ch)) ~x)) ~val)))

(defn set-pixel!*
  "Sets the value of the [x, y] pixel for a given channel."
  ([^ImageUInt8 ch x y val]
   (mult-aset bytes (.data ch)
              (+ (.startIndex ch) (+ (* y (.stride ch)) x))
              val))
  ([^ImageUInt8 ch idx val]
   (mult-aset bytes (.data ch)
              idx 
              val)))

(defn get-parent-point
  "If the given image is a sub-image, returns the top-left coordinates on the former
  image."
  [img]
  (:origin img))

(defn sub-image
  "Returns a sub-image from the given image, both sharing the same internal
  data-array.
  The new sub-image has a field :origin that has the coordinates [x0 y0] of the
  original image where the sub-image was taken."
  [img x0 y0 width height]
  (let [[x00 y00] (:origin img)
        orig (if (sub-image? img)
               [(+ x00 x0) (+ y00 y0)]
               [x0 y0])]
    (-> ^ImageBase (:mat img)
        (.subimage x0 y0 (+ x0 width) (+ y0 height))
        (make-image (:type img))
        (assoc :origin orig))))

(defn channel-to-vec
  "Returns an integer clojure vector of the pixels' value of a specific channel."
  [img ch]
  (let [ch-array (get-channel img ch)]
    (if (sub-image? img)
      (vec (for [x (range (ncols img)), y (range (nrows img))] 
              (get-pixel ch-array x y)))
      (mapv #(bit-and % 0xff) (seq (.data ch-array))))))

(defmacro for-idx
  "Iterates over all pixels of img, binding the pixel's index to idx. The iteration
  runs over row after row.
  Ex.:
  (for-idx [idx img]
    body)"
  ;; fixme: use stride and startindex
  ;; Make a single one macro for-img which embodies for-idx and for-xy
  [[idx img] & body]
  `(let [nr# (nrows ~img)
         nc# (ncols ~img)]
     (dotimes [y# nr#]
       (dotimes [x# nc#]
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
