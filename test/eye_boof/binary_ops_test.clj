(ns eye-boof.test.binary-ops-test
  (:use [clojure.test]
        [eye-boof.resources :only (img-small-connected)])
  (:require [eye-boof
             [binary-ops :as bi]
             [core :as c]
             [helpers :as h]
             [features :as ft]])
  (:import [georegression.struct.point Point2D_I32]
           [boofcv.struct.image ImageSInt32]))

(deftest labeled-img-test
  (is (= (-> (bi/labeled-image img-small-connected 4) .data vec)
         [0 0 0 0 1 1 1 2 2 2 0 1 1 1 2 0 2 0 1 1 0 2 2 2 0 0 0 0 0 0 0 0 3 3 0 0 4 4 0 3 3 0 0 4 4 0 0 0 5])
      "Labeled image with 4-neighbours")
  ;;Perhaps this test is reduntant, but test it anyway =)
  (is (= (-> (bi/labeled-image img-small-connected 8) .data vec)
         [0 0 0 0 1 1 1 2 2 2 0 1 1 1 2 0 2 0 1 1 0 2 2 2 0 0 0 0 0 0 0 0 3 3 0 0 4 4 0 3 3 0 0 4 4 0 0 0 3])
      "Labeled image with 8-neighbours"))

(deftest contour-test
  (let [contours4 (bi/contours img-small-connected 4)]
    (is (= 5 (count contours4))
        "Contours4: correct amount of contours")
    (is (= 1 (count (keep #(-> % .internal seq) contours4)))
        "Contours4: correct amount of internal contours")
    (is (= 5 (count (keep #(-> % .external seq) contours4)))
        "Contours4: correct amount of external contours"))
  
  (let [contours8 (bi/contours img-small-connected 8)]
    (is (= 4 (count contours8))
        "Contours8: correct amount of contours")
    (is (= 1 (count (keep #(-> % .internal seq) contours8)))
        "Contours8: correct amount of internal contours")
    ;;Perhaps this test is reduntant, but test it anyway =)
    (is (= 4 (count (keep #(-> % .external seq) contours8)))
        "Contours8: correct amount of external contours")))


;;if you want to view some examples (very small for being debugable =)
#_(
   ;;8-connected labeled image
   (h/view (bi/bufferedImage<-labeled-image (bi/labeled-image img-small-connected 4)))
   ;;8-connected labeled image
   (h/view (bi/bufferedImage<-labeled-image (bi/labeled-image img-small-connected 8)))
   
   ;;4-connected contours
   (h/view (bi/bufferedImage<-contours (bi/contours img-small-connected 4) :image img-small-connected))
   ;;8-connected contours
   (h/view (bi/bufferedImage<-contours (bi/contours img-small-connected 8) :image img-small-connected))

   )

(def bin-img
  (let [img (c/new-image 5 5 :bw)
        ch (:mat img)]
    (c/set-pixel! ch 0 0 1)
    (c/set-pixel! ch 1 0 1)
    (c/set-pixel! ch 2 0 1)
    (c/set-pixel! ch 0 1 1)
    (c/set-pixel! ch 2 1 1)
    (c/set-pixel! ch 0 2 1)
    (c/set-pixel! ch 1 2 1)
    (c/set-pixel! ch 2 2 1)
    (c/set-pixel! ch 4 3 1)
    (c/set-pixel! ch 3 4 1)
    (c/set-pixel! ch 4 4 1)
    img))

(deftest clusters
  (let [clusters (sort-by count (bi/clusters bin-img 8))
        img-copy (bi/clusters-to-binary clusters 5 5)
        filt-img (bi/clusters-to-binary (filter #(< (count %) 4) clusters) 5 5)]
    (is (== 2 (count clusters)))
    (is (== 3 (count (first clusters))))
    (is (== 8 (count (second clusters))))
    (is (= (c/channel-to-vec bin-img 0) (c/channel-to-vec img-copy 0)))
    (is (= [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 1] 
           (c/channel-to-vec filt-img 0)))))
