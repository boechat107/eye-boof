(ns eye-boof.cnh-test
  (:require 
    [clojure.test :refer :all]
    [eye-boof 
     [core :as c]
     [processing :as p]
     [helpers :as h]
     [visualize :as v]]
    [clojure.java.io :as io]
    )
  (:import 
    [java.io File]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(def images 
  (->> (file-seq (io/file "/home/andre/CETIP/CNH/images/"))
       (map #(.getAbsolutePath ^File %))
       (filter #(some-> (re-find #"\.\w+$" %) 
                        (clojure.string/lower-case)
                        (= ".jpg")))
       (map h/load-file-image)
       (map p/to-gray)
       (map #(p/scale % 0.4))))

(defn imgs-size
  []
  (let [[img & more] images]
    (every? #(and (== (c/ncols img) (c/ncols %))
                  (== (c/nrows img) (c/nrows %)))
            more)))

(defn threshold-tests
  [img-idx]
  {:pre [(< img-idx (count images))]}
  (let [img (nth images img-idx)]
    (v/view img (p/adative-square img 10 -20))))
