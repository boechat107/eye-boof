(ns eye-boof.segmentation.otsu)

(defn compute-threshold
  "Returns the threshold value that maximizes the between-class variance for a 
  given vector representing a histogram."
  [hist-vec]
  (let [hist-sum (reduce + hist-vec)
        n (count hist-vec)]
    (loop [th-idx 0, acc 0,
           i*acc 0, 
           max-var 0, th-max -1]
      (if (< th-idx n)
        (let [w1 (+ acc (hist-vec th-idx))
              w2 (- hist-sum w1)
              new-i*acc (* th-idx i*acc)
              mu1 (/ new-i*acc w1)
              mu2 (/ (- hist-sum new-i*acc) w2)
              diff-mu (- mu1 mu2)
              bvar (-> (* diff-mu diff-mu)
                       (* w1)
                       (* w2))]
          (recur (inc th-idx)
                 w1
                 new-i*acc 
                 (max bvar max-var)
                 (if (> bvar max-var) th-idx th-max)))
        th-max))))
