;; Note: do not confuse status-board with rebus-board.  Status-board is the panel at top
;; that says "match" "not match" etc.  However, the main cell state pick logic happens to
;; be in here (it would probably be more appropriate it for it to be in rebus-board, since
;; that's what I mainly think of as the primary state context).  As a result, it kind is
;; kind of overloaded with additional duties.

(ns re-con.status-board
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.cell :as cell]
   [re-con.main-scene :as main-scene]
   [re-con.rebus-board :as rebus-board]))

(def status-panel)
;; constants
(def ^:const panel-width 2)
(def ^:const panel-height 2)
(def ^:const panel-depth 0.1)
(def ^:const panel-spacing 0.1)
(def ^:const panel-array-width 4)
(def ^:const panel-array-height 4)
(def ^:const panel-array-xc (* -1 (/ (* panel-array-width (+ panel-width panel-spacing)) 2)))
(def ^:const panel-array-yc 1)
(def ^:const panel-array-zc 6)

(defn init-board-status [db]
  (assoc db :board-status {:first-pick-index nil, :first-pick nil, :second-pick-index nil, :second-pick nil}))

(defn update-status-panel [msg]
  (.drawText (-> status-panel .-material .-diffuseTexture) msg 100 200 "200px green" "white" "blue" true true))

;; basically a giant state-machine
(defn cell-picked "maintain :first-pick and :second-pick status" [db selected-mesh]
  (let [first-pick (get-in db [:board-status :first-pick])
        second-pick (get-in db [:board-status :second-pick])
        stem "rebus-panel"
        selected-mesh-index (utils/get-panel-index selected-mesh stem)]
    (if (not first-pick)
      (do
        (update-status-panel "first pick")
        ;; first pick
        (assoc-in (assoc-in db [:board-status :first-pick] selected-mesh) [:board-status :second-pick] nil))
      (do
        (println "second-pick path")
        (println "second-pick path, first-pick.name=" (.-name first-pick) ",selected-mesh.name=" (.-name selected-mesh))
        (if (and (not second-pick) (not= (.-name first-pick) (.-name selected-mesh)))
          (do
            ;; second pick
            (println "setting second-pick, first-pick-index=" (utils/get-panel-index first-pick stem) ",second-pick-img=" (utils/get-front-panel-img db selected-mesh))
            (if (= (utils/get-front-panel-img db first-pick) (utils/get-front-panel-img db selected-mesh))
              (do
                (println "we have a match!, first-pick=" first-pick ", second-pick=" second-pick)
                (update-status-panel "match")
                (let [
                      first-pick-index (utils/get-panel-index first-pick stem)
                      first-pick-rebus-mat (-> (nth (db :rebus-board-cells) first-pick-index) :rebus-mat)
                      second-pick-index selected-mesh-index
                      second-pick-rebus-mat (get-in db [:rebus-board-cells second-pick-index :rebus-mat])
                      first-anim-group-name (:anim-group-name (get (db :game-cells) first-pick-index))
                      second-anim-group-name (:anim-group-name (get (db :game-cells) second-pick-index))]
                  (re-frame/dispatch [:cell-matched first-pick-index])
                  (re-frame/dispatch [:cell-matched second-pick-index])
                  (rebus-board/show-panel-rebus first-pick-index first-pick-rebus-mat)
                  (rebus-board/show-panel-rebus second-pick-index second-pick-rebus-mat)
                  ;; stop matched animations
                  (prn "match: stopping anims, first-pick-index=" first-pick-index)
                  (prn "match: animationGroup=" (nth (.-animationGroups main-scene/scene) first-pick-index))
                  ; (prn "match: anim-group-name first-pick=" (:anim-group-name (get (db :game-cells) first-pick-index)))
                  (prn "match: anim-group-name first-pick=" first-anim-group-name)
                  ; (prn "match: anim-group-name second-pick=" (:anim-group-name (get (db :game-cells) second-pick-index)))
                  ; (js-debugger)
                  ; (.pause (nth (.-animationGroups main-scene/scene) first-pick-index))))
                  (prn "match: animationGroups=" (js->clj (.-animationGroups main-scene/scene)))
                  ; (map #(do
                  ;                (prn "%1=" %1)
                  ;                (prn "%1.name=" (%1 :Name))
                  ;                (when (= (%1 :Name) first-anim-group-name)
                  ;                  (prn "match: now pausing")
                  ;                  (.pause %1))
                  ;             (js->clj (.-animationGroups main-scene/scene))))))
                  (doall (map #(do
                                 (let [o (js->clj %1)]
                                   (prn "%1=" o)
                                   ; (js-debugger)
                                   (prn "keys o=" (js-keys o))
                                   ; (prn "%1.name=" (o :o/name))
                                   (prn "%1.uniqueId=" (.-uniqueId o))
                                   ; (prn "%1.uniqueId=" (:uniqueId o))
                                   ; (prn "%1.isPlay=" (o "isPlaying"))
                                   (prn "%1.name=" (.-name o))
                                   (when (= (.-name o) first-anim-group-name)
                                     (prn "match: now pausing")
                                     (.restart %1)
                                     (.pause %1))))
                              (.-animationGroups main-scene/scene)))))
              (do
                (update-status-panel "non-match")))
            (assoc-in db [:board-status :second-pick] selected-mesh))
          (do
            (println "third-pick checking")
            (if (and second-pick (and (not= (.-name first-pick) (.-name selected-mesh)) (not= (.-name second-pick) (.-name selected-mesh))))
              ; (println "third-pick processing")
              (do
                (println "now resetting first and second picks")
                (update-status-panel "select a panel")
                (let [
                      first-panel-index (utils/get-panel-index first-pick stem)
                      second-panel-index (utils/get-panel-index second-pick stem)]
                  (when-not (= (get-in db [:rebus-board-cells first-panel-index :status]) :matched)
                    (re-frame/dispatch [:reset-panel first-panel-index]))
                  (when-not (= (get-in db [:rebus-board-cells second-panel-index :status]) :matched)
                    (re-frame/dispatch [:reset-panel second-panel-index]))
                  (re-frame/dispatch [:cell-picked (db :selected-mesh)])
                  (assoc-in (assoc-in db [:board-status :first-pick] nil) [:board-status :second-pick] nil)))
              (do (println "cell-picked: last path")
                db))))))))

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
         scene)
        box-text (js/BABYLON.GUI.TextBlock. "box-text" "block hello")
        status-pnl-mat (js/BABYLON.StandardMaterial. "status-panel-mat" scene)]
    (set! (.-position status-pnl) (js/BABYLON.Vector3. -1 (* panel-array-height 2.75) (+ panel-array-zc 1)))
    (set! (.-material status-pnl) status-pnl-mat)
    (set! (.-scaling status-pnl) (js/BABYLON.Vector3. 1 1 0.1))
    (set! (.-diffuseTexture status-pnl-mat) dyn-texture)
    (set! status-panel status-pnl)
    (.drawText dyn-texture "match" 300 200 "200px green" "white" "blue" true true)))

(defn init-status-board [db]
  (init-status-panel)
  (init-board-status db))
