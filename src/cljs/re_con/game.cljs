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
   ; [re-con.scenes.con-panel-scene :as cp-scene]
   [re-con.rebus-board :as rebus-board]
   [re-con.game-board :as gb]))

; (def default-img-map
;   {:0 base/burj-al-arab-img, :1 base/smiley-img, :2 base/smiley-img, :3 base/smiley-img,
;    :4 base/smiley-img, :5 base/smiley-img, :6 base/smiley-img, :7 base/smiley-img,
;    :8 base/burj-al-arab-img, :9 base/smiley-img, :10, base/smiley-img, :11 base/smiley-img,
;    :12 base/smiley-img, :13 base/smiley-img, :14 base/smiley-img, :15 base/smiley-img})
;
; (def rebus-img-stem "dont_beat_round_the_bush-")
(def fps-update-thread)

(defn fps-update []
  (.drawText (-> main-scene/fps-pnl .-material .-diffuseTexture) (int (.getFps main-scene/engine)) 50 50 "60px green" "white" "blue" true true))

(defn render-loop []
  (if base/use-xr
    (ctrl-xr/tick)
    (controller/tick))
  ; (prn "fps=" (.getFps main-scene/engine))
  (.drawText (-> main-scene/fps-pnl .-material .-diffuseTexture) (int (.getFps main-scene/engine)) 50 50 "60px green" "white" "blue" true true)
  (.render main-scene/scene))

(defn init []
  (println "game.cljs: now in orig init")
  (main-scene/init) ;;vt-x
  (re-frame/dispatch [:rnd-board-seq])
  ; (re-frame/dispatch [:init-board-cells]) ;;vt-x
  (re-frame/dispatch [:load-grass "bg_scenes/hampton_court/" "hampton_court_v1.glb"])
  (re-frame/dispatch [:init-rebus-cells]) ;;vt-x
  ; (re-frame/dispatch [:init-board-status]) ;;vt-x
  (re-frame/dispatch [:init-status-board]) ;;vt-x
  ; (re-frame/dispatch [:init-con-panel-scene]) ;;vt-x
  (re-frame/dispatch [:init-rebus-board]) ;;vt-x
  (re-frame/dispatch [:init-game-board])
  ; (println "init.pre-render loop: camera.pos=" (.-position main-scene/camera))
  (re-frame/dispatch [:init-game-cells base/ybot-anim-many-tile-set])
  (re-frame/dispatch [:load-tile-set])
  ; (re-frame/dispatch [:load-power-slave-pyr])
  ; (re-frame/dispatch [:load-grass "models/grass/" "flowers.glb"])
  ; (re-frame/dispatch [:load-grass "models/grass/" "lawn_pbr_glb.glb"])
  (re-frame/dispatch [:load-grass "bg_scenes/hampton_court/" "hampton_court_v1.glb"])
  ; (re-frame/dispatch [:load-model "models/cube_wrap/" "cube_wrap.glb"
  ;                     (fn [nm ps sk]
  ;                       (let [mat (js/BABYLON.StandardMaterial. "cube_mat" main-scene/scene)
  ;                             diff-texture (js/BABYLON.Texture. "models/cube_wrap/cube_wrap_baked.png")
  ;                             color-text (js/BABYLON.Color3. 1 0 0)]
  ;                         (set! (.-diffuseTexture mat) diff-texture)
  ;                         (set! (.-uScale diff-texture) 1.0)
  ;                         (set! (.-vScale diff-texture) 1.0)
  ;                         ; (js-debugger)
  ;                         ; (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
  ;                         (set! (-> (get nm 1) .-material) mat)
  ;                         (set! (-> (get nm 1) .-position) (js/BABYLON.Vector3. 0 4 0))
  ;                         (set! (-> (get nm 1) .-rotation) (js/BABYLON.Vector3. (/ js/Math.PI 4.0) 0 0))
  ;                         (prn "hi from cb, nm[1]=" (get nm 1))))])
  ; (re-frame/dispatch [:load-grass "models/grass/" "lawn_baked_glb.glb"])
  (main-scene/run-scene render-loop) ;;vt-x
  (set! fps-update-thread (js/setInterval fps-update 100)))
  ; var t=setInterval(runFunction,1000));

; (defn init []
;   (println "game.cljs: now in new init"))
;   ; (test-scene-ecsy/init)
;   ; (main-scene/init-basic))
;   ; (main-scene/init-basic-ecsy))
