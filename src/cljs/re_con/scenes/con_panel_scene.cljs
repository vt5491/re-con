(ns re-con.scenes.con-panel-scene
  (:require
    [re-frame.core :as re-frame]
    [babylonjs]
    [re-con.base :as base]
    [re-con.main-scene :as main-scene]))

(def panel)
(def panel2)
(def panels (vector))
(def assetsManager)
;; constants
(def ^:const panel-width 2)
(def ^:const panel-height 2)
(def ^:const panel-depth 0.1)
(def ^:const panel-spacing 0.1)

(def ^:const panel-array-width 4)
(def ^:const panel-array-height 4)
(def ^:const panel-array-xc (* -1 (/ (* panel-array-width (+ panel-width panel-spacing)) 2)))
; (println "panel-array-xc=" panel-array-xc)
(def ^:const panel-array-yc 1)
(def ^:const panel-array-zc 6)

(defn abc []
  (println "hello from abc"))

(defn mesh-selected [mesh]
  ; (println "con_panel_scene: the following mesh was selected" (.-name mesh))
  (re-frame/dispatch [:mesh-selected mesh]))

(defn mesh-unselected [mesh]
  ; (println "con_panel_scene: the following mesh was unselected" (.-name mesh))
  (re-frame/dispatch [:mesh-unselected mesh]))

(defn change-panel-material [panel-name mat]
  ; (println "change-panel-mat: panel-name=" panel-name)
  ; (set! (.-material (-> js/document (.getElementById panel-id))) mat)
  (set! (.-material (-> main-scene/scene (.getMeshByName panel-name))) mat))

(defn toggle-panel-material [db panel-name]
  (let [panel (-> main-scene/scene (.getMeshByName panel-name))
        mat-name (-> panel (.-material) (.-name))]
    ; (cond (= mat-name "redMaterial") (set! (.-material panel) main-scene/imgMat))
    (cond (= mat-name "redMaterial") (set! (.-material panel) (get (nth (db :board) (get base/panel-name-map (keyword panel-name))) :front-mat))
          ; (= mat-name "imgMat")(set! (.-material panel) main-scene/redMaterial)
          (= (subs mat-name 0 4) "mat-")(set! (.-material panel) main-scene/redMaterial))))

(defn texture-loaded [db task index]
    (println "cp-scene.texture-loaded: now setting texutre" task.texture " on index " index)
    (set! (.-diffuseTexture (get (nth (db :board) index) :front-mat)) (js/BABYLON.Texture. task.texture)))
    ; (set! (.-diffuseTexture (nth panels index))))

(defn load-img-cb [index]
  (fn [task]
    (println "cp-scene.load-img-cb: now setting texutre" task.texture " on index " index)
    (set! (.-diffuseTexture (nth panels index)))))

(defn load-front-imgs [db]
  (println "load-front-imgs: db=" db)
  (println "load-front-imgs: board=" (db :board))
  (set! assetsManager (js/BABYLON.AssetsManager. main-scene/scene))
  (doseq [[i cell](map-indexed vector (db :board))]
    (println "cell=" cell ",i=" i)
    (println "cell.front-img=" (get cell :front-img))
    ; (set! (.-onSuccess (.addTextureTask assetsManager "load-texture" (get cell :front-img))) (load-img-cb i)))
    (let [task (.addTextureTask assetsManager "load-texture" (get cell :front-img))]
      ; (set! task.onSuccess (load-img-cb i))
      (set! task.onSuccess (re-frame/dispatch [:front-texture-loaded task i]))))
  (println "now calling load")
  (.load assetsManager))

(defn init-panels [db]
  (println "init-panels: entered")
  (loop [row-index (- panel-array-height 1)
         rows []]
      (println "row-index=" row-index)
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
                                             (recur (dec col-index) (conj row pnl)))))))))))



(defn init-panel-scene[db]
  (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  (-> main-scene/vrHelper .-onSelectedMeshUnselected (.add mesh-unselected))
  (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  (set! panels (init-panels db))
  (println "panels count=" (count panels)))
