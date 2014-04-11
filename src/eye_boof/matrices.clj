(ns eye-boof.matrices
  (:import [boofcv.struct.image ImageSInt8 ImageSInt16 ImageSInt32 ImageSInt64]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

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

;;; Defining a protocol to access matrices' basic properties.

(defprotocol ImageMatrix 
  (width [mat])
  (height [mat])
  (sub-mat [mat x0 y0 width height])
  (sub-mat? [mat])
  (dimension [mat])
  (iget [mat x y] [mat w x y])
  (iset! [mat x y v] [mat w x y v])
  (origin [mat])
  (to-vec [mat]))

(defmacro sing-protocol
  [t]
  `(extend-protocol ImageMatrix
     ~t
     ~@'((to-vec [mat]
                 (vec (for [x (range (width mat)), y (range (height mat))] 
                        (iget mat x y))))
          (parent-point [mat] 
                        (let [start-idx (.startIndex mat)
                              stride (.stride mat)]
                          [(rem start-idx stride) (quot start-idx stride)]))
          (iget [mat x y] (.unsafe_get mat x y))
          (iset! [mat x y v] (.unsafe_set mat x y v))
          (sub-mat [mat x0 y0 width height] 
                   (let [x0 (int x0), y0 (int y0), w (int width), h (int height)]
                     (.subimage mat x0 y0 (+ x0 w) (+ y0 h) nil)))
          (sub-mat? [mat] (.isSubimage mat))
          (width [mat] (.getWidth mat)) 
          (height [mat] (.getHeight mat))
          (dimension [mat] 1))))

(sing-protocol boofcv.struct.image.ImageUInt8)

(sing-protocol boofcv.struct.image.ImageUInt16)

(sing-protocol boofcv.struct.image.ImageSInt8)

(sing-protocol boofcv.struct.image.ImageSInt16)

(sing-protocol boofcv.struct.image.ImageSInt32)

(sing-protocol boofcv.struct.image.ImageSInt64)

(sing-protocol boofcv.struct.image.ImageFloat32)

(sing-protocol boofcv.struct.image.ImageFloat64)

(extend-protocol ImageMatrix 
  (Class/forName "[I")
  (iget [ar ^long width ^long x ^long y]
    (aget ^ints ar (+ x (* y width))))
  (iset! [ar width x y v]
    (let [width (int width), x (int x), y (int y)]
      (aset-int ar (+ x (* y width)) v))))

(extend-protocol ImageMatrix 
  (Class/forName "[F")
  (iget [ar ^long width ^long x ^long y]
    (aget ^floats ar (+ x (* y width))))
  (iset! [ar width x y v]
    (let [width (int width), x (int x), y (int y)]
      (aset-float ar (+ x (* y width)) v))))

(def image-data-type
  '{:sint8 [boofcv.struct.image.ImageSInt8 bytes]
    :sint16 [boofcv.struct.image.ImageSInt16 shorts]
    :sint32 [boofcv.struct.image.ImageSInt32 ints]
    :sint64 [boofcv.struct.image.ImageSInt64 longs]
    :uint8 [boofcv.struct.image.ImageUInt8 bytes]
    :uint16 [boofcv.struct.image.ImageUInt16 shorts]
    :float32 [boofcv.struct.image.ImageFloat32 floats]
    :float64 [boofcv.struct.image.ImageFloat64 doubles]})

(defmacro make-matrix [type-kw w h]
  (if-let [[type _] (get image-data-type type-kw)]
    `(new ~type ~w ~h)
    (throw (IllegalArgumentException. (str "Unknown type " type-kw)))))

(defmacro mget
  "Returns a primitive integer value from a matrix's array mat. If coordinates [x, y],
  the array is handled as 2D matrix.
  Warning: idx is relative to the original or parent image, so it is dangerous to use
  it for sub-images, give preference to x and y indexing.
  e.g.
    => (get-pixel ImageSInt8 img 2)"
  ([type mat idx]
     (if-let [[boof-type internal-type] (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag boof-type)]
          (mult-aget ~internal-type (.data mat#) ~idx))
       (throw (Exception. (str "Type" type " not recognized")))))
  ([type mat x y]
     (if-let [[boof-type internal-type] (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag boof-type)]
          (.unsafe_get mat# ~x ~y))
       (throw (Exception. (str "Type " type " not recognized"))))))

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
     (if-let [[boof-type internal-type] (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag boof-type)]
          (mult-aset ~internal-type (.data mat#) ~idx (unchecked-byte ~val)))
       (throw (Exception. (str "Type " type "not recognized")))))
  ([type mat x y val]
     (if-let [[boof-type internal-type] (get image-data-type type)]
       `(let [mat# ~(vary-meta mat assoc :tag boof-type)]
          (.unsafe_set mat# ~x ~y ~val))
       (throw (Exception. (str "Type " type "not recognized"))))))

;; For compatibility.
(defmacro set-pixel!
  ([type mat idx val] `(mset! ~type ~mat ~idx ~val))
  ([type mat x y val] `(mset! ~type ~mat ~x ~y ~val)))

(defn print-matrix
  "Prints "
  [m]
  (let [nc (.getWidth m)
        nr (.getHeight m)]
    (dotimes [y nr]
      (dotimes [x nc]
        (print (iget m x y) "" ))
      (println))))
