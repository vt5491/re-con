;; main_scene should reference few and be accessible by many
;; note: "/src/cljs" appears to be the absolute "root" dir if you're trying to refer to a js file.
;; However, since this file is in the 'cljs' dir of the absolute root, relative to this file
;; '/src/cljs/re_pure_ecs_simple' is the 'root'.  You *can* referback to the parent dir 'src/cljs'
;; because it *is* the absoltue root and thus accessible.
;; You can't ".." (referback) beyond the absolute root though.
(ns re-con.test-scene-ecsy
  (:require
   [babylonjs]
   [promesa.core :as p]
   [components]
   [systems]
   [ecsy :refer (World System)])) ;; works

(def scene)
(def engine)
(def light-1)
(def camera)
(def canvas)
(def renderer)
(def obj-moving)
(def world)
(def last-time)
(def object-3d)
(def cube-entity)
(def xr)
(def env)


(defn set-movable-system-queries []
  (set! systems/MovableSystemWrapper.queries
    (js-obj "moving"
            (js-obj "components"
                    (array components/Position components/Velocity)))))

(defn set-rotating-system-queries []
  (set! systems/RotatingSystemWrapper.queries
    (js-obj "entities"
            (js-obj "components"
                    (array components/Rotating, components/Object3D)))))

(defn init []
  (println "test-scene-ecsy.init: entered")
  (set! object-3d (components/Object3D.)) ;; works
  (println "init: object-3d=" object-3d)
  (set-movable-system-queries)
  (set-rotating-system-queries)
  (set! world (ecsy/World.))
  (println "init: world=" world)
  (set! canvas (-> js/document (.getElementById "renderCanvas")))
  (set! engine (js/BABYLON.Engine. canvas true))
  (println "init: engine4=" engine)
  (set! scene (js/BABYLON.Scene. engine))
  (set! env (.-createDefaultEnvironment scene))
  ; var xrHelper = await scene.createDefaultXRExperienceAsync();
  ; (set! xr-help)
  ; (set! xr-helper (.createDefaultXRExperienceAsync scene))
  ; (p/do!
   ; (set! xr (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env))))))
  (set! xr (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env)))))
  (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene))
  (.setTarget camera (js/BABYLON.Vector3.Zero))
  (.attachControl camera canvas false)
  (set! light-1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
  (.setEnabled light-1 true)

  (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
                    "status-panel"
                    (js-obj "height" 2
                            "width" 2
                            "depth" 0.1
                            scene)))
  (let [mat (js/BABYLON.StandardMaterial.)]
    (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
    (set! (.-material obj-moving) mat))
  (-> (.-systemManager world) (.registerSystem systems/MovableSystemWrapper))
  (-> (.-systemManager world) (.registerSystem systems/RotatingSystemWrapper))

  (set! cube-entity (-> (.-entityManager world) (.createEntity)))
  (.addComponent cube-entity components/Position)
  (.addComponent cube-entity components/Velocity (js-obj "vx" 0.2, "vy" 0.2))
  (.addComponent cube-entity components/Object3D (js-obj "object" obj-moving))
  (.addComponent cube-entity components/Rotating (js-obj "rotatingSpeed" 0.5))
  (set! last-time (/ (js/performance.now) 1000))
  (println "init: about to runRenderLoop")
  (.runRenderLoop engine (fn []
                           (let [time (-> (js/performance.now) (/ 1000))
                                 delta (- time last-time)]
                             (set! last-time time)
                             (.render scene)
                             (.execute world delta time)))))


(defn ^:export do-it [pos]
  (println "main_scene.do-it: pos.x=" (.-x pos) ", pos.y=" (.-y pos)))



(defn movable-system-execute [this delta time]
  (let [results this.queries.moving.results]
    (doseq [entity results]
      (let [obj (-> (.getComponent entity components/Object3D) (.-object))
            pos (.-position obj)
            vel (-> (.getComponent entity components/Velocity))]
        (set! (.-x pos) (+ (.-x pos) (* (.-vx vel) delta)))
        (set! (.-y pos) (+ (.-y pos) (* (.-vy vel) delta)))))))

(defn rotating-system-execute [this delta]
  (let [results this.queries.entities.results]
    (doseq [entity results]
      (let [rot-speed (-> entity (.getComponent components/Rotating) (.-rotatingSpeed))
            obj (-> entity (.getComponent components/Object3D) (.-object))
            rot (.-rotation obj)]
        (set! (.-x rot) (+ (.-x rot) (* rot-speed delta)))
        (set! (.-y rot) (+ (.-y rot) (* rot-speed delta)))
        (set! (.-z rot) (+ (.-z rot) (* rot-speed delta)))))))
