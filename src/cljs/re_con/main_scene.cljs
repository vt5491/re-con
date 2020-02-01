;; main_scene should reference few and be accessible by many
(ns re-con.main-scene
  (:require
    ;; actually don't need to require babylonjs as it's in the js namespace already.
   ; [babylonjs]
   ; [promesa.core :as p :refer-macros [async]]
   ; [re-con.utils as utils]))
   [re-con.base :as base]
   [re-con.controller :as controller]
   [re-con.controller-xr :as ctrl-xr]
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

(declare init-part-2)
(declare init-basic-2-2)
; (declare setup-xr-ctrl-cbs)
(declare cast-ray)

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
             (ctrl-xr/setup-xr-ctrl-cbs xr)
             ; (js-debugger)
             (println "xr features available=" (.GetAvailableFeatures js/BABYLON.WebXRFeaturesManager))
             (println "xr features acitve=" (-> xr (.-baseExperience) (.-featuresManager) (.getEnabledFeatures)))
             (.registerBeforeRender scene cast-ray)
             (set! tmp-obj (js/BABYLON.MeshBuilder.CreateBox. "tmp-obj"
                                                              (js-obj "height" 0.25, "width" 0.25, "depth" 0.25)
                                                              scene))
             (set! (.-position tmp-obj) (js/BABYLON.Vector3. 0 0.5 0))
             (set! (.-material tmp-obj) greenMaterial)
             (set! camera (-> x (.-baseExperience) (.-camera)))
             (ctrl-xr/init scene xr)
             (set! ray (js/BABYLON.Ray.))
             (set! ray-helper (js/BABYLON.RayHelper. ray))
             (.attachToMesh ray-helper tmp-obj (js/BABYLON.Vector3. 0 0 1) (js/BABYLON.Vector3. 0 0 0) 20)
             (.show ray-helper scene js/BABYLON.Color3. 255 0 0)
             (init-part-2)))))) ;; no work if no obj-moving in stanza
             ; (init-basic-2-2))))))
  (println "at end of init"))
                                                     ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene)))))))
                                                     ; (set! camera (-> x (.-baseExperience) (.-camera))))))))

  ; ; var camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(0, 5, -10), scene);
  ; ; (set! xr (.createDefaultXRExperienceAsync scene))
  ; ; (p/do!
  ; ;  (println "xr about to set")
  ; ;  (set! xr (.createDefaultXRExperienceAsync scene))
  ; ;  (println "xr set"))
  ; ; (controller/init scene vrHelper) ;;vt-x
  ; ; (-> vrHelper .-onNewMeshSelected (.add (fn [] (println "new mesh selected"))))
  ; ; (set! camera (.-webVRCamera vrHelper)) ;;vt-x
  ; ; (.resetToCurrentRotation camera)
  ; ; (.rotationQuaternion camera (.-FromEulersAngles (.-Quaternion js/BABYLON) 0 (/ js/Math.PI 2) 0))
  ; (println "abc=" (-> js/BABYLON.Quaternion (.FromEulerAngles 0 1 0)))
  ; ; (.rotationQuaternion camera (FromEulersAngles js/BABYLON.Quaternion 0 (/ js/Math.PI 2) 0))
  ; ;; in scruz, have to rotate camera 180 deg for some reason to get proper vr camera alignment with board.
  ; ; (set! (.-rotationQuaternion camera) (-> js/BABYLON.Quaternion (.FromEulerAngles 0 (/ js/Math.PI 1) 0)))
  ; ; (js/setupLoaders scene)
  ; (set! redMaterial (js/BABYLON.StandardMaterial. "redMaterial" scene))
  ; (set! (.-diffuseColor redMaterial) (js/BABYLON.Color3. 1 0 0))
  ; (set! blueMaterial (js/BABYLON.StandardMaterial. "blueMaterial" scene))
  ; (set! (.-diffuseColor blueMaterial) (js/BABYLON.Color3. 0 0 1))
  ; (set! greenMaterial (js/BABYLON.StandardMaterial. "greenMaterial" scene))
  ; (set! (.-diffuseColor greenMaterial) (js/BABYLON.Color3. 0 1 0))
  ; (set! imgMat (js/BABYLON.StandardMaterial. "imgMat" scene))
  ; (set! assetsManager (js/BABYLON.AssetsManager. scene))
  ; (set! textureTask (.addTextureTask assetsManager "load-texture" "imgs/burj_al_arab.jpg"))
  ; (set! textureTask.onSuccess (load-img-cb))
  ; (.load assetsManager)
  ; ; (.enableInteractions vrHelper)
  ; (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
  ; (.setEnabled light1 true)
  ; (set! gui-3d-manager (js/BABYLON.GUI.GUI3DManager. scene)))

(defn init-part-2 []
  (println "now in init-part-2")
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 -3) scene))
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
  (.setEnabled light1 true))
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

