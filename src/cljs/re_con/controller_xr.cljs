;; good docs:
;;https://doc.babylonjs.com/how_to/introduction_to_webxr
(ns re-con.controller-xr
  (:require [re-frame.core :as re-frame]
            ; [re-con.main-scene :as main-scene]; note: generates circular dependency))
            [re-con.main-scene :as main-scene]
            [re-con.base :as base]
            [re-con.utils :as utils]))

(def scene)
(def xr)
(def ctrl-xr)
(def left-ctrl-xr)
(def right-ctrl-xr)
(def main-trigger)
(def grip)
(def x-btn)
(def left-ray)
(def right-ray)
(def is-gripping false)
(def grip-start-pos)
(def player-start-pos)
(def last-player-pos)
(def last-grip-vel)
(def last-grip-time)
; (def grip-factor 1.9) ;; probably the best
; (def grip-factor 2.1) ;; good for scale-factor=100
(def grip-factor (if (>= base/scale-factor 100) 2.1 1.9))
; (def GRIP_DECELERATION_INT 1000)
(def GRIP_DECELERATION_INT 1500)

(declare trigger-handler-xr)
(declare grip-handler-xr)
(declare get-ctrl-handedness)
(declare ctrl-added)
(declare left-trigger-handler)
(declare right-trigger-handler)
(declare pointer-collider-handler)
(declare x-btn-handler)

; const ray = getWorldPointerRayToRef(controller);
(defn init [tgt-scene xr-helper]
  (println "controller-xr.init entered")
  (set! scene tgt-scene)
  (set! xr xr-helper)
  (set! last-grip-time (.now js/Date))
  (set! last-grip-vel (js/BABYLON.Vector3. 0 0 0))
  (set! last-player-pos (.-position main-scene/camera)))

(defn ^:export setup-xr-ctrl-cbs [xr-helper]
  (-> xr-helper (.-input ) (.-onControllerAddedObservable) (.add ctrl-added)))

;; Note: should also be able to directly access the controllers via:
;; xr.input.controllers[0 or 1]
(defn ctrl-added [xr-controller]
  (println "controller-xr.ctrl-added: xr-controller.uniqueId=" (.-uniqueId xr-controller) ",handedness=" (get-ctrl-handedness xr-controller))
  (println "controller components=" (-> xr-controller (.-gamepadController) (.getComponentTypes)))
  ; const mainTrigger = xrController.motionController.getComponent(WebXRControllerComponent.TRIGGER);
  (let [handedness (get-ctrl-handedness xr-controller)
        gamepad-ctrl (.-gamepadController xr-controller)]
    (when (= handedness :left)
      (set! left-ctrl-xr xr-controller))
      ; (set! left-ray (.getWorldPointerRayToRef xr-controller)))
    (when (= handedness :right)
      (set! right-ctrl-xr xr-controller)
      ; (.onCollideObservable (.-pointer xr-controller) pointer-collider-handler)
      (-> (.-pointer xr-controller) (.-onCollideObservable) (.add pointer-collider-handler))
      (set! (-> (.-pointer xr-controller) (.-onCollide)) pointer-collider-handler))

    (set! main-trigger (-> xr-controller (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.TRIGGER)))
    ; (js-debugger)
    ; (set! ctrl-xr xr-controller)
    ; (set! main-trigger (-> ctrl-xr (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.TRIGGER)))
    (println "controller-xr.ctrl-added: main-trigger=" main-trigger)
    (when main-trigger
      ; (.onButtonStateChanged main-trigger trigger-handler)
      (println "controller-xr.ctrl-added: add onButtonStateChanged to main-trigger=" main-trigger))
    (set! grip (-> xr-controller (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.SQUEEZE)))
    (when grip
       (-> grip (.-onButtonStateChanged) (.add grip-handler-xr)))
    (set! x-btn (.getComponent gamepad-ctrl "a-button"))
    (when x-btn
      (-> x-btn (.-onButtonStateChanged) (.add x-btn-handler)))))

      ;; the following works, but I chose to do all trigger events handling in main-scene/pointerHandler
      ;; so the mesh-selected and trigger-handler events occur at the same time.  I get timing issues
      ;; if I do them separately.
      ; (-> main-trigger (.-onButtonStateChanged) (.add trigger-handler-xr)))))
      ; (re-frame/dispatch [:attach-ray main-scene/ray xr-controller])
      ; (re-frame/dispatch [:attach-ray handedness main-scene/ray-helper]))))

(defn trigger-handler-xr [trigger-state]
  (re-frame/dispatch [:trigger-handler (js-obj "pressed" (.-pressed trigger-state))]))

