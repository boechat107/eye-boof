(ns eye-boof.binary-ops
  (:require [eye-boof
             [core :as c]
             [processing :as p]])

  (:require [eye-boof
             [helpers :as h]])

  (:import
   [eye_boof.core Image]
   [boofcv.alg.filter.binary BinaryImageOps]
   [boofcv.struct.image ImageSInt32]))

(defn contour [img rule]
  {:pre [(= :bw (:type img))]}
  (let [contours (BinaryImageOps/contour (:mat img) rule nil)]
    (Image. contours :contours)))

(defn labeled-img [img rule]
  {:pre [(= :bw (:type img))]}
  (let [result (ImageSInt32. (c/ncols img) (c/nrows img))]
    (BinaryImageOps/contour (:mat img) rule result)
    (Image. result :labeled)))

#_(defmethod h/to-buffered-image :contours)

#_(defmethod h/to-buffered-image :labeled)