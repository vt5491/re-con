;; main_scene should reference few and be accessible by many
(ns re-con.main-scene
  (:require
   [re-frame.core :as re-frame]
    ;; actually don't need to require babylonjs as it's in the js namespace already.
   ; [babylonjs]
   ; [promesa.core :as p :refer-macros [async]]
   ; [re-con.utils as utils]))
   [re-con.base :as base]
   [re-con.controller :as controller]
   ; [re-con.controller-xr :as ctrl-xr]
   [re-con.test-scene-ecsy :as test-scene-ecsy]
   [promesa.core :as p]
   [components]
   [systems]
   [ecsy :refer (World System)])) ;; works
   ; [async-await.core :refer [async await]]))
   ; [promesa.async-cljs :refer-macros [async]]))
   ; [clojure.core.async :as async :refer :all]))

(def canvas)
(def engine)
(def scene)
(def env)
(def camera)
(def vrHelper)
(def xr)
(def ground)
(def light1)
(def rig)
(def sphere)
(def panel)
(def redMaterial)
(def blueMaterial)
(def greenMaterial)
(def imgMat)
(def assetsManager)
(def meshTask)
(def textureTask)
(def gui-3d-manager)
(def obj-moving)
(def object-3d)
(def tmp-obj)
(def last-time)
(def cube-entity)
(def world)
(def ray)
(def picking-ray)
(def ray-helper)
(def features-manager)

(declare init-part-2)
(declare init-basic-2-2)
; (declare setup-xr-ctrl-cbs)
(declare cast-ray)
(declare pointer-handler)

(defn mesh-selected [])

(defn load-img-cb []
  (fn [task]
    (set! (.-diffuseTexture imgMat) (js/BABYLON.Texture. task.texture))))

(defn init []
  (println "now in init-scene")
  (set! canvas (-> js/document (.getElementById "renderCanvas")))
  (set! engine (js/BABYLON.Engine. canvas true))
  (set! scene (js/BABYLON.Scene. engine))
  (set! env (.-createDefaultEnvironment scene))
  (set! redMaterial (js/BABYLON.StandardMaterial. "redMaterial" scene))
  (set! (.-diffuseColor redMaterial) (js/BABYLON.Color3. 1 0 0))
  (set! blueMaterial (js/BABYLON.StandardMaterial. "blueMaterial" scene))
  (set! (.-diffuseColor blueMaterial) (js/BABYLON.Color3. 0 0 1))
  (set! greenMaterial (js/BABYLON.StandardMaterial. "greenMaterial" scene))
  (set! (.-diffuseColor greenMaterial) (js/BABYLON.Color3. 0 1 0))
  (if (not base/use-xr)
    (do
      (println "now setting up vr")
      (set! vrHelper (.createDefaultVRExperience scene (js-obj "useXR" false)))
      (set! camera (.-webVRCamera vrHelper))
      (controller/init scene vrHelper camera)
      (controller/setup-controller-handlers vrHelper)
      (set! ground (js/BABYLON.MeshBuilder.CreateGround. "ground" (js-obj "width" 10 "height" 10) scene))
      (.enableTeleportation vrHelper (js-obj "floorMeshName" "ground"))
      (.enableInteractions vrHelper)
      (js/setupControllers vrHelper)
      (init-part-2))
    (do
      (println "now setting up xr")
      ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -10) scene))
      ; (set! xr (.createDefaultXRExperienceAsync scene))
      (-> (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env))))
      ; (-> (.createDefaultXRExperienceAsync scene (js-obj))
          (p/then
           (fn [x] (println "xr-x=" x ",scene=" scene)
             (set! xr x)
             ; (set! (.-pointerSelection xr) false)
             ; (ctrl-xr/setup-xr-ctrl-cbs xr)
             (re-frame/dispatch [:setup-xr-ctrl-cbs xr])
             ; (js-debugger)
             ; (js/BABYLON.WebXRFeaturesManager.disableFeature "xr-controller-pointer-selection")
             (set! features-manager (-> xr (.-baseExperience) (.-featuresManager)))
             ; (.disableFeature features-manager "xr-controller-pointer-selection")
             ; (.enableFeature features-manager "xr-controller-pointer-selection")
             (println "xr features available=" (.GetAvailableFeatures js/BABYLON.WebXRFeaturesManager))
             (println "xr features acitve=" (-> xr (.-baseExperience) (.-featuresManager) (.getEnabledFeatures)))
             (println "POINTERDOWN=" js/BABYLON.PointerEventTypes.POINTERDOWN)
             (println "POINTERPICK=" js/BABYLON.PointerEventTypes.POINTERPICK)
             (.registerBeforeRender scene cast-ray)
             (set! tmp-obj (js/BABYLON.MeshBuilder.CreateBox. "tmp-obj"
                                                              (js-obj "height" 0.25, "width" 0.25, "depth" 0.25)
                                                              scene))
             ; (set! (.-position tmp-obj) (js/BABYLON.Vector3. 0 0.5 0))
             (set! (.-position tmp-obj) (js/BABYLON.Vector3. 2 0.5 0))
             (set! (.-material tmp-obj) greenMaterial)
             (set! camera (-> x (.-baseExperience) (.-camera)))
             ; (ctrl-xr/init scene xr)
             (re-frame/dispatch [:init-xr xr])
             (set! ray (js/BABYLON.Ray.))
             (set! ray-helper (js/BABYLON.RayHelper. ray))
             ; (set! ctrl-xr/left-ray ray)
             (.attachToMesh ray-helper tmp-obj (js/BABYLON.Vector3. 0 0 1) (js/BABYLON.Vector3. 0 0 0) 20)
             (.show ray-helper scene js/BABYLON.Color3. 255 0 0)
             (init-part-2)))))) ;; no work if no obj-moving in stanza
             ; (init-basic-2-2))))))
  (println "at end of init"))
                                                     ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene)))))))
                                                     ; (set! camera (-> x (.-baseExperience) (.-camera))))))))


