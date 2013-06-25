(ns eye-boof.binary-ops
  (:require [eye-boof
             [core :as c]
             [processing :as p]
             [image-features :as feats]])

  (:require [eye-boof
             [helpers :as h]])

  (:import
   [eye_boof.image_features ImageFeatures]
   [boofcv.alg.filter.binary BinaryImageOps]
   [boofcv.struct.image ImageSInt32]
   [boofcv.gui.binary VisualizeBinaryData]))



(defn contour
  "Returns a contour image, with the contours of the BW image features
   according to the 4-connected or 8-connected rule."
  [img rule]
  {:pre [(= :bw (:type img))]}
  (let [contours (BinaryImageOps/contour (:mat img) rule nil)]
    (ImageFeatures. contours :contours)))

(defrecord LabeledImage [mat])

(defn labeled-image
  "Returns a labeled image, i.e an Image with the features numbered "
  [img rule]
  {:pre [(= :bw (:type img))]}
  (let [result (ImageSInt32. (c/ncols img) (c/nrows img))]
    (BinaryImageOps/contour (:mat img) rule result)
    (LabeledImage. result)))

(defmethod h/to-buffered-image [ImageFeatures :contours]
  [blob & {:keys [color-internal color-external image width height]
           :or {color-internal 0x0000FF color-external 0xFF0000}}]
  (assert (or image (and width height))
          "I need an 'img' or 'width/height' to know the size for plotting contours")
  (let [[width height] (if (nil? image)
                         [width height]
                         [(c/ncols image) (c/nrows image)])]
  (VisualizeBinaryData/renderContours (feats/features blob) color-external color-internal width height nil)))

(defmethod h/to-buffered-image [LabeledImage nil]
  [blob & {:keys [colors]}]
  (let [labeled-img ^ImageSInt32 (:mat blob)
        colors (or colors (apply max (.data labeled-img)))]
    (VisualizeBinaryData/renderLabeled labeled-img colors nil)))

;;(TODO) implement tests and visualizations for the contours and
;;labeled
;;http://boofcv.org/index.php?title=Tutorial_Binary_Image