; (defn run-scene-2 []
;   (.runRenderLoop engine (fn []
;                            (let [time (-> (js/performance.now) (/ 1000))
;                                  delta (- time last-time)]
;                              (set! last-time time)
;                              (.render scene)))))
;
; (defn init-basic []
;   (set! canvas (-> js/document (.getElementById "renderCanvas")))
;   (set! engine (js/BABYLON.Engine. canvas true))
;   (set! scene (js/BABYLON.Scene. engine))
;   (set! env (.-createDefaultEnvironment scene))
;   ; (set! xr (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env)))))
;   (set! xr (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env)))))
;   ; (-> (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env))))
;   ;     (p/then (fn [x](set! xr x))))
;   (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene))
;   (.setTarget camera (js/BABYLON.Vector3.Zero))
;   (.attachControl camera canvas false)
;   (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
;   (.setEnabled light1 true)
;   (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
;                     "status-panel"
;                     (js-obj "height" 2
;                             "width" 2
;                             "depth" 0.1
;                             scene)))
;   (let [mat (js/BABYLON.StandardMaterial.)]
;     (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
;     (set! (.-material obj-moving) mat))
;   (.runRenderLoop engine (fn []
;                            (let [time (-> (js/performance.now) (/ 1000))
;                                  delta (- time last-time)]
;                              (set! last-time time)
;                              (.render scene)))))
;                              ; (.execute world delta time)))))
;
; (defn init-basic-2 []
;   (set! canvas (-> js/document (.getElementById "renderCanvas")))
;   (set! engine (js/BABYLON.Engine. canvas true))
;   (set! scene (js/BABYLON.Scene. engine))
;   (set! env (.-createDefaultEnvironment scene))
;   ; (set! xr (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env)))))
;   ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene))
;   (when base/use-xr
;     ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene))
;     (-> (.createDefaultXRExperienceAsync scene)
;         (p/then (fn [x]
;                   (println "x=" x)
;                   (set! xr x)
;                   (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene))))))
;   ; (.setTarget camera (js/BABYLON.Vector3.Zero))
;   ; (.attachControl camera canvas false)
;   (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
;   (.setEnabled light1 true)
;   (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
;                     "status-panel"
;                     (js-obj "height" 2
;                             "width" 2
;                             "depth" 0.1
;                             scene)))
;   (let [mat (js/BABYLON.StandardMaterial.)]
;     (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
;     (set! (.-material obj-moving) mat))
;   (.runRenderLoop engine (fn []
;                            (let [time (-> (js/performance.now) (/ 1000))
;                                  delta (- time last-time)]
;                              (set! last-time time)
;                              (.render scene)))))
;
; (defn init-basic-2-2 []
;   (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
;   (.setEnabled light1 true)
;   (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
;                     "status-panel"
;                     (js-obj "height" 0.02
;                             "width" 0.02
;                             "depth" 0.1
;                             scene)))
;   (let [mat (js/BABYLON.StandardMaterial.)]
;     (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
;     (set! (.-material obj-moving) mat))
;   (.runRenderLoop engine (fn []
;                            (let [time (-> (js/performance.now) (/ 1000))
;                                  delta (- time last-time)]
;                              (set! last-time time)
;                              (.render scene)))))
;
; (defn init-basic-ecsy []
;   (println "test-scene-ecsy.init: entered")
;   (set! object-3d (components/Object3D.)) ;; works
;   (println "init: object-3d=" object-3d)
;   (test-scene-ecsy/set-movable-system-queries)
;   (test-scene-ecsy/set-rotating-system-queries)
;   (set! world (ecsy/World.))
;   (println "init: world=" world)
;   (set! canvas (-> js/document (.getElementById "renderCanvas")))
;   (set! engine (js/BABYLON.Engine. canvas true))
;   (println "init: engine4=" engine)
;   (set! scene (js/BABYLON.Scene. engine))
;   (set! env (.-createDefaultEnvironment scene))
;   (set! xr (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env)))))
;   (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene))
;   (.setTarget camera (js/BABYLON.Vector3.Zero))
;   (.attachControl camera canvas false)
;   (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
;   (.setEnabled light1 true)
;
;   (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
;                     "status-panel"
;                     (js-obj "height" 2
;                             "width" 2
;                             "depth" 0.1
;                             scene)))
;   (let [mat (js/BABYLON.StandardMaterial.)]
;     (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
;     (set! (.-material obj-moving) mat))
;   (-> (.-systemManager world) (.registerSystem systems/MovableSystemWrapper))
;   (-> (.-systemManager world) (.registerSystem systems/RotatingSystemWrapper))
;
;   ; (set! cube-entity (-> (.-entityManager world) (.createEntity)))
;   ; (.addComponent cube-entity components/Position)
;   ; (.addComponent cube-entity components/Velocity (js-obj "vx" 0.2, "vy" 0.2))
;   ; (.addComponent cube-entity components/Object3D (js-obj "object" obj-moving))
;   ; (.addComponent cube-entity components/Rotating (js-obj "rotatingSpeed" 0.5))
;   (set! last-time (/ (js/performance.now) 1000))
;   (println "init: about to runRenderLoop")
;   (.runRenderLoop engine (fn []
;                            (let [time (-> (js/performance.now) (/ 1000))
;                                  delta (- time last-time)]
;                              (set! last-time time)
;                              (.render scene)
;                              (.execute world delta time)))))
;
;
