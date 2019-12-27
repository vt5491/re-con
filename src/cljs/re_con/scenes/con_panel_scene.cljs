(ns re-con.scenes.con-panel-scene
  (:require
    [re-frame.core :as re-frame]
    [babylonjs]
    [re-con.main-scene :as main-scene]))

(def panel)
(def panel2)
(def panels (vector))
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

(defn toggle-panel-material [panel-name]
  (let [panel (-> main-scene/scene (.getMeshByName panel-name))
        mat-name (-> panel (.-material) (.-name))]
    (cond (= mat-name "redMaterial") (set! (.-material panel) main-scene/imgMat)
          (= mat-name "imgMat")(set! (.-material panel) main-scene/redMaterial))))

; (defn init-panels []
;   (println "init-panels: entered")
;   (doseq [row-index (range 0 panel-array-width)]
;     (do
;       (println "row-index=" row-index)
;       (let [row (vector)]
;         ; (println "row=" row)
;         (doseq [col-index (range 0 panel-array-height)]
;           (let  [ panel-num (+ (* row-index panel-array-width) col-index)
;                  panel (js/BABYLON.MeshBuilder.CreateBox. (str "panel-"  panel-num)
;                                                           (js-obj "height" panel-height
;                                                                   "width" panel-width
;                                                                   "depth" panel-depth
;                                                                   "material" main-scene/redMaterial
;                                                                   main-scene/scene))]
;             (set! (.-position panel) (js/BABYLON.Vector3.
;                                       (+ (* col-index (+ panel-width panel-spacing)) panel-array-xc)
;                                       (+ (* row-index (+ panel-height panel-spacing)) panel-array-yc)
;                                       panel-array-zc))
;             (set! (.-material panel) main-scene/redMaterial)
;             (concat row col-index)
;             ; (println "init-panels: row point 1=" row ",panel=" panel ",col-indx=" col-index)
;             (conj row panel)))
;         ; (println "init-panels: row point 2=" row)
;         (conj panels row)))))

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
                                                                     (+ (* row-index (+ panel-height panel-spacing)) panel-array-yc)
                                                                     panel-array-zc))
                                             (set! (.-material pnl) main-scene/redMaterial)
                                             (recur (dec col-index) (conj row pnl)))))))))))


; (defn tmp []
;   (println "tmp: entered")
;   (doseq [row-index (range 0 4)]
;     (println "row-index=" row-index)
;     (loop [a (vector)]
;       (conj a row-index)
;       (println "a=" a))))
;
; (defn tmp2 []
;   (loop [i 5
;          a (vector)]
;     (if (neg? i)
;       a
;       (do
;         ; (conj a i)
;         (println "tmp2: i=" i)
;         (recur (dec i) (conj a i))))))


(defn init-panel-scene[db]
  ; (set! panel (js/BABYLON.MeshBuilder.CreateBox. "panel"
  ;                                               (js-obj "height" 2 "width" 2 "depth" 0.1)
  ;                                               main-scene/scene))
  ; (set! (.-position panel) (js/BABYLON.Vector3. 10 2 2))
  (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  (-> main-scene/vrHelper .-onSelectedMeshUnselected (.add mesh-unselected))
  ; (-> main-scene/vrHelper .-onNewMeshSelected (.add (fn []
  ;                                                     (println "init-panel-scene: new mesh selected")
  ;                                                     (abc))))
  (-> main-scene/vrHelper .-onNewMeshSelected (.add mesh-selected))
  ; (set! (.-position panel)(js/BABYLON.Vector3. 0 2 5))
  ; (set! (.-material panel) main-scene/redMaterial)
  ; (set! panel2 (js/BABYLON.MeshBuilder.CreateBox. "panel2"
  ;                                               (js-obj "height" 2 "width" 2 "depth" 0.1)
  ;                                               main-scene/scene))
  ; (set! (.-position panel2)(js/BABYLON.Vector3. 2.5 2 5))
  ; (set! (.-material panel2) main-scene/redMaterial)
  ; (def xyz (tmp2))
  ; (println "xyz=" xyz)
  (set! panels (init-panels db))
  (println "panels count=" (count panels)))
