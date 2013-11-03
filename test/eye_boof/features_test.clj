(ns ss-ocr.test.features-test
  (:use [clojure.test]
        [eye-boof.resources :only [img-small-connected]]
        [eye-boof.features])
  (:require 
    [eye-boof.core :as eyec]
    [eye-boof.binary-ops :as bi :only [clusters]])
  (:import [georegression.struct.point Point2D_I32]))

(deftest feat-properties-test
  (let [feat (map #(Point2D_I32. %1 %2)
                  [2 3 4 5]
                  [1 2 3 1])]
    (is (= 2 (get-feature feat min x)) "Get feature min x")
    (is (= 5 (get-feature feat max x)) "Get feature max x")
    (is (= 1 (get-feature feat min y)) "Get feature min y")
    (is (= 3 (get-feature feat max y)) "Get feature max y")
    (is (= 4 (blob-width feat))  "Get feature width")
    (is (= 3 (blob-height feat)) "Get feature height")))

(deftest feat-properties-test
  (let [feat (map #(Point2D_I32. %1 %2)
                  [2 3 4 5]
                  [1 2 3 1])]
    (is (= 4 (blob-width feat))  "Get feature width")
    (is (= 3 (blob-height feat)) "Get feature height")))

(deftest connected-components
  (let [feats (extract-connected-features img-small-connected 4)]
    (is (= 5 (count feats)) "Extracting connected components with 4-rule")

    (is (= 1 (apply min (map blob-width (vals feats)))) "4-rule: Minimum feat width")
    (is (= 3 (apply max (map blob-width (vals feats)))) "4-rule: Maximum feat width")  

    (is (= 1 (apply min (map blob-height (vals feats)))) "4-rule: Minimum feat height")
    (is (= 3 (apply max (map blob-height (vals feats)))) "4-rule: Maximum feat height")
   )
  
  (is (= (count (extract-connected-features img-small-connected 4 :background-feature true))
         6) "Extracting connected components with 4-rule")
  
  (is (= (count (extract-connected-features img-small-connected 8))
         4) "Extracting connected components with 8-rule")
  (is (= (count (extract-connected-features img-small-connected 8 :background-feature true))
         5) "Extracting connected components with 8-rule"))

(deftest boxing-blob
  (let [blobs (bi/clusters img-small-connected 8)
        box1 [(make-2d-point 2 0) (make-2d-point 3 1)]
        box2 [(make-2d-point 2 0) (make-2d-point 4 1)]
        groups1 (group-by-box blobs 0)
        groups2 (group-by-box blobs 0 2)
        groups3 (group-by-box blobs 2 2)]
    (is (== 1 (count (keep #(pts-in-box? % box1) blobs))))
    (is (== 2 (count (keep #(pts-in-box? % box2) blobs))))
    (is (== 4 (count groups1)))
    (is (== 2 (count groups2)))
    (is (== 1 (count groups3)))))

(deftest set-feature-pixels!-test
  (let [img (eyec/into-bw 2 [0 0 1 1])
        ft1 (list (Point2D_I32. 0 0) (Point2D_I32. 1 0))
        ft2 (list (Point2D_I32. 0 1) (Point2D_I32. 1 1))]
    (set-feature-pixels-on-channel! ft1 (eyec/get-channel img) 1)
    (set-feature-pixels-on-channel! ft2 (eyec/get-channel img) 0)
    (is (= [1 1 0 0]
           (-> img :mat .data seq)))

    (set-feature-pixels-on-image! ft1 img 0)
    (set-feature-pixels-on-image! ft2 img 1)
    (is (= [0 0 1 1]
           (-> img :mat .data seq)))))