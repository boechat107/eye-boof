(ns eye-boof.core
  (:require
    [eye-boof.matrices :as m])
  (:import 
    [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageFloat32 MultiSpectral]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defrecord Image [mat type])

(def color-dimensions
  {:rgb 3
   :argb 4
   :gray 1
   :bw 1})

(defn image?
  [obj]
  (instance? Image obj))

(defn get-type [img]
  (:type img))

(defn sub-image?
  [obj]
  (and (instance? Image obj) (:origin obj)))

(defn valid-type?
  [type]
  (some #(= type %) (keys color-dimensions)))

(defn dimension 
  "Returns the number of the dimensions of the image's color space."
  ^long [img]
  {:pos [#(== % ((:type img) color-dimensions))]}
  (let [chs (:mat img)]
    (if (instance? MultiSpectral chs)
      (.getNumBands ^MultiSpectral chs)
      1)))

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
  (and (= :gray (:type img))
       (== 1 (dimension img))))

(defn bw-type?
  [img]
  (and (= :bw (:type img))
       (== 1 (dimension img))))

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

(defn new-channel-matrix 
  "Returns a matrix used to represent a color channel data."
  [nrows ncols dim] 
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
   (Image. data-chs type)))

(defn new-image
  "Returns an empty image with the given size and color type. If an img is given,
  the returned image should have the same size."
  ([img type] (new-image (nrows img) (ncols img) type))
  ([nrows ncols type]
   {:pre [(contains? color-dimensions type)]}
   (-> (new-channel-matrix nrows ncols (type color-dimensions))
       (make-image type))))

(defn new-gray-image
  "Returns an empty grayscale image (single channel) with the given dimension."
  [nr nc]
  (-> (new-channel-matrix nr nc (:gray color-dimensions))
      (make-image :gray)))

(defn into-bw
  "Given a sequence of byte values, creates a BW image with width 'width'
   e.g.
      => (into-bw 2 [0 1 1 0])
      ;=> #eye_boof.core.Image{:mat ... :type :bw, ...} "
  [width seq]
  {:pre [(= 0 (mod (count seq) width))]}
  (let [chn (ImageUInt8. width (quot (count seq) width))]
    (set! (.data chn) (into-array Byte/TYPE seq))
    (make-image chn :bw)))

(defn into-gray
  "Given a sequence of byte values, creates a gray image with width 'width'
   e.g.
      => (into-gray 2 [10 20 30 40])
      ;=> #eye_boof.core.Image{:mat ... :type :gray, ...} "
  [width seq]
  {:pre [(= 0 (mod (count seq) width))]}
  (let [chn (ImageUInt8. width (quot (count seq) width))]
    (set! (.data chn) (into-array Byte/TYPE seq))
    (make-image chn :gray)))

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

(defmacro get-pixel
  "Returns a primitive integer value from a channel's array ach. If coordinates 
  [x, y] and ncols are provided, the array is handled as 2D matrix.
  Warning: idx is relative to the original or parent image, so it is dangerous to use it
  for sub-images, give preference to x and y indexing."
  ([ch idx]
   `(-> (m/mget ~'ImageUInt8 ~ch ~idx)
        (bit-and ~0xff)))
  ([ch x y]
   `(-> (m/mget ~'ImageUInt8 ~ch ~x ~y)
        (bit-and ~0xff))))

(defn get-pixel*
  "Returns the value of the pixel [x, y] for a given image channel."
  (^long [^ImageUInt8 ch x y]
   (.unsafe_get ch x y))
  (^long [^ImageUInt8 ch idx]
   (-> (m/mult-aget bytes (.data ch) idx)
       (bit-and 0xff))))

(defmacro set-pixel! 
  "Sets the value of a pixel for a given channel's array. If coordinates [x, y] and
  ncols are provided, the array is handled as 2D matrix."
  ([ch idx val]
   `(m/mset! ~'ImageUInt8 ~ch ~idx ~val))
  ([ch x y val]
   `(m/mset! ~'ImageUInt8 ~ch ~x ~y ~val)))

(defn set-pixel!*
  "Sets the value of the [x, y] pixel for a given channel."
  ([^ImageUInt8 ch x y val]
   (let [x (int x)
         y (int y)]
     (.unsafe_set ch x y val)))
  ([^ImageUInt8 ch idx val]
   (m/mult-aset bytes (.data ch)
              idx 
              val)))

(defn get-parent-point
  "Returns the coordinates [x, y] of the first pixel of img in its parent image. 
  If img is not a sub-image, [x, y] = [0, 0]."
  [img]
  (let [ch (get-channel img 0)
        start-idx (.startIndex ch)
        stride (.stride ch)]
    [(rem start-idx stride) (quot start-idx stride)]))

(defn sub-image
  "Returns a sub-image from the given image, both sharing the same internal
  data-array."
  [img x0 y0 width height]
  (-> ^ImageBase (:mat img)
      (.subimage x0 y0 (+ x0 width) (+ y0 height))
      (make-image (:type img))))

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
