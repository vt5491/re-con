;; events is refer to many referred by few (none?)
(ns re-con.events
  (:require
   [re-frame.core :as re-frame]
   [re-con.db :as db]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.main-scene :as main-scene]
   [re-con.rebus-board :as rebus-board]
   [re-con.cell :as cell]
   [re-con.status-board :as status-board]
   [re-con.game-board :as game-board]
   [re-con.game :as game]
   [re-con.controller-xr :as ctrl-xr]))

(def tmp)

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

;;
;;> main-scene
;;
(re-frame/reg-event-db
 :set-main-scene
 (fn [db [_ scene]]
   (assoc db :main-scene scene)))

;;> controller/user interaction level
;;
(re-frame/reg-event-db
  :toggle-light
  (fn [db _]
    (println "toggle-light clicked: enabled status=" (.isEnabled main-scene/light1))
    (let [light main-scene/light1]
      (if (.isEnabled light)
        (.setEnabled light false)
        (.setEnabled light true)))
    db))


;; usage:  (dispatch [:trigger-change true])
; (re-frame/reg-event-db
;   :trigger-change
;   (fn [db [_ new-state]]
;     (println "now in trigger-change, new-state=" new-state)
;     ;; babylon side effect here
;     (if (and new-state (contains? db :selected-mesh))
;       (rebus-board/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)
;       ())
;     (assoc db :trigger-pressed new-state)))

;; non re-frame func
(defn mat-change-check [new-state db]
  (if (and new-state (contains? db :selected-mesh))
      (rebus-board/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)))

(re-frame/reg-event-db
 :show-rebus-panel-face
 (fn [db [_ _]]
   (when (:selected-mesh db)
     (prn "events: show-panel-face")
     (rebus-board/show-rebus-panel-face db (-> (db :selected-mesh) (.-name))))
   db))

;; TODO : is this still used?
(re-frame/reg-event-db
  :toggle-trigger
  (fn [db _]
    (println "now in toggle-trigger, not of trigger-pressed=" (not (:trigger-pressed db)))
    (assoc db :trigger-pressed (not (:trigger-pressed db)))))

;; note: main trigger routine
(re-frame/reg-event-fx
 ; :trigger-handler
 :rebus-panel-trigger-handler
 (fn [{:keys [db]} [_ stateObject]]
   (let [board-status (db :board-status)
         first-pick (get board-status :first-pick)
         second-pick (get board-status :second-pick)]
    (println ":trigger-handler: .-pressed stateObject=" (.-pressed stateObject) ", selected-mesh=" (db :selected-mesh))
    (merge {:db (assoc db :trigger-pressed (.-pressed stateObject))}
           (when-let [dummy-val
                      (and
                                     (.-pressed stateObject)
                                     (not (:trigger-pressed db)))]
             {:dispatch-n (list [:show-rebus-panel-face]
                                [:cell-picked (db :selected-mesh)]
                                ;; and trigger the corresponding game tile to be selected.
                                [:game-board-trigger-handler
                                 (-> main-scene/scene
                                     (.getMeshByName
                                      (str "game-tile-"
                                           (utils/get-panel-index (:selected-mesh db) "rebus-panel"))))])})))))

(re-frame/reg-event-db
 :game-board-trigger-handler
 (fn [db [_ picked-mesh]]
   ;;non-rf side effect
   (game-board/tile-selected picked-mesh)
   db))

(re-frame/reg-event-db
 :print-db
 (fn [db _]
   (println "db=" db ",trigger-pressed=" (:trigger-pressed db))
   db))

;; This is where we promote a babylon mesh selection event up to a re-frame event.
(re-frame/reg-event-db
 :mesh-selected
 (fn [db [_ mesh mesh-index]]
   (println "events: :mesh-selected event")
   (assoc db :selected-mesh mesh)))

(re-frame/reg-event-db
 :mesh-unselected
 (fn [db [_ mesh]]
   (dissoc db :selected-mesh)))

(re-frame/reg-event-db
 :action
 (fn [db _]
   (let [scene main-scene/scene
         skel (.getSkeletonById scene "skeleton-0")]
     (prn "skel=" skel)
     ; (js-debugger)
     (prn "skel.scene=" (.getScene skel))
     ; (prn "count skel.animations" (count (-> skel .-animations)))
     ; (prn "alength skel.animations" (alength (-> skel .-animations)))
     (doall (map #(.pause %1) (.-animationGroups scene))))
     ; (set! (.-isVisible skel) false)
     ; (.stopAnimation scene skel))
     ; (set! (-> skel .-animationPropertiesOverride .-loopMode) 0))
     ; (.beginAnimation scene skel 0 0 false)
     ; (-> (nth (.-animationGroups scene) 4) .stop))
     ; (.returnToRest skel))
     ; (.dispose skel))
   ; (let [mesh1 (.getMeshByID main-scene/scene "__root__")]
   ;   (prn "mesh1=" mesh1)
   ;   (if (.isEnabled mesh1)
   ;     (.setEnabled mesh1 false)
   ;     (.setEnabled mesh1 true)))

   db))

