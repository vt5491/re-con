(ns re-con.events
  (:require
   [re-frame.core :as re-frame]
   [re-con.db :as db]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.main-scene :as main-scene]
   [re-con.scenes.con-panel-scene :as cp-scene]
   [re-con.cell :as cell]
   [re-con.board :as board]
   [re-con.game :as game]))

(def tmp)

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

;;
;; controller/user interaction level
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
(re-frame/reg-event-db
  :trigger-change
  (fn [db [_ new-state]]
    (println "now in trigger-change, new-state=" new-state)
    ;; babylon side effect here
    (if (and new-state (contains? db :selected-mesh))
      (cp-scene/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)
      ())
    (assoc db :trigger-pressed new-state)))

;; non re-frame func
(defn mat-change-check [new-state db]
  (if (and new-state (contains? db :selected-mesh))
      (cp-scene/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)))
      ; ()))

(re-frame/reg-event-db
 :toggle-panel-material
 (fn [db [_ _]]
   (cp-scene/toggle-panel-material db (-> (db :selected-mesh) (.-name)))
   db))

(re-frame/reg-event-db
 :show-panel-face
 (fn [db [_ _]]
   (cp-scene/show-panel-face db (-> (db :selected-mesh) (.-name)))
   db))

(re-frame/reg-event-db
  :toggle-trigger
  (fn [db _]
    (println "now in toggle-trigger, not of trigger-pressed=" (not (:trigger-pressed db)))
    ; {:trigger-pressed true}))
    (assoc db :trigger-pressed (not (:trigger-pressed db)))))
    ; (set! tmp (assoc db :trigger-pressed true))
    ; tmp))


;; note: main trigger routine
; (re-frame/reg-event-db
;  :trigger-handler
;  (fn [db [_ stateObject]]
;    (let [board-status (db :board-status)
;          first-pick (get board-status :first-pick)
;          second-pick (get board-status :second-pick)]
;      (cond (or (= first-pick nil) (= second-pick nil))
;        (if (and (.-pressed stateObject) (not (:trigger-pressed db)))
;          ; side effect
;          (cp-scene/toggle-panel-material db (-> (get db :selected-mesh) (.-name)))
;          ; and fire a cell-picked event
;          (re-frame/dispatch [:cell-picked (get db :selected-mesh)]))))
;
;    (assoc db :trigger-pressed (.-pressed stateObject))))

;; note: main trigger routine
(re-frame/reg-event-fx
 :trigger-handler
 (fn [{:keys [db]} [_ stateObject]]
   (let [board-status (db :board-status)
         first-pick (get board-status :first-pick)
         second-pick (get board-status :second-pick)]
     ; (cond (or (= first-pick nil) (= second-pick nil))
     ;   (if (and (.-pressed stateObject) (not (:trigger-pressed db)))
     ;     ; side effect
     ;     (cp-scene/toggle-panel-material db (-> (get db :selected-mesh) (.-name)))))
         ; and fire a cell-picked event
         ; (re-frame/dispatch [:cell-picked (get db :selected-mesh)]))))
   ; {
    (merge {:db (assoc db :trigger-pressed (.-pressed stateObject))}
           (when-let [dummy-val
                      ; (and (or (= first-pick nil) (= second-pick nil)
                      (and
                                     (.-pressed stateObject)
                                     (not (:trigger-pressed db)))]
             {:dispatch-n (list [:show-panel-face]
                                [:cell-picked (db :selected-mesh)])})))))
           ; (when-let [val (get {:a 7} :a)])
           ; (when-let [val true]
           ;   {:dispatch-n (list [:abc "mj"] [:abc-2 "angus young"])})))))
    ; (if true
    ;   (:dispatch-n (list [:abc "mj"] [:abc-2 "angus young"])))}))

; (re-frame/reg-event-db
;  :toggle-trigger
;  {:trigger-pressed true})
(re-frame/reg-event-db
 :print-db
 (fn [db _]
   (println "db=" db ",trigger-pressed=" (:trigger-pressed db))
   db))

