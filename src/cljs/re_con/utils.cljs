(ns re-con.utils
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]))
   ; [re-con.main-scene :as main-scene]))

(defn get-panel-index
  "given a panel (babylon mesh) and a stem, return the index within the board.  Assumes a panel naming convention of '<stem>-xx'"
  [panel stem]
  (when panel
    (println "get-panel-index, panel=" panel ", stem=" stem)
    (js/parseInt (nth (re-matches (re-pattern (str stem "-(\\d+)")) (.-name panel)) 1))))

(defn get-front-panel-img [db panel]
  ((nth (db :rebus-board-cells) (get-panel-index panel "rebus-panel")) :front-img))


(defn get-game-tile-mesh [idx]
  "Get scene level mesh info for a game-tile")

;; Note: tile-model-loaded should be read-only on db
(defn tile-model-loaded [new-meshes particle-systems skeletons db tile-num]
  ; (println "new-meshes=" new-meshes)
  ; (println "count-abc=" (count new-meshes))
  ; (prn "skeleton count=" (count skeletons))
  ; (println "mesh-loaded: picked-mesh=" picked-mesh)
  ; (println "available animations=" (.-animations (get skeletons 0)))
  (doall (map #(set! (.-scaling %1) (js/BABYLON.Vector3.One))) new-meshes)
  ;; use the following on the original boxing
  ; (doall (map #(set! (.-scaling %1) (.scale (js/BABYLON.Vector3.One) base/mixamo-model-scale-factor)) skeletons))
  (let [main-scene (:main-scene db)
        tile-mesh (-> main-scene (.getMeshByName (str "game-tile-" tile-num)))]
    (doall (map #(set! (.-position %1) (.-position tile-mesh)) new-meshes))
    (doall (map #(set! (.-rotation %1) (js/BABYLON.Vector3. (-> %1 .-rotation .-x)
                                                            ; (+ (-> %1 .-rotation .-y) js/Math.PI)
                                                            0
                                                            (-> %1 .-rotation .-z))) new-meshes)))
  (when-let [mesh0 (get new-meshes 0)]
    ; (prn "id mesh0=" (.-id mesh0))
    (when (-> (.-id mesh0) (= "__root__"))
      (prn "model-loaded: setting _root_ id to " (str "__root__" tile-num))
      (set! (.-id mesh0) (str "__root__" tile-num))
      (.setEnabled mesh0 false))))
      ; (.setEnabled mesh0 true))))

(defn load-tile-set [db]
  (let [main-scene (:main-scene db)]
    ; (map #(.ImportMesh js/BABYLON.SceneLoader "" (:path %1) (:fn %1) main-scene #(tile-model-loaded %1 %2 %3)) (:game-cells db))
    (doseq [[i game-cell] (map-indexed vector (db :game-cells))]
      (.ImportMesh js/BABYLON.SceneLoader ""
                   (:path game-cell)
                   (:fn game-cell)
                   main-scene #(tile-model-loaded %1 %2 %3 db i)))))

(defn power-slave-loaded [new-meshes particle-systems skeletons]
  (prn "power-slave-loaded: new-meshes=" new-meshes)
  (prn "count-new-meshes=" (count new-meshes)))

(defn load-power-slave-pyr [db]
  (.ImportMesh js/BABYLON.SceneLoader ""
               "models/power_slave_pyr/"
               "power_slave_pyramid_2.glb"
               (:main-scene db)
               #(power-slave-loaded %1 %2 %3)))

(defn model-loaded [new-meshes particle-systems skeletons]
  (prn "model-loaded: new-meshes=" new-meshes)
  (prn "count new-messhes=" (count new-meshes)))

(defn load-model [db path fn]
  (.ImportMesh js/BABYLON.SceneLoader ""
               path
               fn
               (:main-scene db)
               #(model-loaded %1 %2 %3)))

;; convert a numeric keyword into an int e.g. :2 -> 2
(defn kwd-to-int [kwd]
  ; (println "hello")
  (let [parse_f #(second (re-find #":(\S+)" %1))
        int-val (-> kwd str parse_f js/parseInt)]
    ; (println "str(kwd)=" (str kwd))
    int-val))


;; generate a random order of the supplied user-seq, such that each member of the original
;; seq is represented exactly once, just in a random order e.g [0 1 2 3] -> [3 0 2 1]
(defn randomize-seq [user-seq]
  (loop [rnd-seq user-seq
         result (vector)]
    (if (= (count rnd-seq) 0)
      result
      (let [rnd-num (-> rnd-seq count dec rand-int)
            rnd-idx (nth rnd-seq rnd-num)
            ;; replace selected index with last index, and decrement size.  This insures
            ;; the previously selected index won't come up again, and that the last one eventually will.
            new-user-seq (pop (assoc rnd-seq rnd-num (last rnd-seq)))]
        (recur new-user-seq (conj result rnd-idx))))))

;[0 5 3 5 6 2 6 7 1 4 4 7 0 2 3 1]
;; generate a default image map for the board front images, given an image set (such as hotels).
; (defn gen-front-img-map [img-set]
;   (let [uniq-img-num (/ (* base/board-row-cnt base/board-col-cnt) 2)
;         linear-img-seq (into [] (flatten (for [i (range uniq-img-num)] (repeat 2 i))))
;         random-img-seq (randomize-seq linear-img-seq)]))


;; Note: rnd-seq should be 2x as long as coll (since every element in coll should be represeneted twice)
(defn randomize-coll-by-seq
  "return a collection in the same order as the index sequence in seq"
  [coll rnd-seq]
  (reduce #(conj %1 (get coll %2)) [] rnd-seq))


(defn rnd-board-seq
  "generate a common random set of indexes on db to later be used to randomize the game board and rebus board"
  []
  (let [uniq-cell-cnt (-> (* base/board-row-cnt base/board-col-cnt) (/ 2))
        board-indexes (vec (into (range uniq-cell-cnt) (range uniq-cell-cnt)))]
      rnd-board-seq (randomize-seq  board-indexes)))


;; defunct with intro of rnd-board-seq
;; this creates the random map for the rebus board
; (defn gen-front-img-map [img-set]
;   (let [uniq-img-num (/ (* base/board-row-cnt base/board-col-cnt) 2)
;         linear-img-seq (into [] (flatten (for [i (range uniq-img-num)] (repeat 2 i))))
;         rnd-img-seq (randomize-seq linear-img-seq)]
;     (println "rnd-img-seq=" rnd-img-seq)
;     (loop [i 0
;            img-idxs rnd-img-seq
;            result {}]
;       (if (pos? (count img-idxs))
;         (let [img-idx (first img-idxs)]
;           ; (println "img-idx=" img-idx)
;           (recur (inc i) (rest img-idxs) (conj result (hash-map (-> i str keyword) (nth img-set img-idx)))))
;         ;; return the result sorted numerically by key
;         (into (sorted-map-by (fn [k1 k2]
;                                (let [n1 (kwd-to-int k1)
;                                      n2 (kwd-to-int k2)]
;                                  (compare n1 n2)))) result)))))

;; defunct
;; when in xr mode, return the laser-pointer mesh attached to the controller model.
;; example: (get-xr-laser-pointer :left my-scene)
;; Note: this only works once a controller is actually attached e.g. on a 'onControllerAddedObservable' event.
; (defn get-xr-laser-pointer [hand scene]
;   ;; we start with the trigger mesh which is the only mesh that has the "hand" in its name
;   (let [trigger-mesh (str "generic-trigger " (name hand))
;         parent (.parent (.getMeshByID scene trigger-mesh))
;         ctrl-pointer-mesh (.getMeshByID scene "controllerPointer")]
;     ctrl-pointer-mesh))
;
; ;;defunct
; (defn get-left-xr-laser-pointer [scene]
;   (get-xr-laser-pointer :left scene))
;
; ;;defunct
; (defn get-right-xr-laser-pointer [scene]
;   (get-xr-laser-pointer :right scene))

;; not-defunct
; (defmacro when-let*
;           [bindings & body]
;           `(let ~bindings
;                 (if (and ~@(take-nth 2 bindings))
;                   (do ~@body))))
