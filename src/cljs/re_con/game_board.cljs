;; This namespace handles all the 3-d mixamo models et al. that will appear
;; on the game tiles.
;; Tiles are physical (babylon state) and cells are logical (re-frame or app) state.

(ns re-con.game-board
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.main-scene :as main-scene]))

;; constants
(def ^:const tile-width 2)
(def ^:const tile-height 2)
(def ^:const tile-depth 0.1)
(def ^:const tile-spacing 0.5)

(defn tile-selected [picked-mesh]
  (let [tile-n (js/parseInt (utils/get-panel-index picked-mesh "game-tile"))
        model (-> main-scene/scene (.getMeshByID (str "__root__" tile-n)))]
    (if (.isEnabled model)
      (do
        (prn "enableed->unenabled")
        (set! (.-material picked-mesh) main-scene/whiteMaterial)
        (.setEnabled model false))
      (do
        (prn "unenabled->enabled")
        (set! (.-material picked-mesh) main-scene/redMaterial)
        (.setEnabled model true)))
    (prn "model status=" (.isEnabled model))))

(defn reset-tile [index]
  (let [tile-mesh (-> main-scene/scene (.getMeshByName (str "game-tile-" index)))
        model (-> main-scene/scene (.getMeshByID (str "__root__" index)))]
    (set! (.-material tile-mesh) main-scene/whiteMaterial)
    (.setEnabled model false)))

(defn init-game-tile [row col]
  ; (println "game-board.init-game-tile: row=" row ", col=" col)
  (let [
        tile (js/BABYLON.MeshBuilder.CreateBox.
              ; "game-tile"
              ; (str "game-tile-" (-> (* row base/game-row-cnt) (+ col)))
              (str "game-tile-" (+ (* row base/game-row-cnt) col))
              (js-obj "height" tile-height
                      "width" tile-width
                      "depth" tile-depth)
              main-scene/scene)
        width (+ tile-width tile-spacing)
        height (+ tile-height tile-spacing)
        rot (.-rotation tile)]
    (set! (.-position tile) (js/BABYLON.Vector3.
                             (-> width (* (dec col)) (- (/ width 2.0)))
                             0
                             ; (* (+ tile-height tile-spacing) row)
                             ; (-> height (* (dec row)) (- (/ height 2.0)))
                             (- (-> height (* (dec row)) (- (/ height 2.0))))))
    (set! (-> rot .-x) (+ (.-x rot) (* base/ONE-DEG 90)))))

(defn init-game-tiles []
  (println "game-board.init-game-tiles: entered")
  (doall
    (for [x (range (* base/game-row-cnt base/game-col-cnt))
          :let [row (quot x base/game-row-cnt)
                col (mod x base/game-col-cnt)]]
      (do
        (init-game-tile row col))))

  (prn "exiting init-game-tiles"))

(defn grass-loaded [new-meshes particle-systems skeletons]
  (prn "grass-loaded: new-meshes=" new-meshes)
  (prn "count new-messhes=" (count new-meshes))
  ; (doall (map #(set! (.-position %1) (js/BABYLON.Vector3. 0 -0.1 0)) new-meshes))
  ; (js-debugger)
  (doall (map #(do
                (when (or (= (.-name %1) "linear_walkway") (re-matches #"^flower_bed_x.*" (.-name %1)))
                     (prn "grass: hi. name=" (.-name %1))
                     (let [mat (js/BABYLON.StandardMaterial. "tile_mat-x" main-scene/scene)
                           diff-text (js/BABYLON.Texture. "imgs/hampton_court/walkway_tile.jpg" main-scene/scene)]
                       (set! (.-diffuseTexture mat) diff-text)
                       (set! (.-uScale diff-text) 1.0)
                       (set! (.-vScale diff-text) 8.0)
                       (set! (-> %1 .-material) mat)))
                (when (re-matches #"^flower_bed_y.*" (.-name %1))
                     (prn "flower_bed_y: hi. name=" (.-name %1))
                     (let [mat (js/BABYLON.StandardMaterial. "tile_mat-y" main-scene/scene)
                           ; diff-text (js/BABYLON.Texture. "imgs/hampton_court/walkway_tile_rot.png" main-scene/scene)
                           diff-text (js/BABYLON.Texture. "imgs/hampton_court/daisy_head.png" main-scene/scene)]
                           ; diff-text (js/BABYLON.Texture. "imgs/hampton_court/walkway_tile.jpg" main-scene/scene)]
                       ; (set! (.-uAng (/ js/Math.PI 2.0)))
                       ; (set! (.-uoffset diff-text) 2.0)
                       ; (set! (.-voffset diff-text) 4.0)
                       ; (set! (.-uScale diff-text) 1.0)
                       ; (set! (.-vScale diff-text) 1.0)
                       (set! (.-diffuseTexture mat) diff-text)
                       (set! (-> %1 .-material) mat)))
                ;; daisy leaf
                (when (re-matches #"Daisy_1\.002_primitive1*" (.-name %1))
                  (prn "found daisy " (.-name %1))
                  (let [mat (js/BABYLON.StandardMaterial. "daisy_mat" main-scene/scene)
                        diff-text (js/BABYLON.Texture. "imgs/hampton_court/flower_leaf.000.png" main-scene/scene)]
                    (set! (.-diffuseTexture mat) diff-text)
                    (set! (-> %1 .-material) mat)))
                ;; daisy head
                (when (re-matches #"Daisy_1\.002_primitive2*" (.-name %1))
                  (prn "found daisy " (.-name %1))
                  (let [mat (js/BABYLON.StandardMaterial. "daisy_mat" main-scene/scene)
                        diff-text (js/BABYLON.Texture. "imgs/hampton_court/daisy_head.png" main-scene/scene)]
                    (set! (.-diffuseTexture mat) diff-text)
                    (set! (-> %1 .-material) mat)))
                (when (re-matches #"gameTile.*" (.-name %1))
                  (prn "found gameTile " (.-name %1))
                  (let [mat (js/BABYLON.StandardMaterial. "tile_mat" main-scene/scene)
                        diff-text (js/BABYLON.Texture. "imgs/hampton_court/daisy_head.png" main-scene/scene)]
                    (set! (.-diffuseTexture mat) diff-text)
                    (set! (.-uScale diff-text) 1.0)
                    (set! (.-vScale diff-text) 1.0)
                    ; (set! (.-uoffset diff-text) 10.0)
                    ; (set! (.-voffset diff-text) 4.0)
                    (set! (-> %1 .-material) mat))))
                ; (when (re-matches #"Daisy.*" (.-name %1))
                ;   (prn "found daisy")
                ;   (let [mat (js/BABYLON.StandardMaterial. "daisy_mat" main-scene/scene)
                ;         diff-text (js/BABYLON.Texture. "imgs/hampton_court/daisy_head.png" main-scene/scene)]
                ;     (set! (.-diffuseTexture mat) diff-text)
                ;     (set! (-> %1 .-material) mat))))
              new-meshes))
  (doall (map #(do
                 (set! (.-id %1) "grass")
                 (set! (.-name %1) "grass")) new-meshes)))
  ;                  (prn "grass: setting texture to ")))))

(defn load-grass [db path fn]
  (.ImportMesh js/BABYLON.SceneLoader ""
               path
               fn
               (:main-scene db)
               #(grass-loaded %1 %2 %3)))
