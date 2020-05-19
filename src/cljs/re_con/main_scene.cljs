;; main_scene should reference few and be accessible by many
(ns re-con.main-scene
  (:require-macros [re-con.macros :as macros])
  (:require
   [re-frame.core :as re-frame]
    ;; actually don't need to require babylonjs as it's in the js namespace already.
   [babylonjs :as BABYLON]
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

(def canvas)
(def engine)
(def scene)
(def env)
(def camera)
(def camera-init-pos (js/BABYLON.Vector3. 0 4 -15))
(def vrHelper)
(def xr)
(def ground)
(def fps-pnl)
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
(declare setup-skybox)

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
  (set! greenMaterial (js/BABYLON.StandardMaterial. "greenMaterial" scene))
  (set! (.-diffuseColor greenMaterial) (js/BABYLON.Color3. 0 1 0))
  (set! whiteMaterial (js/BABYLON.StandardMaterial. "whiteMaterial" scene))
  (set! (.-diffuseColor whiteMaterial) (js/BABYLON.Color3. 1 1 1))
  (js/BABYLON.Debug.AxesViewer.)
  (prn "multiview=" (-> scene .getEngine .getCaps .-multiview))
  (setup-skybox)
  (set! fps-pnl (js/BABYLON.MeshBuilder.CreateBox.
                   "fps-panel"
                   (js-obj "width" 2.50 "height" 2.50 "depth" 0.1)
                   scene))
  (let [dyn-texture (js/BABYLON.DynamicTexture. "fps-pnl-texture" (js-obj "width" 256 "height" 60) scene)
        fps-pnl-mat (js/BABYLON.StandardMaterial. "fps-panel-mat" scene)]
    (set! (.-position fps-pnl) (js/BABYLON.Vector3. -8 10 7))
    (set! (.-material fps-pnl) fps-pnl-mat)
    (set! (.-diffuseTexture fps-pnl-mat) dyn-texture)
    (.drawText (-> fps-pnl .-material .-diffuseTexture) "60" 50 50 "60px green" "white" "blue" true true))

  ;; instrumentation gui
  (if (not base/use-xr)
    (do
      (println "now setting up vr")
      (set! vrHelper (.createDefaultVRExperience scene (js-obj "useXR" false)))
      (set! camera (.-webVRCamera vrHelper))
      (set! (.-id camera) "main-camera")
      (controller/init scene vrHelper camera)
      (controller/setup-controller-handlers vrHelper)
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
      (-> (.createDefaultXRExperienceAsync scene (js-obj
                                                  "floorMeshes" (array (.-ground env))))
                                                  ; "useMultiview" true))
          (p/then
           (fn [xr-default-exp]
             ;; note: x is a WebXRDefaultExperience
             (re-frame/dispatch [:setup-xr-ctrl-cbs xr-default-exp])
             ;; Note: baseExperience is of type WebXRExperienceHelper
             (set! features-manager (-> xr-default-exp (.-baseExperience) (.-featuresManager)))
             (println "xr features available=" (.GetAvailableFeatures js/BABYLON.WebXRFeaturesManager))
             (println "xr features acitve=" (-> xr-default-exp (.-baseExperience) (.-featuresManager) (.getEnabledFeatures)))
             (set! camera (-> xr-default-exp (.-baseExperience) (.-camera)))
             (set! (.-position camera) camera-init-pos)
             ;;Note: setting rotations on the xr camera here have no effect.  You have to do it
             ;; on the pre-xr camera (any rotations on that *will* propagate to the xr camera)
             (re-frame/dispatch [:init-xr xr-default-exp])
             (-> xr-default-exp (.-baseExperience)
                 (.-onStateChangedObservable)
                 (.add (fn [state]
                         (println "state=" state)
                         (when (= state js/BABYLON.WebXRState.IN_XR)
                           (println "state: in xr")
                           (println "state: old camera pos=" (.-position camera) ",camera-init-pos=" camera-init-pos)
                           ;;TODO: figure out why camera-init-pos not properly set here
                           (set! (.-position camera) (js/BABYLON.Vector3. 0 4 -10))
                           (set! (-> xr-default-exp .-baseExperience .-camera .-rotation) (js/BABYLON.Vector3. 0 js/Math.PI 0))
                           (println "state: new camera pos=" (.-position camera))))))

             (init-part-2)))))) ;; no work if no obj-moving in stanza
  (println "at end of init"))


(defn init-part-2 []
  (println "now in init-part-2")
  ;; svale adjustment
  ;; Note: need if not using webxr (browser) extension, comment out if you are.
  ;; Note: do rotations on the pre-xr camera, which will propagate to the xr camera. Doing it here
  ;; has no effect.
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 0 5 -3) scene))
  (.setEnabled light1 true)
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

