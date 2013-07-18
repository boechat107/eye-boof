(ns eye-boof.features 
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
  (when (> (count pts) 2)
    (let [sy (sort-by #(y %) pts)
          sx (sort-by #(x %) pts)]
      [(Point2D_I32. (get-feature pts min x)
                     (get-feature pts min y))
       (Point2D_I32. (get-feature pts max x)
                     (get-feature pts max y))])))

(defn aprox-area
  "Returns the area of a bounding box around the given list of Points."
  [pts]
  (when (> (count pts) 2)
    (let [[tl br] (bounding-box pts)]
      (* (- (x br) (x tl))
         (- (y br) (y tl))))))

(defn blob-width
  "Returns the width of a blob or cluster."
  [pts]
  (inc (- (get-feature pts max x) (get-feature pts min x))))

(defn blob-height
  "Returns the width of a blob or cluster."
  [pts]
  (inc (- (get-feature pts max y) (get-feature pts min y))))
