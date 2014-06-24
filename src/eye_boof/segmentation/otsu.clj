(ns eye-boof.segmentation.otsu
  (:require [eye-boof.dev-tools :refer [do-loop for-loop]])
  (:import (boofcv.struct.image ImageUInt8)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn compute-threshold
  "Returns the threshold value that maximizes the between-class variance for a 
  given vector representing a histogram."
  ^long [hist-vec]
  (let [hist-sum (long (reduce + hist-vec))
        hist-idx-sum (->> (map * hist-vec (range (count hist-vec)))
                          (reduce +)
                          long)]
    (loop [th-idx 0, acc 0, i*acc 0.0, 
           max-var 0.0, th-max -1]
      (let [idx-val (long (hist-vec th-idx))
            w1 (+ acc idx-val)
            w2 (- hist-sum w1)]
        (cond 
          ;; At the last possible th-idx, w1 should equal to hist-sum.
          (zero? w2) th-max
          (zero? w1) (recur (inc th-idx) w1 i*acc max-var th-max)
          :else
          (let [new-i*acc (+ i*acc (* th-idx idx-val))
                mu1 (/ new-i*acc w1)
                mu2 (/ (- hist-idx-sum new-i*acc) w2)
                diff-mu (- mu1 mu2)
                bvar (-> (* diff-mu diff-mu)
                         (* w1)
                         (* w2))]
            (recur (inc th-idx)
                   w1
                   new-i*acc 
                   (max bvar max-var)
                   (if (> bvar max-var) th-idx th-max))))))))

(defn full-square-fn
  "Returns a function that calculates the histogram of a square neighbor considering
  all its pixels."
  [^ImageUInt8 mat ^long radius]
  (fn full-square! [^longs hist-ar ^long x ^long y]
    (do-loop [i (- x radius) (<= i (+ x radius)) (inc i)]
             (do-loop [j (- y radius) (<= j (+ y radius)) (inc j)]
                      (let [pix-val (.unsafe_get mat i j)]
                        (aset hist-ar pix-val (inc (aget hist-ar pix-val))))))
    hist-ar))

(defmacro partial-square
  [[fix-axis x, mov-axis y] radius hist-ar & code]
  (let [hist-ar (vary-meta hist-ar assoc :tag 'longs)]
    `(do
       (do-loop [~mov-axis (- ~y ~radius) (<= ~y (+ ~y ~radius)) (inc ~y)]
                (let [~fix-axis (- ~x ~radius)
                      dec-val# ~@code
                      ~fix-axis (+ ~x ~radius)
                      inc-val# ~@code]
                  (aset ~hist-ar dec-val# (dec (aget ~hist-ar dec-val#)))
                  (aset ~hist-ar inc-val# (inc (aget ~hist-ar inc-val#)))))
       ~hist-ar)))

(defn local-threshold
  "Applies the Otsu method locally, pixel based. For each pixel, the threshold value
  depends on its square neighbor of (radius+1)x(radius+1) size."
  [^ImageUInt8 mat ^long radius]
  (let [fs! (full-square-fn mat radius)
        w (.getWidth mat)
        h (.getHeight mat)
        out (ImageUInt8. w h)]
    (for-loop [y radius (<= y (- h radius)) (inc y)] 
              [vhist (fs! (long-array 256) radius radius)]
      ;; Calculating the histogram for a pixel of the first image's column.
      (let [updated-vhist (partial-square [i radius, j y] radius vhist
                                          (.unsafe_get mat i j))]
        (.unsafe_set out radius y 
                     (if (< (.unsafe_get mat radius y)
                            (compute-threshold (vec updated-vhist)))
                       0 1))
        ;; Loop to calculate the pixels of the other columns of the same row.
        (for-loop [x (inc radius) (<= x (- w radius)) (inc x)]
                  [xhist (aclone ^longs vhist)]
          (let [updated-xhist (partial-square [j y, i x] radius xhist 
                                              (.unsafe_get mat i j))]
            (.unsafe_set out x y (if (< (.unsafe_get mat x y)
                                        (compute-threshold (vec updated-xhist)))
                                   0 1))
            updated-xhist))
        updated-vhist))
    out))
