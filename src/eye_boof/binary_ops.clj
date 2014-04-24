(ns eye-boof.binary-ops
  "Operations over binary images, where zero values are considered false values and
  one values are considered true values."
  #_(:require
    [eye-boof 
     [core :as c]])
  (:import
    [boofcv.alg.filter.binary Contour BinaryImageOps]
    [boofcv.struct.image ImageSInt32 ImageUInt8]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn dilate4!
  "Considers a 4-neighborhood. Changes the output image."
  [^ImageUInt8 input ^ImageUInt8 output]
  (BinaryImageOps/dilate4 input output))

(defn dilate4
  "Considers a 4-neighborhood."
  [input] (dilate4! input nil))

(defn dilate8!
  "Considers a 8-neighborhood. Changes the output image."
  [^ImageUInt8 input ^ImageUInt8 output]
  (BinaryImageOps/dilate8 input output))

(defn dilate8
  "Considers a 8-neighborhood."
  [input] (dilate8! input nil))

(defn erode4!
  "Considers a 4-neighborhood. Changes the output image."
  [^ImageUInt8 input ^ImageUInt8 output]
  (BinaryImageOps/erode4 input output))

(defn erode4
  "Considers a 4-neighborhood."
  [input] (erode4! input nil))

(defn erode8!
  "Considers a 8-neighborhood. Changes the output image."
  [^ImageUInt8 input ^ImageUInt8 output]
  (BinaryImageOps/erode8 input output))

(defn erode8
  "Considers a 8-neighborhood."
  [input] (erode8! input nil))

(defn edge4!
  "Removes all pixels but ones which are on the edge of an object.
  Considers a 4-neighborhood. Changes the output image."
  [^ImageUInt8 input ^ImageUInt8 output]
  (BinaryImageOps/edge4 input output))

(defn edge4
  "Removes all pixels but ones which are on the edge of an object.
  Considers a 4-neighborhood."
  [input] (edge4! input nil))

(defn edge8!
  "Removes all pixels but ones which are on the edge of an object.
  Considers a 8-neighborhood. Changes the output image."
  [^ImageUInt8 input ^ImageUInt8 output]
  (BinaryImageOps/edge8 input output))

(defn edge8
  "Removes all pixels but ones which are on the edge of an object.
  Considers a 8-neighborhood."
  [input] (edge8! input nil))

#_(defn logic-and
  "Applies bit-and to the pixels of two BW images"
  [image1 image2]
  {:pre [(every? c/bw-type? [image1 image2])]}
  (let [result (c/new-image (c/ncols image1) (c/nrows image1) :bw)
        chn-result (c/get-channel result)]
    (BinaryImageOps/logicAnd (:mat image1) (:mat image2) chn-result)
    result))

#_(defn logic-or [image1 image2]
  {:pre [(every? c/bw-type? [image1 image2])]}
  (let [result (c/new-image (c/ncols image1) (c/nrows image1) :bw)
        chn-result (c/get-channel result)]
    (BinaryImageOps/logicOr (:mat image1) (:mat image2) chn-result)
    result))

#_(defn logic-xor [image1 image2]
  {:pre [(every? c/bw-type? [image1 image2])]}
  (let [result (c/new-image (c/ncols image1) (c/nrows image1) :bw)
        chn-result (c/get-channel result)]
    (BinaryImageOps/logicXor (:mat image1) (:mat image2) chn-result)
    result))

#_(defn erode [image rule]
  {:pre [(c/bw-type? image)
         (or (== 4 rule) (== 8 rule))]}
  (let [img-ch (:mat image)]
    (-> (if (== 4 rule)
          (BinaryImageOps/erode4 img-ch nil)
          (BinaryImageOps/erode8 img-ch nil))
        (c/make-image :bw))))

#_(defn dilate [image rule]
  {:pre [(c/bw-type? image)
         (or (== 4 rule) (== 8 rule))]}
  (let [img-ch (:mat image)]
    (-> (if (== 4 rule)
          (BinaryImageOps/dilate4 img-ch nil)
          (BinaryImageOps/dilate8 img-ch nil))
        (c/make-image :bw))))

#_(defn opening 
  "Morphological operation called opening, just a erosion followed by a dilation.
  http://en.wikipedia.org/wiki/Opening_(morphology)"
  [img rule]
  (-> (erode img rule) (dilate rule)))

#_(defn closing 
  "Morphological operation called closing, just a dilation followed by a erosion.
  http://en.wikipedia.org/wiki/Closing_(morphology)"
  [img rule]
  (-> (dilate img rule) (erode rule)))

