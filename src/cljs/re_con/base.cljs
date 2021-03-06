;; A place for global constants.  This namespace is meant to be included in every
;; other namespace
;; base is refer-to few, referred by many
(ns re-con.base
  (:require
   [re-frame.core :as re-frame]))

(def ONE-DEG (/ Math/PI 180.0))
(def board-row-cnt 4)
(def board-col-cnt 4)

; (def board-index-vec (ve))
(def smiley-img "imgs/smiley_face.jpg")
(def burj-al-arab-img "imgs/burj_al_arab.jpg")

(def panel-name-map {
                     :panel-0 0, :panel-1 1, :panel-2 2, :panel-3 3
                     :panel-4 4, :panel-5 5, :panel-6 6, :panel-7 7
                     :panel-8 8, :panel-9 9, :panel-10 10, :panel-11 11
                     :panel-12 12, :panel-13 13, :panel-14 14, :panel-15 15})

(def rebus-panel-name-map {
                           :rebus-panel-0 0, :rebus-panel-1 1, :rebus-panel-2 2, :rebus-panel-3 3
                           :rebus-panel-4 4, :rebus-panel-5 5, :rebus-panel-6 6, :rebus-panel-7 7
                           :rebus-panel-8 8, :rebus-panel-9 9, :rebus-panel-10 10, :rebus-panel-11 11
                           :rebus-panel-12 12, :rebus-panel-13 13, :rebus-panel-14 14, :rebus-panel-15 15})

(def hotel-imgs ["imgs/hotels/aria_sign_overview_2.jpg",
                 "imgs/hotels/burj_al_arab.jpg",
                 "imgs/hotels/encore_signature.png",
                 "imgs/hotels/luxor.png",
                 "imgs/hotels/mirage-overview.jpg",
                 "imgs/hotels/planet_hollywood_overview.jpg",
                 "imgs/hotels/power_slave.jpg",
                 "imgs/hotels/temple_pool.jpg"])

; (def default-img-map
;   {:0 burj-al-arab-img, :1 smiley-img, :2 smiley-img, :3 smiley-img,
;    :4 smiley-img, :5 smiley-img, :6 smiley-img, :7 smiley-img,
;    :8 burj-al-arab-img, :9 smiley-img, :10, smiley-img, :11 smiley-img,
;    :12 smiley-img, :13 smiley-img, :14 smiley-img, :15 smiley-img})

(def default-img-map
  {:0 (hotel-imgs 0), :1 (hotel-imgs 1), :2 (hotel-imgs 2), :3 (hotel-imgs 3),
   :4 (hotel-imgs 4), :5 (hotel-imgs 5), :6 (hotel-imgs 6), :7 (hotel-imgs 7),
   :8 (hotel-imgs 0), :9 (hotel-imgs 1), :10, (hotel-imgs 2), :11 (hotel-imgs 3),
   :12 (hotel-imgs 4), :13 (hotel-imgs 5), :14 (hotel-imgs 6), :15 (hotel-imgs 7)})

(def rebus-img-stem "dont_beat_round_the_bush-")

; (def use-xr false)
(def use-xr true)
(def scale-factor 1)
; (def scale-factor 100)
(def mixamo-model-scale-factor 0.01)
(def game-row-cnt 4)
(def game-col-cnt 4)

(def ybot-many-anim-imgs [
                          "imgs/ybot/many_anim/ybot_boxing.png"
                          "imgs/ybot/many_anim/ybot_roundhouse_kick.png"
                          "imgs/ybot/many_anim/ybot_golf_putt_victory.png"
                          "imgs/ybot/many_anim/ybot_old_man_idle.png"
                          "imgs/ybot/many_anim/ybot_samba_dancing.png"
                          "imgs/ybot/many_anim/ybot_searching_pockets.png"
                          "imgs/ybot/many_anim/ybot_sitting.png"
                          "imgs/ybot/many_anim/ybot_taunt.png"])

(def ybot-mixamo-models [{:path "models/ybot_boxing/" :file "ybot_boxing.glb"}
                         {:path "models/ybot_glb/" :file "ybot.glb"}])

;; game level tile-sets here
;; Note: has to be in the same order as 'ybot-many-anim-imgs'
;; Note 2: the ":anim-group-name" can be determined by calling (i.e. from a javascript debug console):
;; (-> main-scene/scene .-animationGroups)
;; and examing the name field in each animationGroup
;; For now, we'll just set this manually.  It may seem trivial to do, but some names are pascalCase
;; and others are snake_case etc.
;; TODO: figure out a way to get this name at runtime (?)

(def ybot-anim-many-tile-set [
                              {:path "models/ybot/many_anim/" :fn "ybot_boxing.glb" :anim-group-name "boxing"}
                              {:path "models/ybot/many_anim/" :fn "ybot_roundhouse_kick.glb" :anim-group-name "roundhouse_kick"}
                              {:path "models/ybot/many_anim/" :fn "ybot_golf_putt_victory.glb" :anim-group-name "golf_putt_victory"}
                              {:path "models/ybot/many_anim/" :fn "ybot_old_man_idle.glb" :anim-group-name "old_man_idle"}
                              {:path "models/ybot/many_anim/" :fn "ybot_samba_dancing.glb" :anim-group-name "sambaDancing"}
                              {:path "models/ybot/many_anim/" :fn "ybot_searching_pockets.glb" :anim-group-name "searchingPockets"}
                              {:path "models/ybot/many_anim/" :fn "ybot_sitting.glb" :anim-group-name "sitting"}
                              {:path "models/ybot/many_anim/" :fn "ybot_taunt.glb" :anim-group-name "taunt"}])
