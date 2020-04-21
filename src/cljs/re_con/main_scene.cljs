;; main_scene should reference few and be accessible by many
(ns re-con.main-scene
  (:require-macros [re-con.macros :as macros])
  (:require
   [re-frame.core :as re-frame]
    ;; actually don't need to require babylonjs as it's in the js namespace already.
   ; [babylonjs]
   ; [promesa.core :as p :refer-macros [async]]
   [re-con.base :as base]
   [re-con.utils :as utils]
   ; [re-con.macros :as macros]
   [re-con.controller :as controller]
   ; [re-con.controller-xr :as ctrl-xr]
   [re-con.test-scene-ecsy :as test-scene-ecsy]
   [promesa.core :as p]
   [components]
   [systems]
   [ecsy :refer (World System)] ;; works
   [babylonjs-materials :as bjs-m]
   ; ["babylonjs-loaders" :as bjs-l]
   ;; Note: the namespace for babylonjs-loaders is still "js/BABYLON"
   ;; However, we still need to reference the module here so the additional method
   ;; in fact get appended to the "js/BABYLON" namespace (so don't comment this line out)
   [babylonjs-loaders :as bjs-l]))
   ; [async-await.core :refer [async await]]))
   ; [promesa.async-cljs :refer-macros [async]]))
   ; [clojure.core.async :as async :refer :all]))

(def canvas)
(def engine)
(def scene)
(def env)
(def camera)
; (def camera-init-pos (js/BABYLON.Vector3. 0 5 -5))
(def camera-init-pos (js/BABYLON.Vector3. 0 4 -15))
; (def camera-init-pos (js/BABYLON.Vector3. 0 4 15))
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
(def whiteMaterial)
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
(def dummy)
(def model-scale-factor 0.01)
(def game-pad-mgr)
; (def model-scale-factor 1.00)
(def skeleton)
(def dribble-range)
(def defeated-range)
(def idle-range)
(def walking-range)

(declare init-part-2)
(declare init-basic-2-2)
; (declare setup-xr-ctrl-cbs)
(declare cast-ray)
(declare pointer-handler)
(declare set-scaling)

(defn mesh-selected [])

(defn dummy-fn []
  (println "m="))

(defn load-img-cb []
  (fn [task]
    (set! (.-diffuseTexture imgMat) (js/BABYLON.Texture. task.texture))))

(defn gamepad-evt-handler [gamepad state]
  (prn "gamepad-btn-handler: gamepad=" gamepad ", state=" state)
  (-> gamepad .-onButtonDownObservable (.add (fn [btn state]))))

