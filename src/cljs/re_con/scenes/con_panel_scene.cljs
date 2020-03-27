(ns re-con.scenes.con-panel-scene
  (:require
    [re-frame.core :as re-frame]
    [babylonjs]
    [re-con.base :as base]
    [re-con.utils :as utils]
    [re-con.main-scene :as main-scene]))

(def panel)
(def panel2)
;; Note: panels is completly in the babylon layer, and not in re-frame/db at all.
;; We do maintain a rough analog of panels in re-frame/db @ board-game
(def panels (vector))
(def assetsManager)
; (def info-panel)
(def status-panel)
(def mesh-button)
;; constants
(def ^:const panel-width (* 2 base/scale-factor))
(def ^:const panel-height (* 2 base/scale-factor))
(def ^:const panel-depth (* 0.1 base/scale-factor))
(def ^:const panel-spacing (* 0.1 base/scale-factor))

(def ^:const panel-array-width 4)
(def ^:const panel-array-height 4)
(def ^:const panel-array-xc (* -1 (/ (* panel-array-width (+ panel-width panel-spacing)) 2)))
; (println "panel-array-xc=" panel-array-xc)
(def ^:const panel-array-yc 1)
(def ^:const panel-array-zc 6)

; (declare mesh-collider-handler)

(defn abc [msg]
  (println "hello from abc, msg is " msg))

(defn abc-2 [msg]
  (println "hello from abc-2, msg is " msg))

(defn mesh-selected [mesh]
  ; (println "con_panel_scene: the following mesh was selected" (.-name mesh))
  (re-frame/dispatch [:mesh-selected mesh]))
  ; (println "con-panel-scene: dispatched :mesh-selected, mesh=" mesh))
  ; (re-frame/dispatch [(:mesh-selected mesh) (utils/get-panel-index mesh)]))

(defn mesh-unselected [mesh]
  ; (println "con_panel_scene: the following mesh was unselected" (.-name mesh))
  (re-frame/dispatch [:mesh-unselected mesh]))

; (defn mesh-picked [picking-info]
;   (println "mesh-picked entered, picking-info=" picking-info))

(defn change-panel-material [panel-name mat]
  ; (println "change-panel-mat: panel-name=" panel-name)
  ; (set! (.-material (-> js/document (.getElementById panel-id))) mat)
  (set! (.-material (-> main-scene/scene (.getMeshByName panel-name))) mat))

(defn toggle-panel-material [db panel-name]
  (let [panel (-> main-scene/scene (.getMeshByName panel-name))
        mat-name (-> panel (.-material) (.-name))
        cell (nth (db :board-cells) (get base/panel-name-map (keyword panel-name)))]
    ; (cond (= mat-name "redMaterial") (set! (.-material panel) main-scene/imgMat))
    (if (= (get cell :status) :active)
      (cond (= mat-name "redMaterial") (set! (.-material panel) (get cell :front-mat))
        ; (= mat-name "imgMat")(set! (.-material panel) main-scene/redMaterial)
        (= (subs mat-name 0 10) "front-mat-")(set! (.-material panel) main-scene/redMaterial)))))

(defn show-panel-face [db panel-name]
  ; (println "show-panel-face: leftControllerGazeTrackerMesh=" (.-leftControllerGazeTrackerMesh main-scene/vrHelper))
  (let [panel (-> main-scene/scene (.getMeshByName panel-name))
        ; mat-name (-> panel (.-material) (.-name))
        cell (nth (db :board-cells) (get base/panel-name-map (keyword panel-name)))]
    (set! (.-material panel) (cell :front-mat))))

(defn front-texture-loaded [db task index]
    ; (println "cp-scene.front-texture-loaded: now setting texutre" task.texture " on index " index)
    ; (set! (.-diffuseTexture (get (nth (db :board-cells) index) :front-mat)) (js/BABYLON.Texture. task.texture)))
    (let [cell (nth (db :board-cells) index)
          front-mat (get cell :front-mat)]
      ; (set! (.-diffuseTexture (get cell :front-mat)) (js/BABYLON.Texture. task.texture))
      (set! (.-diffuseTexture front-mat) (js/BABYLON.Texture. task.texture))))
      ; (println "cell=" cell)
      ; (println "cell :front-mat=" (get cell :front-mat))
      ; (println "front-mat.name=" (-> front-mat  .-diffuseTexture .-name .-name))))
      ; (js->clj (-> e js/JSON.stringify js/JSON.parse))
      ; (println "diffuseTexture on cell=" (.-diffuseTexture (js->clj (-> (get cell :front-mat) js/JSON.stringify js/JSON.parse))))
      ; (println "diffuseTexture on cell=" (goog.object/get (get cell :front-mat) "diffuseTexture"))))
      ; (println "diffuseTexture on cell=" 7)))
    ; (set! (.-diffuseTexture (nth panels index))))

