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

#_(defprotocol ImageMatrix 
  (width [mat])
  (height [mat])
  (dimension [mat])
  (mget [mat idx] [mat x y])
  (mset! [mat idx v] [mat x y v])
  (parent-point [mat])
  (to-vec [mat]))

#_(extend-protocol ImageMatrix 
  boofcv.struct.image.ImageUInt8
  (width [mat] (.getWidth mat))
  (height [mat] (.getHeight mat))
  (dimension [mat] 1)
  (mget 
    ([mat x y]
     (-> (.unsafe_get mat x y) (bit-and 0xff)))
    ([mat idx]
     (-> (mult-aget bytes (.data mat) idx) (bit-and 0xff))))
  (mset! 
    ([mat x y v] (.unsafe_set mat x y v))
    ([mat idx v] (mult-aset bytes mat idx v)))
  (parent-point [mat] 
    (let [start-idx (.startIndex mat)
          stride (.stride mat)]
      [(rem start-idx stride) (quot start-idx stride)]))
  (to-vec [mat]
    (if (= [0 0] (parent-point mat))
      (mapv #(bit-and % 0xff) (seq (.data mat)))
      (vec (for [x (range (width mat)), y (range (height mat))] 
             (mget mat x y))))))

(def image-data-type
  '{:sint8 [boofcv.struct.image.ImageSInt8 bytes]
    :sint16 [boofcv.struct.image.ImageSInt16 shorts]
    :sint32 [boofcv.struct.image.ImageSInt32 ints]
    :sint64 [boofcv.struct.image.ImageSInt64 longs]
    :uint8 [boofcv.struct.image.ImageUInt8 bytes]
    :uint16 [boofcv.struct.image.ImageUInt16 shorts]})

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
