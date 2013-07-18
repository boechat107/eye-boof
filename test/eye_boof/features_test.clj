(ns ss-ocr.test.features-test
  (:use [clojure.test]
        [eye-boof.resources :only [img-small-connected]]
        [eye-boof.features])
  (:require [eye-boof.core :as eyec])
  (:import [georegression.struct.point Point2D_I32]))

(deftest feat-properties-test
  (let [feat (map #(Point2D_I32. %1 %2)
                  [2 3 4 5]
                  [1 2 3 1])]
    (is (= 4 (feat/blob-width feat))  "Get feature width")
    (is (= 3 (feat/blob-height feat)) "Get feature height")))

#_(deftest connected-components
  (let [feats (feat/extract-connected-features img-small-connected 4)]
    (is (= 5 (count feats)) "Extracting connected components with 4-rule")

    (is (= 1 (apply min (map feat/width (vals feats)))) "4-rule: Minimum feat width")
    (is (= 3 (apply max (map feat/width (vals feats)))) "4-rule: Maximum feat width")  

    (is (= 1 (apply min (map feat/height (vals feats)))) "4-rule: Minimum feat height")
    (is (= 3 (apply max (map feat/height (vals feats)))) "4-rule: Maximum feat height")
   )
  
  (is (= (count (feat/extract-connected-features img-small-connected 4 :background-feature true))
         6) "Extracting connected components with 4-rule")
  
  (is (= (count (feat/extract-connected-features img-small-connected 8))
         4) "Extracting connected components with 8-rule")
  (is (= (count (feat/extract-connected-features img-small-connected 8 :background-feature true))
         5) "Extracting connected components with 8-rule"))