(defn vec-to-local [vector mesh]
  (let [m (.getWorldMatrix mesh)
        v (js/BABYLON.Vector3.TransformCoordinates vector m)]
    v))

; this sets up the pointer hooks for xr support
(defn pointer-handler [pointer-info]
  (macros/when-let* [type (.-type pointer-info)
                     ; picked-mesh (if (and (.-pickInfo pointer-info) (-> pointer-info (.-pickInfo) (.-pickedMesh))))
                     picked-mesh (-> pointer-info (.-pickInfo) (.-pickedMesh))]
      ; (println "POINTER DOWN, picked-mesh.id=" (.-id picked-mesh) ", picked-mesh=" picked-mesh ",
      ; type=" type ",POINTERDOWN=" js/BABYLON.PointerEventTypes.POINTERDOWN)
    (when (re-matches #"rebus-panel-\d+" (.-name picked-mesh))
      (cond
        (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
        (do
          (re-frame/dispatch [:mesh-selected picked-mesh])
          (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" true)]))
        (= type js/BABYLON.PointerEventTypes.POINTERUP)
        (do
          (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" false)]))
        :else nil))
    (when (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
      (cond
        (re-matches #"game-tile.*" (.-name picked-mesh))
        (do
          (println "game-tile selected")
          ;; fire off a rebus mesh selected event and let it indirectly synchronize down to the game tile
          (let [rebus-mesh
                (-> scene (.getMeshByName (str "rebus-panel-" (utils/get-panel-index picked-mesh "game-tile"))))]
            (prn "rebus-mesh id=" (.-id rebus-mesh))
            (re-frame/dispatch [:play-tile-selected-snd])
            (re-frame/dispatch [:mesh-selected rebus-mesh])
            (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" true)])
            (re-frame/dispatch [:rebus-panel-trigger-handler (js-obj "pressed" false)])))))))


(defn set-scaling [mesh, s]
  (set! (.-scaling mesh) (js/BABYLON.Vector3. s s s)))

  ; var skybox = BABYLON.MeshBuilder.CreateBox("skyBox", {size:1000.0}, scene);
  ; var skyboxMaterial = new BABYLON.StandardMaterial("skyBox", scene);
  ; skyboxMaterial.backFaceCulling = false;
  ; skyboxMaterial.reflectionTexture = new BABYLON.CubeTexture("textures/skybox", scene);
  ; skyboxMaterial.reflectionTexture.coordinatesMode = BABYLON.Texture.SKYBOX_MODE;
  ; skyboxMaterial.diffuseColor = new BABYLON.Color3(0, 0, 0);
  ; skyboxMaterial.specularColor = new BABYLON.Color3(0, 0, 0);
  ; skybox.material = skyboxMaterial);
(defn setup-skybox []
  ; (let [skybox (BABYLON/Vector3. 1 1 1)]))
  (let [skybox (BABYLON/MeshBuilder.CreateBox. "sky-box" (js-obj "size" 1000.0) scene)
        skybox-mat (BABYLON/StandardMaterial. "sky-box" scene)]
    (set! (.-backFaceCulling skybox-mat) false)
    (set! (.-reflectionTexture skybox-mat) (BABYLON/CubeTexture. "textures/skybox/skybox" scene))
    (set! (-> skybox-mat .-reflectionTexture .-coordinatesMode) (-> BABYLON .-Texture .-SKYBOX_MODE))
    (set! (-> skybox-mat .-diffuseColor) (BABYLON/Color3. 0 0 0))
    (set! (-> skybox-mat .-specularColor) (BABYLON/Color3. 0 0 0))
    (set! (.-material skybox) skybox-mat)))
; (defn setup-skybox []
;   (let [skybox (js/BABYLON.MeshBuilder.CreateBox. "sky-box" (js-obj "size" 1000.0) scene)
;         skybox-mat (js/BABYLON/StandardMaterial. "sky-box" scene)]
;     (set! (.-backFaceCulling skybox-mat) false)
;     (set! (.-reflectionTexture skybox-mat) (js/BABYLON.CubeTexture. "textures/skybox/skybox" scene))
;     (set! (-> skybox-mat .-reflectionTexture .-coordinatesMode) (-> js/BABYLON .-Texture .-SKYBOX_MODE))
;     (set! (-> skybox-mat .-diffuseColor) (js/BABYLON.Color3 0 0 0))
;     (set! (-> skybox-mat .-specularColor) (js/BABYLON.Color3 0 0 0))
;     (set! (.-material skybox) skybox-mat)))
