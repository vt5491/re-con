(ns re-con.utils
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]))

(defn get-panel-index
  "given a panel (babylon mesh), return the index within the board.  Assumes a panel naming convention of 'panel-xx'"
  [panel]
  ; (println "get-panel-index, panel name=" (.-name panel) ",match=" (re-matches #"panel-(\d+)" (.-name panel)))
  ; (println "result=" (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name panel)) 1)))
  (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name panel)) 1)))

(defn get-front-panel-img [db panel]
  ((nth (db :board-cells) (get-panel-index panel)) :front-img))


; (defn gen-img-map-0 [img-set]
;   ; (loop))
;   (let [result []]
;     (doseq [[i img] (map-indexed vector img-set)]
;       (println "gen-img-map: img=" img)
;       (conj result))))
;
; (defn gen-img-map3 [img-set]
;   (let [board-size (* base/board-row-cnt base/board-col-cnt)
;         empty-img-map (vec (repeat board-size nil))]))
;
;
; (defn gen-img-map-4
;   ([img-set]
;    (println "gen-img-map path a")
;    (let [board-size (* base/board-row-cnt base/board-col-cnt)
;          used-index-vec (vec (range 0 board-size))
;          unique-img-cnt (/ board-size 2)
;          empty-img-map (vec (repeat board-size nil))
;          rnd1 (rand-int board-size)
;          used-result-1 (assoc used-index-vec rnd1 (nth used-index-vec (dec board-size)))
;          rnd2 (rand-int (dec board-size))
;          used-result-2 (assoc used-result-1 rnd2 (nth used-index-vec (- board-size 2)))]
;      (gen-img-map-4 img-set 1 used-result-2 (assoc empty-img-map rnd1 (img-set rnd1)))))
;   ([img-set img-index used-index-vec result]
;    (let [board-size (* base/board-row-cnt base/board-col-cnt)
;          unique-img-cnt (/ board-size 2)
;          index (if-not img-index unique-img-cnt img-index)]
;      (println "gen-img-map path b img-index=" img-index ",used-index-vec=" used-index-vec ",result=" result))))
;
; (defn gen-img-map [unique-img-vec]
;   (let [board-size 16
;         unique-img-seq (vec (range 0 (/ board-size 2)))
;         ; used-index-vec (vec (range 0 board-size))
;         img-map (vec (repeat board-size nil))]
;     (loop [loop-index (/ board-size 2)
;            used-index-vec (vec (range 0 board-size))
;            result img-map]
;       (if (neg? loop-index)
;         result
;         (do
;           (println "result=" result)
;           (recur (dec loop-index)
;             used-index-vec
;             (do
;               ; (println "hello")
;               (let [dbl-loop-idx (* loop-index 2)]
;                 (assoc result dbl-loop-idx (used-index-vec (rand-int (inc dbl-loop-idx)))
;                        (+ dbl-loop-idx 1) (used-index-vec (rand-int dbl-loop-idx)))))))))))

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

(defn gen-front-img-map [img-set]
  (let [uniq-img-num (/ (* base/board-row-cnt base/board-col-cnt) 2)
        linear-img-seq (into [] (flatten (for [i (range uniq-img-num)] (repeat 2 i))))
        rnd-img-seq (randomize-seq linear-img-seq)]
    (println "rnd-img-seq=" rnd-img-seq)
    (loop [i 0
           img-idxs rnd-img-seq
           result {}]
      (if (pos? (count img-idxs))
        (let [img-idx (first img-idxs)]
          ; (println "img-idx=" img-idx)
          (recur (inc i) (rest img-idxs) (conj result (hash-map (-> i str keyword) (nth img-set img-idx)))))
        ;; return the result sorted numerically by key
        (into (sorted-map-by (fn [k1 k2]
                               (let [n1 (kwd-to-int k1)
                                     n2 (kwd-to-int k2)]
                                 (compare n1 n2)))) result)))))
;; when in xr mode, return the laser-pointer mesh attached to the controller model.
;; example: (get-xr-laser-pointer :left my-scene)
;; Note: this only works once a controller is actually attached e.g. on a 'onControllerAddedObservable' event.
(defn get-xr-laser-pointer [hand scene]
  ;; we start with the trigger mesh which is the only mesh that has the "hand" in its name
  (let [trigger-mesh (str "generic-trigger " (name hand))
        parent (.parent (.getMeshByID scene trigger-mesh))
        ctrl-pointer-mesh (.getMeshByID scene "controllerPointer")]
    ctrl-pointer-mesh))


(defn get-left-xr-laser-pointer [scene]
  (get-xr-laser-pointer :left scene))

(defn get-right-xr-laser-pointer [scene]
  (get-xr-laser-pointer :right scene))
