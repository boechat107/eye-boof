# eye-boof

[![Build Status](https://travis-ci.org/boechat107/eye-boof.svg?branch=develop)](https://travis-ci.org/boechat107/eye-boof)
[![codecov](https://codecov.io/gh/boechat107/eye-boof/branch/develop/graph/badge.svg)](https://codecov.io/gh/boechat107/eye-boof)
[![Clojars Project](https://img.shields.io/clojars/v/eye-boof.svg)](https://clojars.org/eye-boof)

**eye-boof** is a small library of image processing algorithms completely based
on [BoofCV](http://boofcv.org/). **eye-boof** aims to provide a much smaller set
of functionalities than BoofCV, focused on simplicity and the most general
algorithms. For applications beyond that, it's strongly recommended to use
BoofCV directly.

## Usage

Most of times we need to work with images that already exist, like files in the
internet. To load a file from a URL, we can write something like this:

```clojure
(require '[eye-boof.core :refer :all]
         '[eye-boof.visualization :refer [show-image]])

(def color-img (-> "http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg"
                   (java.net.URL.)
                   (load-image->planar-u8)))

;; Visualize the image:
(show-image color-img)

color-img
;; #object[boofcv.struct.image.Planar 0x2762ce07 "boofcv.struct.image.Planar@2762ce07"]
```
![color eye](http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg)

Note that a `Planar` data structure is returned, which is the common data structure
to store the pixel intensities of color images. To know how many color channels this
image holds, we can type

```clojure
(num-of-bands color-img)
;; 3
```

what probably means a RGB image. To take a grayscale version of the image, we can take one of the color bands, like the Red:

```clojure
(band! color-img 0)
;; #object[boofcv.struct.image.GrayU8 0x77ca00df "boofcv.struct.image.GrayU8@77ca00df"]
```

![red channel](http://i58.tinypic.com/slgrdj.png)

Other way of doing this is loading the image as a gray image (the intensities of
each channel are averaged together):

```clojure
(-> "http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg"
    (java.net.URL.)
    (load-image->gray-u8))
;; #object[boofcv.struct.image.GrayU8 0x77ca00df "boofcv.struct.image.GrayU8@77ca00df"]
```

To save an image into a disk file, we can use the function `save-image!`
from the `eye-boof.io` namespace. The string path of the file tells the format
representation of the image, like `jpg` or `png` for example. The supported
formats are the same supported by `javax.imageio.ImageIO`, the underlying class
to read/write image files.

```clojure
(save-image! color-img "color_eye.jpg")
;; nil
```

### More examples

Additional examples and documentation can be found in
the [wiki pages](https://github.com/boechat107/eye-boof/wiki).

## License

Copyright © 2013 Andre Boechat

Distributed under the Eclipse Public License, the same as Clojure.