(defn init-part-2 []
  (println "now in init-part-2")
  ;; svale adjustment
  ;; Note: need if not using webxr (browser) extension, comment out if you are.
  ; (set! (.-rotationQuaternion camera) (-> js/BABYLON.Quaternion (.FromEulerAngles 0 (/ js/Math.PI 1) 0)))
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 0 5 -3) scene))
  (.setEnabled light1 true)
  ;; need to have obj-moving for some reason
  (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
                    "status-panel"
                    (js-obj "height" 0.2
                            "width" 0.2
                            "depth" 0.1)
                    scene))
  ; (let [mat (js/BABYLON.StandardMaterial.)]
  ;   (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
  ;   (set! (.-material obj-moving) mat))
  ; (init-basic-2-2))
  (.attachControl camera canvas false)
  ; (println "abc=" (-> js/BABYLON.Quaternion (.FromEulerAngles 0 1 0)))
  (set! imgMat (js/BABYLON.StandardMaterial. "imgMat" scene))
  (set! assetsManager (js/BABYLON.AssetsManager. scene))
  (set! textureTask (.addTextureTask assetsManager "load-texture" "imgs/burj_al_arab.jpg"))
  (set! textureTask.onSuccess (load-img-cb))
  (.load assetsManager)
  ; (.enableInteractions vrHelper)
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
  (.setEnabled light1 true)
  (if base/use-xr
    (-> (.-onPointerObservable scene) (.add pointer-handler))))
  ; (set! gui-3d-manager (js/BABYLON.GUI.GUI3DManager. scene)))
  ; (.runRenderLoop engine (fn []
  ;                          (let [time (-> (js/performance.now) (/ 1000))
  ;                                delta (- time last-time)]
  ;                            (set! last-time time)
  ;                            (.render scene)))))

(defn init-panel-scene[]
  (set! panel (js/BABYLON.MeshBuilder.CreateBox. "panel"
                                                (js-obj "height" 5 "width" 2 "depth" 0.5)
                                                scene))
  (set! (.-position panel)(js/BABYLON.Vector3. 0 0 3))
  (set! (.-material panel) redMaterial))

(defn run-scene [render-loop]
  (.runRenderLoop engine (fn [] (render-loop))))

