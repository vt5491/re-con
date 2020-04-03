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
; (def ^:const tile-width (* 2 base/scale-factor))
; (def ^:const tile-height (* 2 base/scale-factor))
; (def ^:const tile-depth (* 0.1 base/scale-factor))
; (def ^:const tile-spacing (* 0.5 base/scale-factor))
(def ^:const tile-width 2)
(def ^:const tile-height 2)
(def ^:const tile-depth 0.1)
(def ^:const tile-spacing 0.5)

;; defunct
; (defn mixamo-model-loaded [new-meshes particle-systems skeletons picked-mesh]
; ; (defn mesh-loaded [new-meshes particle-systems skeletons]
;   (println "new-meshes=" new-meshes)
;   (println "count=" (count new-meshes))
;   (prn "skeleton count=" (count skeletons))
;   (println "mesh-loaded: picked-mesh=" picked-mesh)
;   ; (doall (map #(set! (.-scaling %1) (js/BABYLON.Vector3. model-scale-factor model-scale-factor model-scale-factor)) new-meshes))
;   (doall (map #(set! (.-scaling %1) (.scale (js/BABYLON.Vector3.One) base/mixamo-model-scale-factor)) new-meshes))
;   ; (doall (map #(set! (.-scaling %1) (.scale (js/BABYLON.Vector3.One) base/mixamo-model-scale-factor)) skeletons))
;   (doall (map #(set! (.-position %1) (.-position picked-mesh)) new-meshes))
;   ; (if (and (get new-mesh1 0)))
;   ; (when (and (get)))
;   (when-let [mesh0 (get new-meshes 0)]
;     (when (-> (.-id mesh0) (= "__root__"))
;       (set! (.-id mesh0) (str "__root__-" 2)))))
;   ; (when-let* [mesh0 (get new-meshes 0)])
;   ; (set! (.-id)))

(defn model-loaded [new-meshes particle-systems skeletons picked-mesh tile-num]
  (println "new-meshes=" new-meshes)
  (println "count-abc=" (count new-meshes))
  (prn "skeleton count=" (count skeletons))
  (println "mesh-loaded: picked-mesh=" picked-mesh)
  (println "available animations=" (.-animations (get skeletons 0)))
  ; (doall (map #(set! (.-scaling %1) (js/BABYLON.Vector3. model-scale-factor model-scale-factor model-scale-factor)) new-meshes))
  ;vt-x (doall (map #(set! (.-scaling %1) (.scale (js/BABYLON.Vector3.One) base/mixamo-model-scale-factor)) new-meshes))
  (doall (map #(set! (.-scaling %1) (js/BABYLON.Vector3.One))) new-meshes)
  ;; use the following on the original boxing
  ; (doall (map #(set! (.-scaling %1) (.scale (js/BABYLON.Vector3.One) base/mixamo-model-scale-factor)) skeletons))
  (doall (map #(set! (.-position %1) (.-position picked-mesh)) new-meshes))
  (prn "new-meshes at 0=" (get new-meshes 0))
  (when-let [mesh0 (get new-meshes 0)]
    (prn "id mesh0=" (.-id mesh0))
    (when (-> (.-id mesh0) (= "__root__"))
      (prn "model-loaded: setting _root_ id to " (str "__root__" tile-num))
      (set! (.-id mesh0) (str "__root__" tile-num)))))

(defn tile-selected [picked-mesh]
  (println "hi from tile-selected")
  (set! (.-material picked-mesh) main-scene/redMaterial)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb" main-scene/scene mesh-loaded)
  ;; works
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb" main-scene/scene
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_dribble/" "ybot_dribble.glb" main-scene/scene)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/boxing/" "ybot_boxing.glb" main-scene/scene)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/boxing2/" "boxing2.glb" main-scene/scene)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/many_anim/" "ybot_many_anim.glb" main-scene/scene)
  (let [tile-idx (js/parseInt (utils/get-panel-index picked-mesh "game-tile"))
        mod (mod tile-idx 3)]
    ; (if (even? tile-idx)
    (when (= mod 0)
      ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/many_anim/" "ybot_boxing.glb" main-scene/scene)
      (.ImportMesh js/BABYLON.SceneLoader "" "models/jasper/many_anim/" "jasper_boxing.glb" main-scene/scene
                   #(model-loaded %1 %2 %3 picked-mesh tile-idx)))
    (when (= mod 1)
      ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/many_anim/" "ybot_taunt.glb" main-scene/scene)
      (.ImportMesh js/BABYLON.SceneLoader "" "models/jasper/many_anim/" "jasper_taunt.glb" main-scene/scene
                   #(model-loaded %1 %2 %3 picked-mesh tile-idx)))
    (when (= mod 2)
      ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/many_anim/" "ybot_old_man_idle.glb" main-scene/scene)
      (.ImportMesh js/BABYLON.SceneLoader "" "models/jasper/many_anim/" "jasper_tpose.glb" main-scene/scene
                   #(model-loaded %1 %2 %3 picked-mesh tile-idx)))))

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
        ; col-adj (/ (* (+ tile-width tile-spacing) base/game-row-cnt) 2)
        width (+ tile-width tile-spacing)
        height (+ tile-height tile-spacing)
        ;;TODO col-ad not used
        ; col-adj (-> (+ tile-width tile-spacing) (* base/game-row-cnt) (/ 2))
        rot (.-rotation tile)]
    ; (set! (.-position tile) (js/BABYLON.Vector3. (* (- tile-width) col) 0 (* tile-height row)))
    ; (set! (.-position tile) (js/BABYLON.Vector3. (* (+ tile-width tile-spacing) col) 0 (* (+ tile-height tile-spacing) row)))
    ; (set! (.-id))
    (set! (.-position tile) (js/BABYLON.Vector3.
                             (-> width (* (dec col)) (- (/ width 2.0)))
                             0
                             ; (* (+ tile-height tile-spacing) row)
                             ; (-> height (* (dec row)) (- (/ height 2.0)))
                             (- (-> height (* (dec row)) (- (/ height 2.0))))))
    (set! (-> rot .-x) (+ (.-x rot) (* base/ONE-DEG 90)))))

(defn init-game-tiles []
  (println "game-board.init-game-tiles: entered")
  ; (doall (for [ x (range 16)]
  ;          (prn "x=" x)))
  ; (for [x (range 16)
  ;       :let [row (quot x 4)
  ;             col (mod x 4)]]
  ;   (init-game-tile row col)))
  (doall
    (for [x (range (* base/game-row-cnt base/game-col-cnt))
          :let [row (quot x base/game-row-cnt)
                col (mod x base/game-col-cnt)]]
      (do
        ; (prn row "," col)
        (init-game-tile row col))))
        ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb" main-scene/scene
        ;      #(model-loaded %1 %2 %3 picked-mesh x)))))

  (prn "exiting init-game-tiles"))
  ; (init-game-tile 0 0)
  ; (init-game-tile 0 1)
  ; (init-game-tile 0 2)
  ; (init-game-tile 0 3)
  ; (init-game-tile 1 0)
  ; (init-game-tile 2 1)
  ; (init-game-tile 3 3))

; (assoc db :board-cells (conj (:board-cells db) cell)))

; (defn init-game-cells [db model-coll row-cnt col-cnt])
(defn init-game-cells [db model-coll]
  (println "init-game-cells: db=" db)
  (println "init-game-cells: model-coll=" model-coll)
  (let [tmp
        (assoc db :game-cells
               ;; double the models in random coll. because each avatar will 2x in game-board
               (let [mdls (utils/randomize-seq (reduce #(conj %1 %2)  model-coll (seq model-coll)))]
                 ; (reduce #(conj %1 {:status nil :model (:path (nth mdls %2))}) [] (range (count mdls)))
                 (println "mdls=" mdls)
                 ; (reduce #(conj %1 {:status nil :model "abc"}) [] (range (count mdls)))
                 (reduce #(conj %1 {:status nil :path (:path (nth mdls %2))}) [] (range (count mdls)))))]
    (println "tmp=" tmp)
    tmp))