(defn rebus-texture-loaded [db task index]
  ; (println "cp-scene.rebus-texture-loaded: now setting rebus texutre" task.texture " on index " index)
  (let [cell (nth (db :board-cells) index)
        rebus-mat (get cell :rebus-mat)]
    ; (set! (.-diffuseTexture (get cell :rebus-mat)) (js/BABYLON.Texture. task.texture))
    (set! (.-diffuseTexture rebus-mat) (js/BABYLON.Texture. task.texture))))
    ; (println "diffuseTexture on cell=" (.-diffuseTexture (js->clj (get cell :rebus-mat))))
    ; (println "rebus-mat.name=" (-> rebus-mat  .-diffuseTexture .-name .-name))))

(defn load-img-cb [index]
  (fn [task]
    (println "cp-scene.load-img-cb: now setting texutre" task.texture " on index " index)
    (set! (.-diffuseTexture (nth panels index)))))

(defn load-front-imgs [db]
  ; (println "load-front-imgs: db=" db)
  ; (println "load-front-imgs: board=" (db :board-cells))
  ; (set! assetsManager (js/BABYLON.AssetsManager. main-scene/scene))
  (doseq [[i cell](map-indexed vector (db :board-cells))]
    ; (println "cell=" cell ",i=" i)
    ; (println "cell.front-img=" (get cell :front-img))
    ; (set! (.-onSuccess (.addTextureTask assetsManager "load-texture" (get cell :front-img))) (load-img-cb i)))
    (let [task (.addTextureTask assetsManager "load-texture" (get cell :front-img))]
      ; (set! task.onSuccess (load-img-cb i))
      (set! task.onSuccess (re-frame/dispatch [:front-texture-loaded task i]))))
  (println "now calling load")
  (.load assetsManager))

(defn load-rebus-imgs [db]
  (println "con_panel_scene.load-rebus-imgs: entered")
  (let [am (js/BABYLON.AssetsManager. main-scene/scene)]
    (doseq [[i cell](map-indexed vector (db :board-cells))]
      (let [row (quot i base/board-row-cnt)
            col (mod i base/board-row-cnt)
            task (.addTextureTask am "load-texture" (str "imgs/rebus_part/dont_beat_round_the_bush/" (get cell :rebus-img-stem) col "-" row ".png"))]
        (set! task.onSuccess (re-frame/dispatch [:rebus-texture-loaded task i]))))
    (println "now calling load on rebus imgs")
    (.load am)))

;; Note: this is read-only on rf-db.
(defn init-panels [db]
  (println "init-panels: entered")
  (loop [row-index (- panel-array-height 1)
         rows []]
      ; (println "row-index=" row-index)
      (if (neg? row-index)
        rows
        (recur (dec row-index) (conj rows
                                     (loop [col-index (- panel-array-width 1)
                                            row []]
                                       (if (neg? col-index)
                                         row
                                         (do
                                           (let  [ panel-num (+ (* row-index panel-array-height) col-index)
                                                  pnl (js/BABYLON.MeshBuilder.CreateBox. (str "panel-"  panel-num)
                                                                                         (js-obj
                                                                                          "height" panel-height
                                                                                          "width" panel-width
                                                                                          "depth" panel-depth
                                                                                          "material" main-scene/redMaterial
                                                                                                 main-scene/scene))]
                                             (set! (.-position pnl) (js/BABYLON.Vector3.
                                                                     (+ (* col-index (+ panel-width panel-spacing)) panel-array-xc)
                                                                     (+ (* -1 row-index (+ panel-height panel-spacing)) panel-array-yc (* (- base/board-row-cnt 1) panel-height))
                                                                     panel-array-zc))
                                             (set! (.-material pnl) main-scene/redMaterial)
                                             ;;vt-x add
                                             (-> pnl (.-onCollideObservable) (.add (fn [] (println "mesh collision"))))
                                             ;;vt-x end
                                             (recur (dec col-index) (conj row pnl)))))))))))

(defn init-status-panel []
  (let [scene main-scene/scene
        status-pnl-height (* panel-height 1.5)
        status-pnl-width (* panel-width 6)
        status-pnl-pixel-height 256
        status-pnl-pixel-width 1024
        status-pnl
        (js/BABYLON.MeshBuilder.CreateBox.
         "status-panel"
         (js-obj "height" status-pnl-height
                 "width" status-pnl-width
                 "depth" panel-depth)
         scene)
        dyn-texture
        (js/BABYLON.DynamicTexture.
         "status-panel-texture"
         (js-obj "height" status-pnl-pixel-height
                 "width" status-pnl-pixel-width)
                 ; "font" "96px")
         scene)
        ; font "bold 344px monospace"
        ; box-text (js/BABYLON.GUI.TextBlock "box-text" "block hello")
        box-text (js/BABYLON.GUI.TextBlock. "box-text" "block hello")
        status-pnl-mat (js/BABYLON.StandardMaterial. "status-panel-mat" scene)]
        ; stack (js/BABYLON.GUI.StackPanel.)]
    (set! (.-position status-pnl) (js/BABYLON.Vector3. -1 (* panel-array-height 2.75) (+ panel-array-zc 1)))
    (set! (.-material status-pnl) status-pnl-mat)
    (set! (.-scaling status-pnl) (js/BABYLON.Vector3. 1 1 0.1))
    (set! (.-diffuseTexture status-pnl-mat) dyn-texture)
    (set! status-panel status-pnl)
    ; (.drawText dyn-texture "hello" (/ status-pnl-pixel-width 2) (/ status-pnl-pixel-height 4) "200px green" "white" "blue" true true)
    (.drawText dyn-texture "match" 300 200 "200px green" "white" "blue" true true)))
    ; (.addControl stack box-text)))
    ; (.set! (.isVisible stack true))))

    ; var text1 = new BABYLON.GUI.TextBlock();
    ;   text1.text = "reset";
    ;   text1.color = "white";
    ;   text1.fontSize = 24;
    ;   button.content = text1));
