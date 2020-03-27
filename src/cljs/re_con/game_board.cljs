;; This namespace handles all the 3-d mixamo models et al. that will appear
;; on the game tiles.

(ns re-con.game-board
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.main-scene :as main-scene]))

;; constants
(def ^:const tile-width (* 2 base/scale-factor))
(def ^:const tile-height (* 2 base/scale-factor))
(def ^:const tile-depth (* 0.1 base/scale-factor))
(def ^:const tile-spacing (* 0.5 base/scale-factor))

(defn mesh-loaded [new-meshes particle-systems skeletons picked-mesh]
; (defn mesh-loaded [new-meshes particle-systems skeletons]
  (println "new-meshes=" new-meshes)
  (println "count=" (count new-meshes))
  (println "mesh-loaded: picked-mesh=" picked-mesh)
  ; (doall (map #(set! (.-scaling %1) (js/BABYLON.Vector3. model-scale-factor model-scale-factor model-scale-factor)) new-meshes))
  (doall (map #(set! (.-scaling %1) (.scale (js/BABYLON.Vector3.One) base/mixamo-model-scale-factor)) new-meshes))
  (doall (map #(set! (.-position %1) (.-position picked-mesh)) new-meshes)))

(defn tile-selected [picked-mesh]
  (println "hi from tile-selected")
  (set! (.-material picked-mesh) main-scene/redMaterial)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb" main-scene/scene mesh-loaded)
  (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb" main-scene/scene
               #(mesh-loaded %1 %2 %3 picked-mesh)))
  ; (let [pm (atom 0)])
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb"
  ;              ; (fn [new-meshes particle-systems skeletons] (mesh-loaded new-meshes particle-systems skeletons))
  ;              (fn [new-meshes particle-systems skeletons] (println "fn.new-meshes=" new-meshes))))
  ; (let [pm 0]
  ;   (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb"
  ;                (fn [new-meshes particle-systems skeletons]
  ;                  ; (println "cb: picked-mesh=" pm)
  ;                  ; (mesh-loaded new-meshes particle-systems skeletons @pm)
  ;                  (mesh-loaded new-meshes particle-systems skeletons)))))

; (defn init-game-tiles []
;   (println "game-board.init-game-tiles: entered")
;   (let [
;         ; tile-height (* panel-height 1.0)
;         ; tile-width (* panel-width 1)
;         tile (js/BABYLON.MeshBuilder.CreateBox.
;               "game-tile"
;               (js-obj "height" tile-height
;                       "width" tile-width
;                       "depth" tile-depth)
;               main-scene/scene)
;         rot (.-rotation tile)]
;     (set! (.-position tile) (js/BABYLON.Vector3. (- tile-width) 0  tile-width))
;     (set! (-> rot .-x) (+ (.-x rot) (* base/ONE-DEG 90)))))

(defn init-game-tile [row col]
  (println "game-board.init-game-tile: row=" row ", col=" col)
  (let [
        tile (js/BABYLON.MeshBuilder.CreateBox.
              "game-tile"
              (js-obj "height" tile-height
                      "width" tile-width
                      "depth" tile-depth)
              main-scene/scene)
        rot (.-rotation tile)]
    ; (set! (.-position tile) (js/BABYLON.Vector3. (* (- tile-width) col) 0 (* tile-height row)))
    (set! (.-position tile) (js/BABYLON.Vector3. (* (+ tile-width tile-spacing) col) 0 (* (+ tile-height tile-spacing) row)))
    (set! (-> rot .-x) (+ (.-x rot) (* base/ONE-DEG 90)))))

(defn init-game-tiles []
  (println "game-board.init-game-tiles: entered")
  (init-game-tile 1 1)
  (init-game-tile 2 1))
