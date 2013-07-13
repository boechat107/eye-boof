(ns eye-boof.canny-test
  (:use clojure.test)
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p]
    [eye-boof.binary-ops :as bi]
    [eye-boof.features :as ft]
    )
  (:import
    [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageFloat32 MultiSpectral]
    [boofcv.alg.filter.blur BlurImageOps]
    [boofcv.alg.filter.derivative GradientSobel]
    [boofcv.alg.filter.binary ThresholdImageOps BinaryImageOps]
    [boofcv.factory.feature.detect.edge FactoryEdgeDetectors]
    [boofcv.alg.feature.detect.edge CannyEdge]
    [boofcv.alg.feature.shapes ShapeFittingOps]
    [boofcv.gui.image ShowImages]
    [boofcv.gui.binary VisualizeBinaryData]
    [boofcv.gui.feature VisualizeShapes]
    [java.awt Color Graphics2D BasicStroke]
    [java.awt.geom AffineTransform]
    [java.awt.image BufferedImage AffineTransformOp]))

(def img (h/load-file-image "test/black_plate.jpg"))

(defn canny-edge
  " http://boofcv.org/index.php?title=Example_Canny_Edge"
  [img blur-int thr-low thr-high]
  (let [img (if (> (c/dimension img) 1) (p/to-gray img) img)
        nr (c/nrows img)
        nc (c/ncols img)
        res (c/new-image (c/nrows img) (c/ncols img) :bw)
        canny (FactoryEdgeDetectors/canny blur-int true true ImageUInt8 ImageSInt16)
        img-m (c/get-channel img 0) 
        res-m (c/get-channel res 0)
        _ (.process canny img-m thr-low thr-high res-m)
        edge (.getContours canny)
        contours (BinaryImageOps/contour res-m 8 nil)]
    (h/view* img res)
    (ShowImages/showWindow
      (VisualizeBinaryData/renderBinary res-m nil)
      "binary edges from canny")
    (ShowImages/showWindow
      (VisualizeBinaryData/renderContours edge nil nc nr nil)
      "canny trace graph")
    (ShowImages/showWindow
      (VisualizeBinaryData/renderExternal contours nil nc nr nil)
      "contour from canny binary")))

(deftest canny 
  (let [bin-edges (p/canny-edge img 5 0.1 0.3)
        nc (c/ncols img)
        nr (c/nrows img)]
    (h/view* bin-edges)
    (ShowImages/showWindow
      (VisualizeBinaryData/renderExternal 
        (BinaryImageOps/contour (:mat bin-edges) 8 nil) nil nc nr nil)
      "contour from canny binary")))

(deftest fitting-polygons
  (let [bin-edges (p/canny-edge img 5 0.1 0.3)
        nc (c/ncols img)
        nr (c/nrows img)
        buff (BufferedImage. nc nr (BufferedImage/TYPE_INT_RGB))
        g2 (.createGraphics buff)]
    (.setStroke g2 (BasicStroke. 2))
    (doseq [contour (bi/contour bin-edges 8)]
      (let [poly (ft/fit-polygon contour false 2 0.1 100)]
        (when (> (ft/aprox-area poly) 200)
          (.setColor g2 (Color. (rand-int 200) (rand-int 200) (rand-int 200)))
          (VisualizeShapes/drawPolygon poly true g2))))
    (h/view* bin-edges buff)))