; (defn init-3d-gui []
;   (set! info-panel (js/BABYLON.GUI.Button3D. "hello"))
;   (.addControl main-scene/gui-3d-manager info-panel)
;   (set! (-> info-panel .-position) (js/BABYLON.Vector3. 0 (* panel-array-height 2.75) (+ panel-array-zc 1)))
;   ; (set! (-> info-panel .-scaling .-x) 2)
;   ; (set! (-> info-panel .-scaling) (js/BABYLON.Vector3. 8 2 0.5))
;   (set! (-> info-panel .-scaling) (js/BABYLON.Vector3. 8 1 1))
;   (set! (-> info-panel .-contentScaleRatio) 4)
;   (let [mesh (js/BABYLON.MeshBuilder.CreateBox. "mesh-button" (js-obj "height" 2, "width" 4, "depth" 0.5))]
;     (set! (.-position mesh) (js/BABYLON.Vector3. -2 (* panel-array-height 2.75) (+ panel-array-zc 1))))
;     ; (set! (.-mesh info-panel) mesh))
;   (let [text (js/BABYLON.GUI.TextBlock.)]
;     (set! (.-text  text) "hello")
;     (set! (.-color text) "green")
;     ; (set! (.-fontSize text) 24)
;     (set! (.-fontSize text) 96)
;     (set! (.-content info-panel) text)))
;
; (defn init-3d-gui-2 []
;   (let [mesh (js/BABYLON.MeshBuilder.CreateBox. "mesh-button" (js-obj "height" 2, "width" 4, "depth" 0.5))]
;     (set! (.-position mesh) (js/BABYLON.Vector3. -2 (* panel-array-height 2.75) (+ panel-array-zc 1)))
;     (set! mesh-button (js/BABYLON.GUI.MeshButton3D. mesh))
;     (let [text (js/BABYLON.GUI.TextBlock.)]
;       (set! (.-text  text) "hello")
;       (set! (.-color text) "green")
;       (set! (.-content mesh) text))))
; (defn init-game-tiles []
;   (let [tile-height (* panel-height 1.0)
;         tile-width (* panel-width 1)
;         tile (js/BABYLON.MeshBuilder.CreateBox.
;               "game-tile"
;               (js-obj "height" tile-height
;                       "width" tile-width
;                       "depth" 0.1)
;               main-scene/scene)
;         rot (.-rotation tile)]
;     (set! (.-position tile) (js/BABYLON.Vector3. (- panel-width) 0  panel-width))
;     ; (set! (.-x (.-rotation tile)))
;     (set! (-> rot .-x) (+ (.-x rot) (* base/ONE-DEG 90)))))

;; read-only on db
(defn init-con-panel-scene [db]
  (when (not base/use-xr)
    (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
    (-> main-scene/vrHelper .-onSelectedMeshUnselected (.add mesh-unselected)))
  ; (-> main-scene/vrHelper .-onNewMeshPicked (.add mesh-picked))
  ; (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  (set! assetsManager (js/BABYLON.AssetsManager. main-scene/scene))
  (set! panels (init-panels db))
  ; (println "panels count=" (count panels)))
  ; (load-rebus-imgs db))
  (init-status-panel)
  ; (init-game-tiles)
  (re-frame/dispatch [:init-game-board]))
  ; (init-3d-gui))
  ; (init-3d-gui-2))

(defn show-panel-rebus [index mat]
  (println "show-panel-rebus: index=" index ",mat=" mat)
  (let [panel (-> main-scene/scene (.getMeshByName (str "panel-" index)))]
    (set! (.-material panel) mat)))

(defn show-full-rebus [db]
  (doseq [[i cell] (map-indexed vector (db :board-cells))]
    (show-panel-rebus i (cell :rebus-mat))))


(defn show-full-rebus-2 [db]
  (show-panel-rebus 6 (get (nth (db :board-cells) 6) :rebus-mat)))

(defn reset-panel [index]
  (set! (.-material (-> main-scene/scene (.getMeshByName (str "panel-" index)))) main-scene/redMaterial))

(defn update-status-panel [msg]
  (.drawText (-> status-panel .-material .-diffuseTexture) msg 100 200 "200px green" "white" "blue" true true))
