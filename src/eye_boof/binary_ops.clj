(ns eye-boof.binary-ops
  (:require
    [eye-boof 
     [core :as c]])
  (:require [eye-boof
             [helpers :as h]])
  (:import
   [boofcv.alg.filter.binary Contour BinaryImageOps]
   [boofcv.struct.image ImageSInt32]
   [boofcv.gui.binary VisualizeBinaryData]))


(defn logic-and
  "Applies bit-and to the pixels of two BW images"
  [image1 image2]
  {:pre [(every? c/bw-type? [image1 image2])]}
  (let [result (c/new-image (c/ncols image1) (c/nrows image1) :bw)
        chn-result (c/get-channel result)]
    (BinaryImageOps/logicAnd (:mat image1) (:mat image2) chn-result)
    result))

(defn logic-or [image1 image2]
  {:pre [(every? c/bw-type? [image1 image2])]}
  (let [result (c/new-image (c/ncols image1) (c/nrows image1) :bw)
        chn-result (c/get-channel result)]
    (BinaryImageOps/logicOr (:mat image1) (:mat image2) chn-result)
    result))

(defn logic-xor [image1 image2]
  {:pre [(every? c/bw-type? [image1 image2])]}
  (let [result (c/new-image (c/ncols image1) (c/nrows image1) :bw)
        chn-result (c/get-channel result)]
    (BinaryImageOps/logicXor (:mat image1) (:mat image2) chn-result)
    result))

(defn erode [image rule]
  {:pre [(c/bw-type? image)
         (or (= 4 rule) (= 8 rule))]}
  (let [result (c/new-image (c/ncols image) (c/nrows image) :bw)
        chn-result (c/get-channel result)]
    (if (= 4 rule)
      (BinaryImageOps/erode4 image chn-result)
      (BinaryImageOps/erode8 image chn-result))
    result))

(defn dilate [image rule]
  {:pre [(c/bw-type? image)
         (or (= 4 rule) (= 8 rule))]}
  (let [result (c/new-image (c/ncols image) (c/nrows image) :bw)
        chn-result (c/get-channel result)]
    (if (= 4 rule)
      (BinaryImageOps/dilate4 image chn-result)
      (BinaryImageOps/dilate8 image chn-result))
    result))

(defn edge [image rule]
  {:pre [(c/bw-type? image)
         (or (= 4 rule) (= 8 rule))]}
  (let [result (c/new-image (c/ncols image) (c/nrows image) :bw)
        chn-result (c/get-channel result)]
    (if (= 4 rule)
      (BinaryImageOps/edge4 image chn-result)
      (BinaryImageOps/edge4 image chn-result))
    result))

(defn remove-point-noise
  [img]
  {:pre [(c/bw-type? img)]}
  (let [out (c/new-image img :bw)]
    (BinaryImageOps/removePointNoise (:mat img) (:mat out))
    out))

(defn contours
  "Returns the contours of a binary image, according to the 4-connected or
  8-connected rule."
  [img rule]
  {:pre [(= :bw (:type img))]}
  (BinaryImageOps/contour (:mat img) rule nil))

(defn ext-pts-contour
  "Returns the list of external points Point2D_I32 of a binary blob."
  [^Contour contour]
  (.external contour))

;;(TODO) implement this function
;; (defn relabel [])

;;(TODO) implement this function
;; (defn label-to-binary [])

;;(TODO) implement this function
;; (defn label-to-cluster [])


(defn labeled-image
  "Returns a labeled image, i.e an Image with the features numbered "
  ^ImageSInt32 [img rule]
  {:pre [(= :bw (:type img))]}
  (let [result (ImageSInt32. (c/ncols img) (c/nrows img))]
    (BinaryImageOps/contour (:mat img) rule result)
    result))

(defn contours-to-binary
  "Returns a binary image from a list of contours or blobs. Similar to BoofCV
  clursterToBinary function."
  [blobs width height]
  (let [out (c/new-image height width :bw)]
    (BinaryImageOps/clusterToBinary blobs (:mat out))
    out))

(defn bufferedImage<-contours
  [contours & {:keys [color-internal color-external image width height]
               :or {color-internal 0x0000FF color-external 0xFF0000}}]
  {:pre [(or image (and width height))]}
  (let [[width height] (if (nil? image)
                         [width height]
                         [(c/ncols image) (c/nrows image)])]
    (VisualizeBinaryData/renderContours contours color-external color-internal width height nil)))

;; (defn view-contours [contours img & opts]
;;   (h/view (apply bufferedImage<-contours contours :img img opts)))

(defn bufferedImage<-labeled-image
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

(defn render-binary
  "Returns a grayscale image where the 1s of the binary image are translated to 255.
  It is util to visualize the image."
  [img]
  {:pre [(== 1 (c/dimension img))]}
  (let [ch (c/get-channel img 0)
        out (c/new-image (c/nrows img) (c/ncols img) (:type img))
        out-m (c/get-channel out 0)]
    (c/for-xy [x y img]
      (if (zero? (c/get-pixel ch x y))
        (c/set-pixel! out-m x y 0)
        (c/set-pixel! out-m x y 255)))
    out))
