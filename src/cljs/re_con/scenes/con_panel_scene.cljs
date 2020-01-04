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

(defn abc [msg]
  (println "hello from abc, msg is " msg))

(defn abc-2 [msg]
  (println "hello from abc-2, msg is " msg))

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
        mat-name (-> panel (.-material) (.-name))
        cell (nth (db :board-cells) (get base/panel-name-map (keyword panel-name)))]
    ; (cond (= mat-name "redMaterial") (set! (.-material panel) main-scene/imgMat))
    (if (= (get cell :status) :active)
      (cond (= mat-name "redMaterial") (set! (.-material panel) (get cell :front-mat))
        ; (= mat-name "imgMat")(set! (.-material panel) main-scene/redMaterial)
        (= (subs mat-name 0 10) "front-mat-")(set! (.-material panel) main-scene/redMaterial)))))

(defn front-texture-loaded [db task index]
    ; (println "cp-scene.front-texture-loaded: now setting texutre" task.texture " on index " index)
    ; (set! (.-diffuseTexture (get (nth (db :board-cells) index) :front-mat)) (js/BABYLON.Texture. task.texture)))
    (let [cell (nth (db :board-cells) index)
          front-mat (get cell :front-mat)]
      ; (set! (.-diffuseTexture (get cell :front-mat)) (js/BABYLON.Texture. task.texture))
      (set! (.-diffuseTexture front-mat) (js/BABYLON.Texture. task.texture))
      ; (println "cell=" cell)
      ; (println "cell :front-mat=" (get cell :front-mat))
      (println "front-mat.name=" (-> front-mat  .-diffuseTexture .-name .-name))))
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
                                             (recur (dec col-index) (conj row pnl)))))))))))



(defn init-con-panel-scene[db]
  (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  (-> main-scene/vrHelper .-onSelectedMeshUnselected (.add mesh-unselected))
  (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  (set! assetsManager (js/BABYLON.AssetsManager. main-scene/scene))
  (set! panels (init-panels db))
  (println "panels count=" (count panels)))
  ; (set! (-> js/BABYLON .-Tools .-fallbackTexture) "data:image/jpg;base64,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"))
  ; (load-rebus-imgs db))

(defn show-panel-rebus [index mat]
  (println "show-panel-rebus: index=" index ",mat=" mat)
  (let [panel (-> main-scene/scene (.getMeshByName (str "panel-" index)))]
    (set! (.-material panel) mat)))

; (defn show-panel-rebus [mat]
;   (let [panel (->)]))

(defn show-full-rebus [db]
  ; (show-panel-rebus 0 (db :rebus-mat 0)))
  ; (show-panel-rebus 5 (get (nth (db :board-cells) 5) :rebus-mat))
  ; (show-panel-rebus 5 (get (nth (db :board-cells) 5) :rebus-mat))
  ; (map #(show-panel-rebus % (% :rebus-mat)) (map-indexed vector (db :board-cells))))
  (doseq [[i cell] (map-indexed vector (db :board-cells))]
    (show-panel-rebus i (cell :rebus-mat))))
  ; (show-panel-rebus 5 main-scene/blueMaterial))


(defn show-full-rebus-2 [db]
  (show-panel-rebus 6 (get (nth (db :board-cells) 6) :rebus-mat)))