#_(defn edge [image rule]
  {:pre [(c/bw-type? image)
         (or (= 4 rule) (= 8 rule))]}
  (let [result (c/new-image (c/ncols image) (c/nrows image) :bw)
        chn-result (c/get-channel result)]
    (if (= 4 rule)
      (BinaryImageOps/edge4 image chn-result)
      (BinaryImageOps/edge4 image chn-result))
    result))

#_(defn remove-point-noise
  [img]
  {:pre [(c/bw-type? img)]}
  (let [out (c/new-image img :bw)]
    (BinaryImageOps/removePointNoise (:mat img) (:mat out))
    out))

#_(defn contours
  "Returns the contours of a binary image, according to the 4-connected or
  8-connected rule."
  [img rule]
  {:pre [(= :bw (:type img))]}
  (BinaryImageOps/contour (:mat img) rule nil))

#_(defn get-external-contour
  "Returns a list of the external points of a Contour."
  [^Contour c]
  (.external c))

#_(defn get-internal-contour
  "Returns a list of lists of internal points of a Contour."
  [^Contour c]
  (.internal c))

#_(defn clusters 
  "Returns a list of clusters of a binary image, according to the 4-connected or
  8-connected rule. Each cluster is composed of a list of Point2D_I32."
  [img rule]
  {:pre [(= :bw (:type img))]}
  (let [^ImageSInt32 labels (ImageSInt32. (c/ncols img) (c/nrows img))
        contours (BinaryImageOps/contour (:mat img) rule labels)]
    (BinaryImageOps/labelToClusters labels (count contours) nil)))

;;(TODO) implement this function
;; (defn relabel [])

;;(TODO) implement this function
;; (defn label-to-binary [])

;;(TODO) implement this function
;; (defn label-to-cluster [])


#_(defn labeled-image
  "Returns a labeled image, i.e an Image with the features numbered "
  ^ImageSInt32 [img rule]
  {:pre [(= :bw (:type img))]}
  (let [result (ImageSInt32. (c/ncols img) (c/nrows img))]
    (BinaryImageOps/contour (:mat img) rule result)
    result))

#_(defn clusters-to-binary
  "Returns a binary image from a list of clusters or blobs."
  [blobs width height]
  (let [out (c/new-image height width :bw)]
    (BinaryImageOps/clusterToBinary blobs (:mat out))
    out))

#_(defn bufferedImage<-contours
  [contours & {:keys [color-internal color-external image width height]
               :or {color-internal 0x0000FF color-external 0xFF0000}}]
  {:pre [(or image (and width height))]}
  (let [[width height] (if (nil? image)
                         [width height]
                         [(c/ncols image) (c/nrows image)])]
    (VisualizeBinaryData/renderContours contours
                                        color-external 
                                        color-internal
                                        width height
                                        nil)))

;; (defn view-contours [contours img & opts]
;;   (h/view (apply bufferedImage<-contours contours :img img opts)))

#_(defn bufferedImage<-labeled-image
  "Renders a labeled image to a BufferedImage.
   Color-count indicates the amount of random colors to use"
  ([labeled-img]
     (bufferedImage<-labeled-image labeled-img (apply max (.data labeled-img))))
  ([labeled-img color-count]
     (VisualizeBinaryData/renderLabeled labeled-img color-count nil)))

;; (defn view-labeled-image
;;   ([labeled-img]
;;      (h/view (bufferedImage<-labeled-image labeled-img)))
;;   ([labeled-img color-count]
;;      (h/view (bufferedImage<-labeled-image labeled-img color-count))))




;;(TODO) implement tests and visualizations for the contours and
;;labeled
;;http://boofcv.org/index.php?title=Tutorial_Binary_Image

#_(defn render-binary
  "Returns a grayscale image where the 1s of the binary image are translated to 255.
  It is util to visualize the image."
  [img]
  {:pre [(= :bw (:type img))]}
  (let [ch (c/get-channel img 0)
        out (c/new-image (c/nrows img) (c/ncols img) :gray)
        out-m (c/get-channel out 0)]
    (c/for-xy [x y img]
      (if (zero? (c/get-pixel ch x y))
        (c/set-pixel! out-m x y 0)
        (c/set-pixel! out-m x y 255)))
    out))

#_(defn invert-pixels
  "Changes 1 to 0 and 0 to 1 of the given image, returning a new one."
  [img]
  {:pre [(= :bw (:type img))]}
  (let [out (c/new-image img :bw)
        orig (:mat img)
        dest (:mat out)]
    (c/for-xy [x y img]
      (when (zero? (c/get-pixel orig x y))
        (c/set-pixel! dest x y 1)))
    out))