;
; function vecToLocal(vector, mesh) {}
; var m = mesh.getWorldMatrix();
; var v = BABYLON.Vector3.TransformCoordinates(vector, m);
; return v;
(defn vec-to-local [vector mesh]
  (let [m (.getWorldMatrix mesh)
        ; v (.TransformCoordinates js/BABYLON.Vector3) vector m
        v (js/BABYLON.Vector3.TransformCoordinates vector m)]
    v))

;; refer to https://www.babylonjs-playground.com/#BCU1XR#329
(defn cast-ray []
  ; (println "now in cast-ray")
  (let [origin (.-position tmp-obj)
        fwd-1 (js/BABYLON.Vector3. 0 0 1)
        fwd-2 (vec-to-local fwd-1 tmp-obj)
        dir (js/BABYLON.Vector3.Normalize (.subtract fwd-2 origin))]
    (set! ray (js/BABYLON.Ray. origin (js/BABYLON.Vector3. 0 0 0) 100))))
  ; (set! picking-ray
  ;   (.createPickingRay scene (.-pointerX scene) (.-pointerY scene) (.Identity js/BABYLON.Matrix) (.-activeCamera scene)))

(defn pointer-handler [pointer-info]
  (let [type (.-type pointer-info)
        ; picked-mesh (-> pointer-info (.-pickInfo) (.-pickedMesh) (.-id))
        picked-mesh (if (and
                         (.-pickInfo pointer-info)
                         (-> pointer-info (.-pickInfo) (.-pickedMesh)))
                         ; (-> pointer-info (.-pickInfo) (.-pickedMesh) (.-id)))
                      (-> pointer-info (.-pickInfo) (.-pickedMesh)))]
    (cond
      ; (= type js/BABYLON.PointerEventTypes.POINTERDOWN) (do (println "POINTER DOWN, picked-mesh=" (.-id picked-mesh)))
      ; (= type js/BABYLON.PointerEventTypes.POINTERPICK)
      (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
      (do
        (println "POINTER DOWN, picked-mesh=" picked-mesh)
        ; (js-debugger)
        (re-frame/dispatch [:mesh-selected picked-mesh])
        ; (re-frame/dispatch [:trigger-handler (js-obj "pressed" (.-pressed trigger-state))])
        (re-frame/dispatch [:trigger-handler (js-obj "pressed" true)]))
      (= type js/BABYLON.PointerEventTypes.POINTERUP)
      (do
        (println "POINTER UP, picked-mesh=" picked-mesh)
        ; (js-debugger)
        ; (re-frame/dispatch [:mesh-selected picked-mesh])
        ; (re-frame/dispatch [:trigger-handler (js-obj "pressed" (.-pressed trigger-state))])
        (re-frame/dispatch [:trigger-handler (js-obj "pressed" false)]))
      ; (= type js/BABYLON.PointerEventTypes.POINTERMOVE) (do (println "POINTER MOVE"))
      ; :else (println "cond-CATCHALL")
      :else nil)))
  ;(case (.-type pointer-info)
  ;   js/BABYLON.PointerEventTypes.POINTERDOWN (do (println "POINTER DOWN"))
  ;   js/BABYLON.PointerEventTypes.POINTERPICK (do (println "POINTER PICK"))
  ;   (println "CATCHALL: pointer-info.type=" (.-type pointer-info))))

; scene.onPointerObservable.add((pointerInfo) => {})
;     switch (pointerInfo.type) {}
;         case BABYLON.PointerEventTypes.POINTERDOWN:
;             console.log("POINTER DOWN");
;             break;
;         case BABYLON.PointerEventTypes.POINTERUP:
;             console.log("POINTER UP");
;             break;
;         case BABYLON.PointerEventTypes.POINTERMOVE:
;             console.log("POINTER MOVE");
;             break;
;         case BABYLON.PointerEventTypes.POINTERWHEEL:
;             console.log("POINTER WHEEL");
;             break;
;         case BABYLON.PointerEventTypes.POINTERPICK:
;             console.log("POINTER PICK");
;             break;
;         case BABYLON.PointerEventTypes.POINTERTAP:
;             console.log("POINTER TAP");
;             break;
;         case BABYLON.PointerEventTypes.POINTERDOUBLETAP:
;             console.log("POINTER DOUBLE-TAP");
;             break;
;
;
