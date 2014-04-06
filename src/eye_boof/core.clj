(ns eye-boof.core
  "Namespace that contains some basic functions to help to handle the BoofCV data 
  structures.
  An image is considered to be an ImageUInt8 or a MultiSpectral with n ImageUInt8
  bands."
  (:import 
    [boofcv.struct.image
     ImageBase
     ImageInteger
     ImageUInt8
     ImageSingleBand
     MultiSpectral]
    [boofcv.alg.misc PixelMath]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn new-image
  "Returns an ImageUInt8 or MultiSpectral object with the given width and height."
  ([w h] (new-image w h 1))
  ([w h nb]
   (if (> nb 1)
     (MultiSpectral. ImageUInt8 w h nb)
     (ImageUInt8. w h))))

(defn image?
  "Returns true if the given object is an ImageUInt8 or MultiSpectral."
  [obj]
  (or (instance? boofcv.struct.image.ImageUInt8 obj)
      (instance? boofcv.struct.image.MultiSpectral obj)))

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
  (sub-image [img x0 y0 w h]
             "Returns a sub-image from the given image, both sharing the same
             internal data-array.")
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
      single-band
      {:sub-image (fn [img x0 y0 w h]
                    (.subimage ^ImageSingleBand img x0 y0 (+ x0 w) (+ y0 h) nil))
       :nbands (fn [img] 1)}
      multispectral 
      {:sub-image (fn [img x0 y0 w h]
                    (.subimage ^MultiSpectral img x0 y0 (+ x0 w) (+ y0 h) nil))
       :nbands (fn [^MultiSpectral img] (.getNumBands img))}]
  (extend ImageUInt8
    PImage
    (merge base single-band
           {:set-pixel! (fn [^ImageUInt8 img x y v] (set-pixel!* img x y v))
            :pixel (fn [img x y] (pixel* img x y))}))
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
  (let [^ImageUInt8 out-img (new-image (width img) (height img))]
    (PixelMath/averageBand img out-img)
    out-img))

;
;(defn channel-to-vec
;  "Returns an integer clojure vector of the pixels' value of a specific channel."
;  ([img]
;     (channel-to-vec img 0))
;  ([img ch]
;     (let [ch-array (get-channel img ch)]
;       (if (sub-image? img)
;         (vec (for [x (range (ncols img)), y (range (nrows img))] 
;                (get-pixel ch-array x y)))
;         (mapv #(bit-and % 0xff) (seq (.data ch-array)))))))
;
;(defmacro for-idx
;  "Iterates over all pixels of img, binding the pixel's index to idx. The iteration
;  runs over row after row.
;  Ex.:
;  (for-idx [idx img]
;    body)"
;  ;; fixme: use stride and startindex
;  ;; Make a single one macro for-img which embodies for-idx and for-xy
;  [[idx img] & body]
;  `(let [nr# (nrows ~img)
;         nc# (ncols ~img)]
;     (dotimes [y# nr#]
;       (dotimes [x# nc#]
;         (let [~idx (+ x# (* y# nc#))]
;           ~@body)))))
;
;(defmacro for-xy
;  "Iterates over all pixels of an image, binding the pixels' index to [x, y]."
;  [[x y img] & body]
;  `(let [nr# (nrows ~img)
;         nc# (ncols ~img)]
;     (dotimes [~x nc#]
;       (dotimes [~y nr#]
;         ~@body))))
;
;(defmacro for-chs
;  "Binds the channels of images to a local variable.
;  Ex.:
;  (for-chs [img-ch img, res-ch res]
;    body)"
;  [chs-imgs & body]
;  `(let [])
;  )
;
;(defn copy-image
;  "Returns a copy of a given image."
;  [img]
;  (let [out (new-image (nrows img) (ncols img) (:type img))]
;    (dotimes [ch (dimension img)]
;      (let [och (get-channel out ch)
;            ich (get-channel img ch)]
;        (for-xy [x y img]
;                (->> (get-pixel ich x y)
;                     (set-pixel! och x y)))))))
;
;(defn print-img [img]
;  (let [nc (ncols img)
;        nr (nrows img)]
;    (dotimes [nchn (dimension img)]
;      (println "Channel" nchn)
;      (m/print-matrix (get-channel img nchn)))))
