;; A place for global constants.  This namespace is meant to be included in every
;; other namespace
;; base is refer-to few, referred by many
(ns re-con.base
  (:require
   [re-frame.core :as re-frame]))

(def board-row-cnt 4)
(def board-col-cnt 4)

(def smiley-img "imgs/smiley_face.jpg")
(def burj-al-arab-img "imgs/burj_al_arab.jpg")

(def panel-name-map {
                     :panel-0 0, :panel-1 1, :panel-2 2, :panel-3 3
                     :panel-4 4, :panel-5 5, :panel-6 6, :panel-7 7
                     :panel-8 8, :panel-9 9, :panel-10 10, :panel-11 11
                     :panel-12 12, :panel-13 13, :panel-14 14, :panel-15 15})


(def default-img-map
  {:0 burj-al-arab-img, :1 smiley-img, :2 smiley-img, :3 smiley-img,
   :4 smiley-img, :5 smiley-img, :6 smiley-img, :7 smiley-img,
   :8 burj-al-arab-img, :9 smiley-img, :10, smiley-img, :11 smiley-img,
   :12 smiley-img, :13 smiley-img, :14 smiley-img, :15 smiley-img})

(def rebus-img-stem "dont_beat_round_the_bush-")
