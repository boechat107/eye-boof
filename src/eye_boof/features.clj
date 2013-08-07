(ns eye-boof.features 
  (:require [eye-boof
             [core :as eyec]
             [binary-ops :as binop]
             [matrices :as eyem]])
  (:import 
    [boofcv.struct PointIndex_I32]
    [boofcv.alg.filter.binary Contour]
    [boofcv.alg.feature.shapes ShapeFittingOps]
    [georegression.struct.point Point2D_I32]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defmacro pt-getter
  [pt xy]
  `(let [pt# ~(vary-meta pt assoc :tag 'georegression.struct.point.Point2D_I32)]
     (. pt# ~xy)))

(defmacro x
  [pt]
  `(pt-getter ~pt x))

(defmacro y 
  [pt]
  `(pt-getter ~pt y))

(defmacro get-feature [feature fn from]
  `(apply ~fn (map #(~from %) ~feature)))

(defn pts-to-vec
  "Returns a sequence of vectors [x y] from the given list of Points."
  [pts]
  (map #(vector (x %) (y %)) pts))

(defn make-2d-point
  [x y]
  (Point2D_I32. x y))

(defn fit-polygon
  "Returns a list of points PointIndex_I32 representing the vertexes of a fitted
  polygon on the given binary contour."
  ([^Contour contour] (fit-polygon contour true 2 0.1 100))
  ([^Contour contour loop? tol-dist tol-angle iters]
   (ShapeFittingOps/fitPolygon 
     (.external contour) loop? tol-dist tol-angle iters)))

(defn trapezoid?
  [pts]
  (= (count pts) 4))

;;(TODELETE)
;; ss-ocr.license-plate> (def asd (doall (repeatedly 1000000 #(rand-int 10000))))
;; #'ss-ocr.license-plate/asd
;; ss-ocr.license-plate> (time (last (sort asd)))
;; "Elapsed time: 2189.537332 msecs"
;; 9999
;; ss-ocr.license-plate> (time (apply max asd))
;; "Elapsed time: 207.637494 msecs"
;; 9999

(defn bounding-box
  "Returns two points [tl br], top-left and bottom-right, of the bounding box of the
  given list of points."
  [pts]
  (let [sy (sort-by #(y %) pts)
        sx (sort-by #(x %) pts)]
      [(make-2d-point (get-feature pts min x)
                      (get-feature pts min y))
       (make-2d-point (get-feature pts max x)
                      (get-feature pts max y))])))

(defn blob-width
  "Returns the width of a blob or cluster."
  [pts]
  (inc (- (get-feature pts max x) (get-feature pts min x))))

(defn blob-height
  "Returns the width of a blob or cluster."
  [pts]
  (inc (- (get-feature pts max y) (get-feature pts min y))))

(defn aprox-area
  "Returns the area of a bounding box around the given list of Points."
  [pts]
  (when (> (count pts) 2)
    (* (blob-width pts)
       (blob-height pts))))

(defn pts-in-box?
  "Returns true if some point of the blob is inside the box [top-left bottom-right]."
  [pts [tl br]]
  (some #(and (>= (x %) (x tl)) (<= (x %) (x br))
              (>= (y %) (y tl)) (<= (y %) (y br))) 
        pts))

(defn group-by-box
  "Groups close blobs in groups and returns a sequence of groups of blobs.
  Two blobs belong to the same group with theirs bounding box overlap."
  ([blobs tol] (group-by-box blobs tol tol))
  ([blobs ^long tol-x ^long tol-y]
   (letfn [(make-tl-pt [x y]
             ;; Make a top-left bounding-box point adding the tolerance.
             (make-2d-point (- x tol-x) (- y tol-y)))
           (make-br-pt [x y]
             ;; Make a bottom-right bounding-box point adding the tolerance.
             (make-2d-point (+ x tol-x) (+ y tol-y)))
           (group [ung-blobs ung-visited cur-group cur-bb groups]
             ;; Recursive function to group blobs.
             (let [b1 (first ung-blobs)
                   r (rest ung-blobs)]
               (cond 
                 ;; There is no ungrouped and not visited blobs, neither ungrouped
                 ;; and visited blobs.  
                 (and (empty? ung-visited) (nil? b1))
                 (if cur-group (cons cur-group groups) groups)
                 ;; All ungrouped blobs were visited, but some of them remain
                 ;; ungrouped.
                 (nil? b1) 
                 (let [b (first ung-visited)]
                   (recur (rest ung-visited) 
                          nil
                          (cons b nil)
                          (let [[tl br] (bounding-box b)]
                            [(make-tl-pt (x tl) (y tl))
                             (make-br-pt (x br) (y br))])
                          (if cur-group (cons cur-group groups) groups)))
                 ;; An ungrouped-not-visited blob is inside the current bounding box,
                 ;; so it should be added to the current group and the current
                 ;; bounding box should be recalculated.
                 (pts-in-box? b1 cur-bb)
                 (recur r
                        ung-visited
                        (cons b1 cur-group)
                        (let [[b1-tl b1-br] (bounding-box b1)
                              [tl br] cur-bb]
                          [(make-tl-pt (min (x tl) (x b1-tl)) (min (y tl) (y b1-tl)))
                           (make-br-pt (max (x br) (x b1-br)) (max (y br) (y b1-br)))])
                        groups)
                 ;; An ungrouped-not-visited blob is not inside the bounding box and
                 ;; becomes an ungrouped-visited blob.
                 :else
                 (recur r (cons b1 ung-visited) cur-group cur-bb groups))))]
     (group
       nil 
       (->> blobs 
            (map #(vector (bounding-box %) %))
            (sort-by #(x (first (first %))))
            (map second))
       nil nil nil))))

(defn extract-connected-features
  "Extracts the connected features from a bw-img.
   Optionally extract the background as a feature
   => (feat/extract-connected-features img 4)
   ;=> { 1 (Point1, Point2 ...)
         2 (PointN, PointN+1 ...)
         ...{

   => (feat/extract-connected-features img 4 :background-feature true)
   ;=> { 0 (Point1, Point2 ...)
         1 (PointN, PointN+1 ...)
         ...{ "
  [bw-img rule & {:keys [background-feature]}]
  (let [labeled-img (binop/labeled-image bw-img rule)]
    (persistent!(reduce (fn [result [x y]]
                          (let [label (eyem/get-pixel :sint32 labeled-img x y)]
                 (if (or (not= 0 label) background-feature)
                   (assoc! result label
                           (conj (get result label []) (Point2D_I32. x y)))
                   result)))
                        (transient {})
                        (for [x (range (eyec/ncols bw-img))
                              y (range (eyec/nrows bw-img)) ]
                          [x y])))))


(defn crop [feat]
  (let [x-offset (get-feature feat min x)
        y-offset (get-feature feat min y)]
    (map #(make-2d-point (- (x %) x-offset)
                         (- (y %) y-offset))
         feat)))

(defn to-image
  "Extract a 'feat' from an image and draws on a new cropped image"
  ([img feat]
     (let [feat (crop feat)
           img-output (eyec/new-image (blob-height feat)
                                      (blob-width feat)
                                      (eyec/get-type img))]
       (dotimes [c-chn (eyec/dimension img)]
         (let [chn-input (eyec/get-channel img c-chn)
               chn-output (eyec/get-channel img-output c-chn)]
           (doseq [pt feat]
             (eyec/set-pixel!* chn-output
                               (x pt)
                               (y pt)
                               (eyec/get-pixel chn-input (x pt) (y pt))))))
       img-output))
  ([feat]
     (let [feat (crop feat)
           img-output (eyec/new-image (blob-height feat)
                                      (blob-width feat)
                                      :bw)
           chn-output (eyec/get-channel img-output)]
       (doseq [pt feat]
         (eyec/set-pixel!* chn-output
                           (x pt)
                           (y pt)
                           1))
       img-output)))

(defn rectangle-as-feature
  "Given a starting point (x0,y0) returns a rectangle as features with 'width' and 'height'"
  [[x0 y0 w h]]
  (for [x (range x0 (+ x0 w))
        y (range y0 (+ y0 h))]
    (make-2d-point x y)))
