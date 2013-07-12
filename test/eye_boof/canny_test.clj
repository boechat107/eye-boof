(ns eye-boof.canny-test
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p]
    [eye-boof.binary-ops :as bi]
    [incanter.core :as ic])
  (:import
    [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageFloat32 MultiSpectral]
    [boofcv.alg.filter.blur BlurImageOps]
    [boofcv.alg.filter.derivative GradientSobel]
    [boofcv.alg.filter.binary ThresholdImageOps BinaryImageOps]
    [boofcv.factory.feature.detect.edge FactoryEdgeDetectors]
    [boofcv.alg.feature.detect.edge CannyEdge]
    [boofcv.gui.image ShowImages]
    [boofcv.gui.binary VisualizeBinaryData]
    [java.awt.geom AffineTransform]
    [java.awt.image BufferedImage AffineTransformOp]))

(def img (p/scale (h/load-file-image "test/black_plate.jpg")
                  0.2 0.2))

(defn canny-edge
  " http://boofcv.org/index.php?title=Example_Canny_Edge"
  [img blur-int thr-low thr-high]
  (let [img (if (> (c/dimension img) 1) (p/to-gray img) img)
        nr (c/nrows img)
        nc (c/ncols img)
        res (c/new-image (c/nrows img) (c/ncols img) (:type img))
        canny (FactoryEdgeDetectors/canny blur-int true true ImageUInt8 ImageSInt16)
        img-m (c/get-channel img 0) 
        res-m (c/get-channel res 0)
        _ (.process canny img-m thr-low thr-high res-m)
        edge (.getContours canny)
        contours (BinaryImageOps/contour res-m 8 nil)]
    (h/view* img (bi/render-binary res))
    (ShowImages/showWindow
      (VisualizeBinaryData/renderBinary res-m nil)
      "binary edges from canny")
    (ShowImages/showWindow
      (VisualizeBinaryData/renderContours edge nil nc nr nil)
      "canny trace graph")
    (ShowImages/showWindow
      (VisualizeBinaryData/renderExternal contours nil nc nr nil)
      "contour from canny binary")))

(canny-edge img 5 0.1 0.3)
