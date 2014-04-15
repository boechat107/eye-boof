# eye-boof

This is a Clojure wrapper for the image processing library [BoofCV](http://boofcv.org/).
Some algorithms, not implemented in BoofCV, are implemented in Clojure using the
BoofCV data structures.

## Installation

The newest version of the library, `[org.clojars.boechat107/eye-boof "1.0.0"]`, is
not compatible with the older versions. A lot of modifications and simplifications
were made! It is better to forget about the previous versions.

## Features

* We are trying to provide wrappers for most of algorithms of 
[BoofCV](http://boofcv.org/index.php?title=Manual).

## Usage

Most of times we need to work with images that already exist, such files in the
disk. So, to load a file from the disk, we write something like

```clojure
(require '[eye-boof.core :refer :all]
         '[eye-boof.io :refer :all]
         '[eye-boof.visualize :refer [view]])

(def color-img (-> "http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg"
                   (java.net.URL.)
                   (load-image)))

;; Visualize the image:
(view color-img)
 
 color-img
;; @@
;; =>
;;; #<MultiSpectral boofcv.struct.image.MultiSpectral@15a27b54>
;; <=
```
![color eye](http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg)

Note that a `MultiSpectral` data structure is returned, which is the common data structure
to store the pixels' intensities of color images. To know how many color channels this
image holds, we can type

```clojure
(nbands color-img)
;; @@
;; =>
;;; 3
;; <=
```

what probably means a RGB image. To take a grayscale version of the image, we can take one of the color bands, like the Red:

```clojure
(band color-img 0)
;; @@
;; =>
;;; #<ImageUInt8 boofcv.struct.image.ImageUInt8@6be86f52>
;; <=
```

![red channel](http://i58.tinypic.com/slgrdj.png)

Other way of doing this is taking the average of the RGB pixel values

```clojure
(average-band color-img)
;; @@
;; =>
;;; #<ImageUInt8 boofcv.struct.image.ImageUInt8@80ac97f>
;; <=
```

To save an image into a disk file, we can use the function `save-image!`
from the `eye-boof.io` namespace. The string path of the file tells the format
representation of the image, like `jpg` or `png` for example. The supported
formats are the same supported by `javax.imageio.ImageIO`, the underlying class
to read/write image files.

```clojure
(save-image! color-img "color_eye.jpg")
;; @@
;; =>
;;; true
;; <=
```

### Image segmentation

[Image segmentation](http://en.wikipedia.org/wiki/Image_segmentation) is tipically used to locate objects and boundaries (lines, curves, etc.) in images. The simplest technique to achieve image segmentation is called [thresholding](thresholding), which is applied on a grayscale image and turns it into a binary image. Taking the Red band of our `color-img`, we can try to segment the pupil from the rest of the eye using the following code:

```clojure
(require '[eye-boof.segmentation :refer [threshold]])

(-> (band color-img 0) (threshold :default 100))
;; @@
;; =>
;;; #<ImageUInt8 boofcv.struct.image.ImageUInt8@4dfd4ca9>
;; <=
```

The above code results in a binary image, where most of the pupil is represented by `0` pixels and the rest of the image by `1` pixels. But if we try to visualize this output we would see a completely dark picture, since `0`s and `1`s are rendered as very dark pixels. To better visualize the results, we could multiply all the pixels' values by `255`: 

```clojure
(require '[eye-boof.math :as m])

(-> (band color-img 0) (threshold :default 100) (m/* 255))
;; @@
;; =>
;;; true
;; <=
```

![black-white eye](http://i58.tinypic.com/105nxwk.png)

## License

Copyright © 2013 Andre Boechat

Distributed under the Eclipse Public License, the same as Clojure.
