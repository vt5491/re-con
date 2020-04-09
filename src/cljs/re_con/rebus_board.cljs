;; TODO
;; This now mostly does rebus panel stuff.  It should eventually be named to 'rebus-board'
;; Just keep aware that prior name for a 'rebus-panel' was just 'panel'.  I'm trying to adapt
;; using 'rebus-panel' at least publically. Internally, or privately, it's still ok if this
;; namespace refers to 'panel'.
; (ns re-con.scenes.con-panel-scene)
(ns re-con.rebus-board
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
; (def status-panel)
(def mesh-button)
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

(defn mesh-selected [mesh]
  (re-frame/dispatch [:mesh-selected mesh]))

(defn mesh-unselected [mesh]
  (re-frame/dispatch [:mesh-unselected mesh]))

(defn change-panel-material [panel-name mat]
  (set! (.-material (-> main-scene/scene (.getMeshByName panel-name))) mat))

;; pretty sure this is defunct..no longer called
; (defn toggle-panel-material [db panel-name]
;   (println "toggle-panel-material entered")
;   (let [panel (-> main-scene/scene (.getMeshByName panel-name))
;         mat-name (-> panel (.-material) (.-name))
;         cell (nth (db :board-cells) (get base/panel-name-map (keyword panel-name)))]
;     (if (= (get cell :status) :active)
;       (cond (= mat-name "redMaterial") (set! (.-material panel) (get cell :front-mat))
;         (= (subs mat-name 0 10) "front-mat-")(set! (.-material panel) main-scene/redMaterial)))))

(defn show-panel-face [db panel-name]
  (prn "show-panel-face: panel-name=" panel-name)
  (let [panel (-> main-scene/scene (.getMeshByName panel-name))
        ; cell (nth (db :board-cells) (get base/panel-name-map (keyword panel-name)))
        cell (nth (db :rebus-board-cells) (get base/rebus-panel-name-map (keyword panel-name)))]
    (set! (.-material panel) (cell :front-mat))))

(defn front-texture-loaded [db task index]
    (let [cell (nth (db :rebus-board-cells) index)
          front-mat (get cell :front-mat)]
      (set! (.-diffuseTexture front-mat) (js/BABYLON.Texture. task.texture))))

(defn rebus-texture-loaded [db task index]
  (let [cell (nth (db :rebus-board-cells) index)
        rebus-mat (get cell :rebus-mat)]
    (set! (.-diffuseTexture rebus-mat) (js/BABYLON.Texture. task.texture))))

(defn load-img-cb [index]
  (fn [task]
    (println "rebus-board.load-img-cb: now setting texutre" task.texture " on index " index)
    (set! (.-diffuseTexture (nth panels index)))))

(defn load-front-imgs [db]
  (doseq [[i cell](map-indexed vector (db :rebus-board-cells))]
    (let [task (.addTextureTask assetsManager "load-texture" (get cell :front-img))]
      (set! task.onSuccess (re-frame/dispatch [:front-texture-loaded task i]))))
  (println "now calling load")
  (.load assetsManager))

(defn load-rebus-imgs [db]
  (println "rebus-board.load-rebus-imgs: entered")
  (let [am (js/BABYLON.AssetsManager. main-scene/scene)]
    (doseq [[i cell](map-indexed vector (db :rebus-board-cells))]
      (let [row (quot i base/board-row-cnt)
            col (mod i base/board-row-cnt)
            task (.addTextureTask am "load-texture" (str "imgs/rebus_part/dont_beat_round_the_bush/" (get cell :rebus-img-stem) col "-" row ".png"))]
        (set! task.onSuccess (re-frame/dispatch [:rebus-texture-loaded task i]))))
    (println "now calling load on rebus imgs")
    (.load am)))

(defn init-rebus-panel [row col]
  (let [panel-num (+ (* row base/game-row-cnt) col)
        panel (js/BABYLON.MeshBuilder.CreateBox.
               ; (str "panel-"  panel-num)
               (str "rebus-panel-"  panel-num)
               (js-obj
                "height" panel-height
                "width" panel-width
                "depth" panel-depth
                "material" main-scene/redMaterial
                main-scene/scene))
        width (+ panel-width panel-spacing)
        height (+ panel-height panel-spacing)]
    (set! (.-position panel) (js/BABYLON.Vector3.
                              (+ (* col width) panel-array-xc)
                              (+ (* -1 row height) panel-array-yc (* (- base/board-row-cnt 1) height))
                              panel-array-zc))
    (set! (.-material panel) main-scene/redMaterial)
    (-> panel (.-onCollideObservable) (.add (fn [] (println "mesh collision"))))))

(defn init-rebus-panels []
  (doall
    (for [x (range (* base/game-row-cnt base/game-col-cnt))
          :let [row (quot x base/game-row-cnt)
                col (mod x base/game-col-cnt)]]
      (do
        ; (prn row "," col)
        (init-rebus-panel row col)))))

;; TODO: Move into status-board
; (defn init-status-panel []
;   (let [scene main-scene/scene
;         status-pnl-height (* panel-height 1.5)
;         status-pnl-width (* panel-width 6)
;         status-pnl-pixel-height 256
;         status-pnl-pixel-width 1024
;         status-pnl
;         (js/BABYLON.MeshBuilder.CreateBox.
;          "status-panel"
;          (js-obj "height" status-pnl-height
;                  "width" status-pnl-width
;                  "depth" panel-depth)
;          scene)
;         dyn-texture
;         (js/BABYLON.DynamicTexture.
;          "status-panel-texture"
;          (js-obj "height" status-pnl-pixel-height
;                  "width" status-pnl-pixel-width)
;          scene)
;         box-text (js/BABYLON.GUI.TextBlock. "box-text" "block hello")
;         status-pnl-mat (js/BABYLON.StandardMaterial. "status-panel-mat" scene)]
;     (set! (.-position status-pnl) (js/BABYLON.Vector3. -1 (* panel-array-height 2.75) (+ panel-array-zc 1)))
;     (set! (.-material status-pnl) status-pnl-mat)
;     (set! (.-scaling status-pnl) (js/BABYLON.Vector3. 1 1 0.1))
;     (set! (.-diffuseTexture status-pnl-mat) dyn-texture)
;     (set! status-panel status-pnl)
;     (.drawText dyn-texture "match" 300 200 "200px green" "white" "blue" true true)))

;; read-only on db
; (defn init-con-panel-scene [db])
(defn init-rebus-board []
  (when (not base/use-xr)
    (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
    (-> main-scene/vrHelper .-onSelectedMeshUnselected (.add mesh-unselected)))
  (set! assetsManager (js/BABYLON.AssetsManager. main-scene/scene))
  (set! panels (init-rebus-panels)))
  ; (init-status-panel)
  ;; TODO: move into game
  ; (re-frame/dispatch [:init-game-board]))

(defn show-panel-rebus [index mat]
  (println "show-panel-rebus: index=" index ",mat=" mat)
  ; (let [panel (-> main-scene/scene (.getMeshByName (str "panel-" index)))])
  (let [panel (-> main-scene/scene (.getMeshByName (str "rebus-panel-" index)))]
    (set! (.-material panel) mat)))

(defn show-full-rebus [db]
  (doseq [[i cell] (map-indexed vector (db :rebus-board-cells))]
    (show-panel-rebus i (cell :rebus-mat))))


(defn show-full-rebus-2 [db]
  (show-panel-rebus 6 (get (nth (db :rebus-board-cells) 6) :rebus-mat)))

(defn reset-panel [index]
  ; (set! (.-material (-> main-scene/scene (.getMeshByName (str "panel-" index)))) main-scene/redMaterial)
  (set! (.-material (-> main-scene/scene (.getMeshByName (str "rebus-panel-" index)))) main-scene/redMaterial))

; (defn update-status-panel [msg]
;   (.drawText (-> status-panel .-material .-diffuseTexture) msg 100 200 "200px green" "white" "blue" true true))
