;; A place for global constants.  This namespace is meant to be included in every
;; other namespace
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
