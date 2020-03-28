;; game is refer to many, referred by few
(ns re-con.game
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.controller :as controller]
   [re-con.controller-xr :as ctrl-xr]
   [re-con.cell :as cell]
   [re-con.main-scene :as main-scene]
   [re-con.test-scene-ecsy :as test-scene-ecsy]
   [re-con.scenes.con-panel-scene :as cp-scene]
   [re-con.game-board :as gb]))

; (def default-img-map
;   {:0 base/burj-al-arab-img, :1 base/smiley-img, :2 base/smiley-img, :3 base/smiley-img,
;    :4 base/smiley-img, :5 base/smiley-img, :6 base/smiley-img, :7 base/smiley-img,
;    :8 base/burj-al-arab-img, :9 base/smiley-img, :10, base/smiley-img, :11 base/smiley-img,
;    :12 base/smiley-img, :13 base/smiley-img, :14 base/smiley-img, :15 base/smiley-img})
;
; (def rebus-img-stem "dont_beat_round_the_bush-")

(defn render-loop []
  ; (println "render loop")
  ; (::controller/tick)
  (if base/use-xr
    (ctrl-xr/tick)
    (controller/tick))
  (.render main-scene/scene))

(defn init []
  (println "game.cljs: now in orig init")
  (main-scene/init) ;;vt-x
  ; (main-scene/init-basic)
  ; (main-scene/init-basic-2))
  ; ; (cell/init-board base/con-row-cnt base/con-col-cnt default-img-map)
  (re-frame/dispatch [:init-board-cells]) ;;vt-x
  (re-frame/dispatch [:init-board-status]) ;;vt-x
  ; ; (doseq [i (range 1 5)] (println "tmp"))
  ; ; (cp-scene/init-panel-scene)
  (re-frame/dispatch [:init-con-panel-scene]) ;;vt-x
  (println "init.pre-render loop: camera.pos=" (.-position main-scene/camera))
  (re-frame/dispatch [:init-game-cells])
  ; (println "abc=" (gb/init-game-cells {} base/ybot-mixamo-models))
  (main-scene/run-scene render-loop)) ;;vt-x
  ; (main-scene/run-scene-2))

; (defn init []
;   (println "game.cljs: now in new init"))
;   ; (test-scene-ecsy/init)
;   ; (main-scene/init-basic))
;   ; (main-scene/init-basic-ecsy))
