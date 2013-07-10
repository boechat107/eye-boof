(ns eye-boof.pyramid-test
  "Tests of the BoofCV's Pyramid operations, rescaling an image in different sizes
  (smaller than the original)."
  (:use clojure.test)
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p])
  (:import 
    [boofcv.factory.transform.pyramid FactoryPyramid]
    [boofcv.struct.pyramid PyramidDiscrete]
    [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageFloat32 MultiSpectral]
    [boofcv.gui.image ShowImages DiscretePyramidPanel]))

(def img-test (h/load-file-image "test/plate.jpg"))

(deftest pyramid
  (let [img-gray (p/rgb-to-gray img-test)
        py (FactoryPyramid/discreteGaussian 
             (into-array Integer/TYPE [1 2 4]) -1 2 true ImageUInt8)
        dpp (DiscretePyramidPanel.)]
    (.process py (:mat img-gray))
    (doto dpp
      (.setPyramid py)
      (.render))
    (ShowImages/showWindow dpp "Pyramid")
    (h/view (-> (.getLayer py 1)
                (c/make-image :gray)))))
