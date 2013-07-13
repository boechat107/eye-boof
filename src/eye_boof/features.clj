(ns eye-boof.features 
  (:import 
    [boofcv.struct PointIndex_I32]
    [boofcv.alg.filter.binary Contour]
    [boofcv.alg.feature.shapes ShapeFittingOps]
    [georegression.struct.point Point2D_I32]))

(defn pts-to-vec
  "Returns a sequence of vectors [x y] from the given list of Points."
  [pts]
  (map #(vector (.x %) (.y %)) pts))

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

(defn bounding-box
  "Returns two points [tl br], top-left and bottom-right, of the bounding box of the
  given list of points."
  [pts]
  (when (> (count pts) 2)
    (let [sy (sort-by #(.y %) pts)
          sx (sort-by #(.x %) pts)]
      [(Point2D_I32. (.x (first sx)) (.y (first sy)))
       (Point2D_I32. (.x (last sx)) (.y (last sy)))])))