;; This is where we promote a babylon mesh selection event up to a re-frame event.
(re-frame/reg-event-db
 :mesh-selected
 (fn [db [_ mesh mesh-index]]
   ; (println "events: :mesh-selected event")
   (assoc db :selected-mesh mesh)))
   ; (assoc (assoc db :selected-mesh mesh) :selected-mesh-index mesh-index)))

(re-frame/reg-event-db
 :mesh-unselected
 (fn [db [_ mesh]]
   (dissoc db :selected-mesh)))

(re-frame/reg-event-db
 :info
 (fn [db _]
   ;non-rf side effect
    ; (cp-scene/show-full-rebus db)
   (println "selected-mesh=" (if (contains? db :selected-mesh)
                               (-> (get db :selected-mesh) .-name)
                               nil))
   db))

(re-frame/reg-event-db
 :action-2
 (fn [db _]
   ;side effect
   ; (println "rebus-mat 4=" (db :rebus-mat 4))
   ; (println "board-cells-2=" (db :board-cells-2))
   ; (cp-scene/show-full-rebus-2 db)
   ; (println "rebus-mat-4=" (cell/get-rebus-mat db 4))
   (let [dynMat (.getMaterialByName main-scene/scene "status-panel-mat")]
     (println "dynMat.texture=" (.-diffuseTexture dynMat))
     (.drawText (.-diffuseTexture dynMat) "abc" 300 200 "200px green" "white" "blue" true true))

   db))

;;
;; cp-scene level
;;
(re-frame/reg-event-db
 :abc
 (fn [db [_ msg]]
   (cp-scene/abc msg)
   db))

(re-frame/reg-event-db
 :abc-2
 (fn [db [_ msg]]
   (cp-scene/abc-2 msg)
   db))

(re-frame/reg-event-db
 :load-front-imgs
 (fn [db [_]]
   ; side effect
   (cp-scene/load-front-imgs db)
   db))

(re-frame/reg-event-db
 :load-rebus-imgs
 (fn [db [_]]
   ;non-rf side effect
   (cp-scene/load-rebus-imgs db)
   db))

(re-frame/reg-event-db
 :front-texture-loaded
 (fn [db [_ task index]]
   ;non-rf side effect
   (cp-scene/front-texture-loaded db task index)
   db))

(re-frame/reg-event-db
 :rebus-texture-loaded
 (fn [db [_ task index]]
   ;side effect
   (cp-scene/rebus-texture-loaded db task index)
   db))

(re-frame/reg-event-db
 :init-con-panel-scene
 (fn [db [_]]
   ; side effect
   (cp-scene/init-con-panel-scene db)
   ; (assoc db :selectedMesh nil)))
   db))

(re-frame/reg-event-db
 :reset-panel
 (fn [db [_ index]]
   (println "now resetting panel " index)
   ;non-rf side effect
   (cp-scene/reset-panel index)
   db))
;;
;; cell related events
;;
(re-frame/reg-event-db
 :add-cell
 (fn [db [_ cell]]
   ; (println "add-cell.fn: entered, db.do-it=" (db/do-it))
   (assoc db :board-cells (conj (:board-cells db) cell))))

(re-frame/reg-event-db
 :cell-front-img
 (fn [db [_ index img]]
   db))

(re-frame/reg-event-db
 :init-board-cells
 (fn [db [_]]
   ; non-rf side effect
   (cell/init-board-cells db base/board-row-cnt base/board-col-cnt base/default-img-map)
   db))

(re-frame/reg-event-db
 :reset-picks
 (fn [db [_ _]]
   ; non-rf side effect
   (cell/reset-cell-picks db)))


;;
;; board level
;;
(re-frame/reg-event-db
 :init-board-status
 (fn [db [_]]
   ; non-rf side effect
   (board/init-board-status db)))

(re-frame/reg-event-db
 :cell-picked
 (fn [db [_ selected-mesh]]
   ; non-rf side effect
   (board/cell-picked db selected-mesh)))