(defn grip-handler-xr [grip-state]
  (if (.-pressed grip-state)
    (when (and left-ctrl-xr (not is-gripping))
      (set! grip-start-pos (-> left-ctrl-xr (.-grip) (.-position) (.clone)))
      (set! is-gripping true)
      (set! player-start-pos (.-position main-scene/camera))
      (set! last-grip-time (.now js/Date)))
    (if is-gripping
      (do
        ;; transition from gripping to non-gripping
        (set! is-gripping false)
        (set! last-grip-time (.now js/Date))
        ;; secret for good coasting velocity is to go off camera deltas not grip deltas.
        (let [normal-vel (.normalize (.subtract (.-position main-scene/camera) player-start-pos))
              mag (.length last-grip-vel)]
          (set! last-grip-vel (.multiplyByFloats normal-vel mag mag mag))))
      ;; non-transitioning non-gripping
      (set! is-gripping false))))

(defn left-trigger-handler []
  (println "left trigger fired"))

(defn right-trigger-handler []
  (println "right trigger fired"))

(defn pointer-collider-handler []
  (println "pointer collider detected"))

(defn x-btn-handler [state]
  (when (.-pressed state)
    (println "x-btn pressed")
    (set! (.-position main-scene/camera) main-scene/camera-init-pos)))

;; determine if id of the ctrl is "left" or "right"
(defn get-ctrl-handedness [ctrl]
  (let [id (.-uniqueId ctrl)]
    (if (re-matches #".*-(left).*" id)
      :left
      (if (re-matches #".*-(right).*" id)
        :right))))

;
; function predicate(mesh){}
;   if (mesh == box2 || mesh == box){}
;     return false;
;
;   return true;
(defn mesh-select-predicate [mesh]
  (if (= (.-name mesh) "tmp-obj")
    false
    true))

(defn ^:export tick []
  ; (println "controller_xr.tick.enter: camera-pos=" (.-position main-scene/camera))
  (when left-ctrl-xr
    (cond
      is-gripping
      (let [ctrl-delta-pos (-> left-ctrl-xr (.-grip) (.-position) (.subtract grip-start-pos) (.multiplyByFloats grip-factor grip-factor grip-factor))
            new-pos (.subtract (.-position main-scene/camera) ctrl-delta-pos)]
        ; (println "controller_xr.tick.1: camera-pos=" (.-position main-scene/camera))
        (set! (.-position main-scene/camera) new-pos)
        (set! last-grip-vel (.subtract new-pos last-player-pos))
        (set! last-player-pos (.-position main-scene/camera)))
      ; (and (not is-gripping) last-grip-vel (< (- (.now js/Date)  last-grip-time) 1000))
      (and (not is-gripping) last-grip-vel (< (- (.now js/Date)  last-grip-time) GRIP_DECELERATION_INT))
      (let [delta-time (- (.now js/Date) last-grip-time)
            vel-strength (* 5.6 (- 1.0 (/ delta-time GRIP_DECELERATION_INT)))
            delta-pos (.multiplyByFloats last-grip-vel vel-strength vel-strength vel-strength)]
        ; (println "controller_xr.tick.2: camera-pos=" (.-position main-scene/camera) ", delta-pos=" delta-pos)
        (set! (.-position main-scene/camera) (.add (.-position main-scene/camera) delta-pos))))))
  ; (println "controller_xr.tick.exit: camera-pos=" (.-position main-scene/camera)))



  ; (if false
  ; ; (if left-ray
  ; ; (if main-scene/ray
  ;   ; (let [hit (.-pickWithRay left-ray)])
  ;   (let [hit (.pickWithRay scene left-ray mesh-select-predicate)]
  ;     (if hit
  ;       (println "picking hit: mesh.name=" (-> hit (.-pickedMesh) (.-name)) ", mesh=" (.-pickedMesh hit))))))
;
; (defn ^:export tick []
;   (if main-scene/ray
;     (let [hit (.pickWithRay main-scene/ray)]
;       (if hit
;         (println "picking hit: mesh=" (.-pickedMesh hit))))))
;defunct
(defn attach-ray-to-laser-pointer [hand ray]
  (println "attach-ray-to-last-pointer: ray=" ray)
  ; (let [laser-pointer-mesh (.getMeshByID xr-ctrl "laserPointer")])
  (let [laser-pointer-mesh (utils/get-xr-laser-pointer hand main-scene/scene)]
    (.attachToMesh main-scene/ray-helper laser-pointer-mesh)))
  ; (.attachToMesh main-scene/ray-helper tmp-obj (js/BABYLON.Vector3. 0 0 1) (js/BABYLON.Vector3. 0 0 0) 20))
