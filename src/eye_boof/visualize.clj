(ns eye-boof.visualize
  (:require 
    [eye-boof.core :as c :only [image?]]
    [eye-boof.helpers :as h]
    [seesaw.core :as w]))

(def ^:dynamic *visualize-properties*
  {:cols 6
   ;:scale-to-fit true
   })

(defmacro with-view-props
  "Redefines view properties, e.g.
      (v/with-view-props {:cols 1}
          ...)"
  [props & body]
  `(binding [*visualize-properties* (merge *visualize-properties* ~props)]
     ~@body))

(defn- new-frame
  "Creates a new frame for viewing the images."
  []
  (w/frame :title "Image Viewer" ))

(defonce frame (atom (new-frame)))

(defn view 
  "Shows the images on a grid-panel window. If each argument is composed of a
  collection like [img 'title'], a grid panel with title and its title is showed.
  Ex.:
  (view i1 i2 i3) or (view [i1 'str1'] [i2 'str2'] ['str3' i3])"
  [& imgs]
  (let [;; Creates a sequence of [image label], both as w/label widgets. 
        items (->> imgs
                   (map #(if (or (h/is-buffImg? %) (c/image? %)) [% ""] %))
                   (map #(let [[i l] (if (string? (first %)) (reverse %) %)]
                           (vector (w/label :text l) 
                                   (->> (if (h/is-buffImg? i)
                                          i (h/to-buffered-image i))
                                        (w/label :icon))))))
        grid (w/grid-panel
               :border 5
               :hgap 10 :vgap 10
               :columns (min (:cols *visualize-properties*) (count imgs))
               :items (map #(-> (w/vertical-panel :items %)
                                (w/scrollable))
                           items))]
    (doto @frame
      (.setContentPane grid)
      w/pack!
      w/show!)))

(defn view*
  "View the images in a new frame"
  [& imgs]
  (reset! frame (new-frame))
  (apply view imgs))

(defn view-new [& imgs]
  (println "'view-new' is depreacted, please start using 'view*'")
  (apply view* imgs))