(re-frame/reg-event-db
 :action-2
 (fn [db _]
   ;side effect
   (-> main-scene/scene (.-debugLayer) (.show))
   db))

;;
;;> rebus-board level
;;

(re-frame/reg-event-db
 :load-front-imgs
 (fn [db [_]]
   ; non-rf side effect
   (rebus-board/load-front-imgs db)
   db))

(re-frame/reg-event-db
 :load-rebus-imgs
 (fn [db [_]]
   ;non-rf side effect
   (rebus-board/load-rebus-imgs db)
   db))

(re-frame/reg-event-db
 :front-texture-loaded
 (fn [db [_ task index]]
   ;non-rf side effect
   (rebus-board/front-texture-loaded db task index)
   db))

(re-frame/reg-event-db
 :rebus-texture-loaded
 (fn [db [_ task index]]
   ;side effect
   (rebus-board/rebus-texture-loaded db task index)
   db))

(re-frame/reg-event-db
 :init-rebus-board
 (fn [db [_]]
   ; side effect
   (rebus-board/init-rebus-board)
   db))

(re-frame/reg-event-db
 :reset-panel
 (fn [db [_ index]]
   (println "now resetting panel " index)
   ;non-rf side effect
   (rebus-board/reset-panel index)
   (game-board/reset-tile index)
   db))

;;
;;> cell related events
;;
;; TODO: rename to 'rebus-board-cells'
(re-frame/reg-event-db
 :add-cell
 (fn [db [_ cell]]
   (assoc db :rebus-board-cells (conj (:rebus-board-cells db) cell))))

(re-frame/reg-event-db
 :cell-front-img
 (fn [db [_ index img]]
   db))

(re-frame/reg-event-db
 ; :init-board-cells
 :init-rebus-cells
 (fn [db [_]]
   ; non-rf side effect
   (prn "events: init-rebus-cells: rnd-board-seq=" (:rnd-board-seq db))
   (prn "events: init-rebus-cells: randomized coll=" (utils/randomize-coll-by-seq base/ybot-many-anim-imgs (:rnd-board-seq db)))
   (cell/init-rebus-cells db base/board-row-cnt base/board-col-cnt
                          (utils/randomize-coll-by-seq base/ybot-many-anim-imgs (:rnd-board-seq db)))
   db))

(re-frame/reg-event-db
 :init-game-cells
 (fn [db [_ tile-set]]
   ;; delegate to outside fn.
   (prn "event: init-game-cells: rnd tile-set=" (utils/randomize-coll-by-seq tile-set (:rnd-board-seq db)))
   (assoc db :game-cells (utils/randomize-coll-by-seq tile-set (:rnd-board-seq db)))))

(re-frame/reg-event-db
 :reset-picks
 (fn [db [_ _]]
   ; non-rf side effect
   (cell/reset-cell-picks db)))

;;
;;> game-tile related events
;;
(re-frame/reg-event-db
 :load-tile-set
 (fn [db [_]]
   ; non-rf side effect
   (utils/load-tile-set db)
   db))

(re-frame/reg-event-db
 :load-power-slave-pyr
 (fn [db [_]]
   ; non-rf side effect
   (utils/load-power-slave-pyr db)
   db))

(re-frame/reg-event-db
 :load-model
 (fn [db [_ path fn & [cb]]]
   ; non-rf side effect
   (utils/load-model db path fn cb)
   db))

(re-frame/reg-event-db
 :load-grass
 (fn [db [_ path fn]]
   ; non-rf side effect
   (game-board/load-grass db path fn)
   db))

(re-frame/reg-event-db
 :play-tile-selected-snd
 (fn [db [_]]
   (game-board/play-tile-selected-snd)
   db))

;;
;;> status board level
;;
(re-frame/reg-event-db
 ; :init-board-stajtus
 :init-status-board
 (fn [db [_]]
   ; non-rf side effect
   (status-board/init-status-board db)))

(re-frame/reg-event-db
 :cell-picked
 (fn [db [_ selected-mesh]]
   ; non-rf side effect
   (status-board/cell-picked db selected-mesh)))

(re-frame/reg-event-db
 :cell-matched
 (fn [db [_ match-index]]
   (println "cell-matched: index=" match-index ", status=" (get-in db [:rebus-board-cells match-index :status]))
   (assoc-in db [:rebus-board-cells match-index :status] :matched)))

;;
;; game board level
;;
(re-frame/reg-event-db
 :init-game-board
 (fn [db [_]]
   ; non-rf side effect
   (game-board/init-game-tiles)
   (game-board/init-snd)
   db))

;;
;; utils level
;;
(re-frame/reg-event-db
 :rnd-board-seq
 (fn [db [_]]
   ;; non-rf delegate
   (assoc db :rnd-board-seq (utils/rnd-board-seq))))

;;> xr events

(re-frame/reg-event-db
 :setup-xr-ctrl-cbs
 (fn [db [_ xr]]
   (ctrl-xr/setup-xr-ctrl-cbs xr)
   db))

(re-frame/reg-event-db
 :init-xr
 (fn [db [_ scene xr]]
   (ctrl-xr/init scene xr)
   db))
