(ns eye-boof.segmentation.otsu
  (:require 
    [hiphip.int :as hi :only [asum]]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn compute-threshold
  "Returns the threshold value that maximizes the between-class variance for a 
  given vector representing a histogram."
  [^ints hist-array]
  (let [hist-sum (long (hi/asum hist-array))
        hist-idx-sum (long (areduce ^ints hist-array i ret 0 
                                    (+ ret (* i (aget ^ints hist-array i)))))]
    (loop [th-idx 0, acc 0, i*acc 0.0, 
           max-var 0.0, th-max -1]
      (let [idx-val (aget ^ints hist-array th-idx)
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