(defn init []
  (println "now in init-scene")
  ;; following line necessary for mixamo animations.
  (set! js/BABYLON.Animation.AllowMatricesInterpolation true)
  (set! canvas (-> js/document (.getElementById "renderCanvas")))
  (set! engine (js/BABYLON.Engine. canvas true))
  (set! scene (js/BABYLON.Scene. engine))
  (re-frame/dispatch [:set-main-scene scene])
  (set! env (.-createDefaultEnvironment scene))
  (set! redMaterial (js/BABYLON.StandardMaterial. "redMaterial" scene))
  (set! (.-diffuseColor redMaterial) (js/BABYLON.Color3. 1 0 0))
  (set! blueMaterial (js/BABYLON.StandardMaterial. "blueMaterial" scene))
  (set! (.-diffuseColor blueMaterial) (js/BABYLON.Color3. 0 0 1))
  (set! greenMaterial (js/BABYLON.StandardMaterial. "greenMater ial" scene))
  (set! (.-diffuseColor greenMaterial) (js/BABYLON.Color3. 0 1 0))
  (set! whiteMaterial (js/BABYLON.StandardMaterial. "whiteMaterial" scene))
  (set! (.-diffuseColor whiteMaterial) (js/BABYLON.Color3. 1 1 1))
  (js/BABYLON.Debug.AxesViewer.)
  (set! ground (js/BABYLON.MeshBuilder.CreateGround. "ground" (js-obj "width" 20 "height" 20) scene))
  ; (set! (.-material ground) (js/BABYLON.GridMaterial. "mat" scene))
  (set! (.-material ground) (bjs-m/GridMaterial. "mat" scene))
  ; (set! (.-material ground) (bjs-m/SimpleMaterial. "mat" scene))
  (if (not base/use-xr)
    (do
      (println "now setting up vr")
      (set! vrHelper (.createDefaultVRExperience scene (js-obj "useXR" false)))
      ; var camera = new BABYLON.UniversalCamera("UniversalCamera", new BABYLON.Vector3(0, 0, -10), scene);
      (set! camera (.-webVRCamera vrHelper))
      (set! (.-id camera) "main-camera")
      (controller/init scene vrHelper camera)
      (controller/setup-controller-handlers vrHelper)
      ; var ground = BABYLON.MeshBuilder.CreateGround("ground", { width: 1000, height: 1000 }, scene);
      (.enableTeleportation vrHelper (js-obj "floorMeshName" "ground"))
      (.enableInteractions vrHelper)
      (js/setupControllers vrHelper)
      (init-part-2))
    (do
      (println "now setting up xr")
      ;; Note: this is not needed as we set camera later in the promise handler
      ;; Note: no, we still need this camera as it's the camera that used prior to clicking on "enter vr"
      ;; Note: rotations set here *are* propagated to the xr camera (upon entering full xr mode)
      (set! camera (js/BABYLON.UniversalCamera. "uni-cam" camera-init-pos scene))
      ;; Note: babylonjs is a left-handed system.  This means the pos x-axis is going into the screen not coming out of it.
      ;; hence we do *not* want to rotate by 180 deg. here.
      ; (set! (.-rotation camera) (js/BABYLON.Vector3. 0 js/Math.PI 0))
      (set! game-pad-mgr (js/BABYLON.GamepadManager. scene))
      (-> game-pad-mgr .-onGamepadConnectedObservable (.add gamepad-evt-handler))
      (prn "scene.gamepadManager=" (-> scene .-gamepadManager))
      ; (js-debugger)
      (-> (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env))))
          (p/then
           (fn [xr-default-exp]
             ;; note: x is a WebXRDefaultExperience
             ; (println "xr-x=" x ",scene=" scene)
             ; (set! xr x)
             (re-frame/dispatch [:setup-xr-ctrl-cbs xr-default-exp])
             ;; Note: baseExperience is of type WebXRExperienceHelper
             (set! features-manager (-> xr-default-exp (.-baseExperience) (.-featuresManager)))
             ; (.disableFeature features-manager "xr-controller-pointer-selection")
             ; (.enableFeature features-manager "xr-controller-pointer-selection")
             (println "xr features available=" (.GetAvailableFeatures js/BABYLON.WebXRFeaturesManager))
             (println "xr features acitve=" (-> xr-default-exp (.-baseExperience) (.-featuresManager) (.getEnabledFeatures)))
             ; (println "POINTERDOWN=" js/BABYLON.PointerEventTypes.POINTERDOWN)
             ; (println "POINTERPICK=" js/BABYLON.PointerEventTypes.POINTERPICK)
             ; (.registerBeforeRender scene cast-ray)
             ; (set! tmp-obj (js/BABYLON.MeshBuilder.CreateBox. "tmp-obj"
             ;                                                  (js-obj "height" 0.25, "width" 0.25, "depth" 0.25)
             ;                                                  scene))
             ; (set! (.-position tmp-obj) (js/BABYLON.Vector3. 0 0.5 0))
             ; (set! (.-position tmp-obj) (js/BABYLON.Vector3. 2 0.5 0))
             ; (set! (.-material tmp-obj) greenMaterial)
             (set! camera (-> xr-default-exp (.-baseExperience) (.-camera)))
             (set! (.-position camera) camera-init-pos)
             ;;Note: setting rotations on the xr camera here have no effect.  You have to do it
             ;; on the pre-xr camera (any rotations on that *will* propagate to the xr camera)
             ; (prn "opening-camera: rot (pre)=" (.-rotation camera))
             ; (set! (.-rotation camera) (js/BABYLON.Vector3. 0 js/Math.PI 0))
             ; (prn "opening-camera: rot (post)=" (.-rotation camera))
             ; (ctrl-xr/init scene xr)
             (re-frame/dispatch [:init-xr xr-default-exp])
             ;; note: ray stuff works, but we don't need
             ; (set! ray (js/BABYLON.Ray.))
             ; (set! ray-helper (js/BABYLON.RayHelper. ray))
             ; (.attachToMesh ray-helper tmp-obj (js/BABYLON.Vector3. 0 0 1) (js/BABYLON.Vector3. 0 0 0) 20)
             ; (.show ray-helper scene js/BABYLON.Color3. 255 0 0)
             (-> xr-default-exp (.-baseExperience)
                 (.-onStateChangedObservable)
                 (.add (fn [state]
                         (println "state=" state)
                         (when (= state js/BABYLON.WebXRState.IN_XR)
                           (println "state: in xr")
                           (println "state: old camera pos=" (.-position camera) ",camera-init-pos=" camera-init-pos)
                           ;;TODO: figure out why camera-init-pos not properly set here
                           ; (set! (.-position camera) camera-init-pos)
                           ; (set! (.-position camera) (js/BABYLON.Vector3. 0 4 -5))
                           (set! (.-position camera) (js/BABYLON.Vector3. 0 4 -10))
                           (set! (-> xr-default-exp .-baseExperience .-camera .-rotation) (js/BABYLON.Vector3. 0 js/Math.PI 0))
                           ; (set! (.-position camera) (js/BABYLON.Vector3. 0 4 10))
                           ; (set! (.-y (.-rotation camera)) js/Math.PI)
                           ; (set! (.-rotation camera) (js/BABYLON.Vector3. 0 js/Math.PI 0))
                           (println "state: new camera pos=" (.-position camera))))))

             (init-part-2)))))) ;; no work if no obj-moving in stanza
             ; (init-basic-2-2))))))
  (println "at end of init"))


