;; good docs:
;;https://doc.babylonjs.com/how_to/introduction_to_webxr
(ns re-con.controller-xr
  (:require [re-frame.core :as re-frame]
            ; [re-con.main-scene :as main-scene]; note: generates circular dependency))
            [re-con.main-scene :as main-scene]
            [re-con.utils :as utils]))
            ; [re-con.db :as db]))

(def scene)
(def xr)
(def ctrl-xr)
(def left-ctrl-xr)
(def right-ctrl-xr)
(def main-trigger)
(def left-ray)
(def right-ray)

(declare trigger-handler)
(declare get-ctrl-handedness)
(declare ctrl-added)
(declare left-trigger-handler)
(declare right-trigger-handler)
(declare pointer-collider-handler)

; const ray = getWorldPointerRayToRef(controller);
(defn init [tgt-scene xr-helper]
  (println "controller-xr.init entered")
  (set! scene tgt-scene)
  (set! xr xr-helper))
  ; (set! left-ray main-scene/ray))

(defn ^:export setup-xr-ctrl-cbs [xr-helper]
  (-> xr-helper (.-input ) (.-onControllerAddedObservable) (.add ctrl-added)))

;; Note: should also be able to directly access the controllers via:
;; xr.input.controllers[0 or 1]
(defn ctrl-added [xr-controller]
  (println "controller-xr.ctrl-added: xr-controller.uniqueId=" (.-uniqueId xr-controller) ",handedness=" (get-ctrl-handedness xr-controller))
  ; const mainTrigger = xrController.motionController.getComponent(WebXRControllerComponent.TRIGGER);
  (let [handedness (get-ctrl-handedness xr-controller)]
    (when (= handedness :left)
      (set! left-ctrl-xr xr-controller))
      ; (set! left-ray (.getWorldPointerRayToRef xr-controller)))
    (when (= handedness :right)
      (set! right-ctrl-xr xr-controller)
      ; (.onCollideObservable (.-pointer xr-controller) pointer-collider-handler)
      (-> (.-pointer xr-controller) (.-onCollideObservable) (.add pointer-collider-handler))
      (set! (-> (.-pointer xr-controller) (.-onCollide)) pointer-collider-handler))
      ; (js-debugger))
      ; (set! right-ray (.getWorldPointerRayToRef xr-controller)))

    (set! main-trigger (-> xr-controller (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.TRIGGER)))
    ; (set! ctrl-xr xr-controller)
    ; (set! main-trigger (-> ctrl-xr (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.TRIGGER)))
    (println "controller-xr.ctrl-added: main-trigger=" main-trigger)
    (when main-trigger
      ; (.onButtonStateChanged main-trigger trigger-handler)
      (println "controller-xr.ctrl-added: add onButtonStateChanged to main-trigger=" main-trigger)
      ; (-> main-trigger (.-onButtonStateChanged) (.add trigger-handler))
      (-> main-trigger (.-onButtonStateChanged) (.add (if (= handedness :right)
                                                        right-trigger-handler
                                                        left-trigger-handler))))))
      ; (re-frame/dispatch [:attach-ray main-scene/ray xr-controller])
      ; (re-frame/dispatch [:attach-ray handedness main-scene/ray-helper]))))

(defn trigger-handler []
  (println "trigger fired"))

(defn left-trigger-handler []
  (println "left trigger fired"))

(defn right-trigger-handler []
  (println "right trigger fired"))

(defn pointer-collider-handler []
  (println "pointer collider detected"))

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
  (if false
  ; (if left-ray
  ; (if main-scene/ray
    ; (let [hit (.-pickWithRay left-ray)])
    (let [hit (.pickWithRay scene left-ray mesh-select-predicate)]
      (if hit
        (println "picking hit: mesh.name=" (-> hit (.-pickedMesh) (.-name)) ", mesh=" (.-pickedMesh hit))))))
;
; (defn ^:export tick []
;   (if main-scene/ray
;     (let [hit (.pickWithRay main-scene/ray)]
;       (if hit
;         (println "picking hit: mesh=" (.-pickedMesh hit))))))
(defn attach-ray-to-laser-pointer [hand ray]
  (println "attach-ray-to-last-pointer: ray=" ray)
  ; (let [laser-pointer-mesh (.getMeshByID xr-ctrl "laserPointer")])
  (let [laser-pointer-mesh (utils/get-xr-laser-pointer hand main-scene/scene)]
    (.attachToMesh main-scene/ray-helper laser-pointer-mesh)))
  ; (.attachToMesh main-scene/ray-helper tmp-obj (js/BABYLON.Vector3. 0 0 1) (js/BABYLON.Vector3. 0 0 0) 20))
