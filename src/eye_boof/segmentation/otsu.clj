(ns eye-boof.segmentation.otsu
  (:require [eye-boof.dev-tools :refer [do-loop for-loop]]
            [hiphip.int :as hi :only [asum]]
            [primitive-math :refer [use-primitive-operators 
                                    unuse-primitive-operators]]
            )
  (:import (boofcv.struct.image ImageUInt8)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(use-primitive-operators)

(defn compute-threshold
  "Returns the threshold value that maximizes the between-class variance for a 
  given vector representing a histogram."
  ^long [^ints hist-ar]
  (let [hist-sum (int (hi/asum hist-ar))
        hist-idx-sum (double 
                       (areduce hist-ar i
                                ret 0
                                (-> (aget hist-ar i) (* i) (+ ret))))]
    (loop [th-idx 0, acc 0, i*acc 0, 
           max-var 0.0, th-max -1]
      (let [idx-val (aget hist-ar th-idx)
            w1 (+ acc idx-val)
            w2 (- hist-sum w1)]
        (cond 
          ;; At the last possible th-idx, w1 should equal to hist-sum.
          (zero? w2) th-max
          (zero? w1) (recur (inc th-idx) w1 i*acc max-var th-max)
          :else
          (let [new-i*acc (+ i*acc (* th-idx idx-val))
                mu1 (/ (double new-i*acc) (double w1))
                mu2 (/ (- hist-idx-sum (double new-i*acc)) (double w2))
                diff-mu (- mu1 mu2)
                bvar (-> (* diff-mu diff-mu)
                         (* (double w1))
                         (* (double w2)))]
            (recur (inc th-idx)
                   w1
                   new-i*acc 
                   (max bvar max-var)
                   (if (> bvar max-var) th-idx th-max))))))))

(defn full-square-fn
  "Returns a function that returns the histogram of a square neighbor considering
  all its pixels."
  [^ImageUInt8 mat ^long radius]
  (fn full-square 
    ([^long x ^long y] (full-square x y 0))
    ([^long x ^long y ^long offset-y]
     (let [hist-ar (int-array 256)]
       (do-loop [i (- x radius) (<= i (+ x radius)) (inc i)]
                (do-loop [j (- y radius) (<= j (+ offset-y (+ y radius))) (inc j)]
                         (let [pix-val (.unsafe_get mat i j)]
                           (aset hist-ar pix-val (inc (aget hist-ar pix-val))))))
       hist-ar))))

(defmacro partial-square
  [[fix-sym xc, mov-sym yc] radius hist-ar & code]
  (let [hist-ar (vary-meta hist-ar assoc :tag 'ints)]
    `(do
       (do-loop [~mov-sym (- ~yc ~radius) (<= ~mov-sym (+ ~yc ~radius)) (inc ~mov-sym)]
                (let [~fix-sym (- ~xc (inc ~radius))
                      dec-val# ~@code
                      ~fix-sym (+ ~xc ~radius)
                      inc-val# ~@code]
                  (when (pos? dec-val#)
                    (aset ~hist-ar dec-val# (dec (aget ~hist-ar dec-val#))))
                  (aset ~hist-ar inc-val# (inc (aget ~hist-ar inc-val#)))))
       ~hist-ar)))

(defn local-threshold
  "Applies the Otsu method locally, pixel based. For each pixel, the threshold value
  depends on its square neighbor of (radius+1)x(radius+1) size."
  [^ImageUInt8 mat ^long radius]
  (let [fs (full-square-fn mat radius)
        w (.getWidth mat)
        h (.getHeight mat)
        out (ImageUInt8. w h)]
    (for-loop [y radius (< y (- h radius)) (inc y)] 
              [vhist (fs radius radius -1)]
      ;; Calculating the histogram for a pixel of the first image's column.
      (let [updated-vhist (partial-square [j y, i radius] radius vhist
                                          (if (neg? j) -1 (.unsafe_get mat i j)))]
        (.unsafe_set out radius y 
                     (if (< (.unsafe_get mat radius y)
                            (compute-threshold updated-vhist))
                       0 1))
        ;; Loop to calculate the pixels of the other columns of the same row.
        (for-loop [x (inc radius) (< x (- w radius)) (inc x)]
                  [xhist (aclone ^ints updated-vhist)]
          (let [updated-xhist (partial-square [i x, j y] radius xhist 
                                              (.unsafe_get mat i j))]
            (.unsafe_set out x y (if (< (.unsafe_get mat x y)
                                        (compute-threshold updated-xhist))
                                   0 1))
            updated-xhist))
        updated-vhist))
    out))