(defn init-part-2 []
  (println "now in init-part-2")
  ;; svale adjustment
  ;; Note: need if not using webxr (browser) extension, comment out if you are.
  ;; Note: do rotations on the pre-xr camera, which will propagate to the xr camera. Doing it here
  ;; has no effect.
  ; (set! (.-rotationQuaternion camera) (-> js/BABYLON.Quaternion (.FromEulerAngles 0 (/ js/Math.PI 1) 0)))
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 0 5 -3) scene))
  (.setEnabled light1 true)
  ; var light = new BABYLON.HemisphericLight("HemiLight", new BABYLON.Vector3(0, 1, 0), scene);
  (js/BABYLON.HemisphericLight. "hemiLight" (js/BABYLON.Vector3. 0 1 0) scene)
  ;; need to have obj-moving for some reason
  (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
                    "status-panel"
                    (js-obj "height" 0.2
                            "width" 0.2
                            "depth" 0.1)
                    scene))
  (.attachControl camera canvas false)
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
; (defn cast-ray []
;   ; (println "now in cast-ray")
;   (let [origin (.-position tmp-obj)
;         fwd-1 (js/BABYLON.Vector3. 0 0 1)
;         fwd-2 (vec-to-local fwd-1 tmp-obj)
;         dir (js/BABYLON.Vector3.Normalize (.subtract fwd-2 origin))]
;     (set! ray (js/BABYLON.Ray. origin (js/BABYLON.Vector3. 0 0 0) 100))))
;   ; (set! picking-ray
;   ;   (.createPickingRay scene (.-pointerX scene) (.-pointerY scene) (.Identity js/BABYLON.Matrix) (.-activeCamera scene)))

; this sets up the pointer hooks for xr support
(defn pointer-handler [pointer-info]
  ; (macros/when-let* [a true b true] 7)
  ; (utils/kwd-to-int :7)
                     ; (let [type (.-type pointer-info)
  (macros/when-let* [type (.-type pointer-info)
                     ; picked-mesh (if (and (.-pickInfo pointer-info) (-> pointer-info (.-pickInfo) (.-pickedMesh))))
                     ;  (-> pointer-info (.-pickInfo) (.-pickedMesh))]
                     picked-mesh (-> pointer-info (.-pickInfo) (.-pickedMesh))]
      ; (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
      (println "POINTER DOWN, picked-mesh.id=" (.-id picked-mesh) ", picked-mesh=" picked-mesh ",
      type=" type ",POINTERDOWN=" js/BABYLON.PointerEventTypes.POINTERDOWN)
      ; (= type js/BABYLON.PointerEventTypes.POINTERPICK)
    ; (when (re-matches #"panel-\d+" (.-name picked-mesh)))
    ; (js-debugger)
    (when (re-matches #"rebus-panel-\d+" (.-name picked-mesh))
      (cond
        (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
        (do
          ; (println "pointer-handler: POINTER DOWN, picked-mesh=" picked-mesh)
          ; (js-debugger)
          (re-frame/dispatch [:mesh-selected picked-mesh])
          ; (re-frame/dispatch [:trigger-handler (js-obj "pressed" (.-pressed trigger-state))])
          ;  (re-frame/dispatch [:trigger-handler (js-obj "pressed" true)])
          (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" true)]))
        (= type js/BABYLON.PointerEventTypes.POINTERUP)
        (do
          ; (re-frame/dispatch [:trigger-handler (js-obj "pressed" false)])
          (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" false)]))
        :else nil))
    ; (when (re-matches #"game-tile.*" (.-name picked-mesh))
    ;   (println "game-tile selected")
    ;   (cond
    ;     (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
    ;     (do
    ;       (println "about to dispatch game-board-trigger-handler")
    ;       (re-frame/dispatch [:game-board-trigger-handler (js-obj "pressed" true)]))))
    (when (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
      (cond
        (re-matches #"game-tile.*" (.-name picked-mesh))
        (do
          (println "game-tile selected")
          ;; fire off a rebus mesh selected event and let it indirectly synchronize down to the game tile
          (let [rebus-mesh
                (-> scene (.getMeshByName (str "rebus-panel-" (utils/get-panel-index picked-mesh "game-tile"))))]
            (prn "rebus-mesh id=" (.-id rebus-mesh))
            (re-frame/dispatch [:mesh-selected rebus-mesh])
            ; (re-frame/dispatch [:rebus-panel-trigger-handler rebus-mesh])
            (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" true)])
            (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" false)])))))))
          ; (println "game-tile selected")
          ; (println "about to dispatch game-board-trigger-handler")
          ; (re-frame/dispatch [:game-board-trigger-handler picked-mesh]))))))


(defn set-scaling [mesh, s]
  (set! (.-scaling mesh) (js/BABYLON.Vector3. s s s)))
