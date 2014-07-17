(ns pink.audio.oscillators
  "Oscillator Functions"
  (:require [pink.audio.engine :refer [*sr*]]
            [pink.audio.util :refer [create-buffer fill map-d 
                                     swapd! setd! getd arg]]
            [pink.audio.gen :refer [gen-sine]] 
            ))

(def ^:const PI Math/PI)

(defmacro dec-if 
  [a] 
  `(if (>= ~a 1.0) (dec ~a) ~a))

(defn phasor 
  "Phasor with fixed frequency and starting phase"
  [^double freq ^double phase]
  (let [phase-incr ^double (/ freq  *sr*)
        cur-phase (double-array 1 phase)
        out (create-buffer)]
      (fn ^doubles [] 
        (fill out cur-phase #(dec-if (+ phase-incr ^double %))))))

(defn sine 
  "Sine generator with fixed frequency and starting phase"
  ([^double freq]
   (sine freq 0.0))
  ([^double freq ^double phase]
   (let [phsr (phasor freq phase)
         out (create-buffer)]
     (fn ^doubles []
       (map-d out #(Math/sin (* 2.0 PI ^double %)) (phsr))))))


(defmacro phs-incr
  [cur incr phs-adj]
  `(dec-if (+ ~cur ~incr ~phs-adj)))

(defn vphasor [freq phase]
  "Phasor with variable frequency and phase (where freq and phase are generator
  functions"
  (let [out ^doubles (create-buffer)
        cur-phase (double-array 1 0)
        len (alength ^doubles out)
        lastindx (dec len)]
    (fn ^doubles [] 
      (let [f (freq)
            p (phase)]
        (when (and f p)
          (loop [cnt (unchecked-int 0)]
            (when (< cnt len)
              (let [incr ^double (/ (aget ^doubles f cnt) *sr*)
                    phs-adj (aget ^doubles p cnt)] 
                (aset out cnt (setd! cur-phase (phs-incr (getd cur-phase) incr phs-adj)))
                (recur (unchecked-inc-int cnt)))) 
            )
          out)))))

(defn sine2 
  "Sine generator with variable frequency and phase (where freq and phase are
  generator functions"
  ([f]
   (sine2 f 0))
  ([f p]
   (let [phsr (vphasor (arg f) (arg p))
         out (create-buffer)]
     (fn ^doubles []
       (map-d out #(Math/sin (* 2.0 PI ^double %)) (phsr))))))

(def sine-table (gen-sine))

;; fixme - handle amplitude as function by updating map-d to take in multiple buffers
(defn oscil
  "Oscillator with table (defaults to sine wave table, truncates indexing)"
  ([amp freq]
   (oscil amp freq sine-table 0))
  ([amp freq table]
   (oscil amp freq table 0))
  ([amp freq ^doubles table phase]
   (let [phsr (vphasor (arg freq) (arg phase))
         out (create-buffer)
         tbl-len (alength table)]
      (fn ^doubles []
        (map-d out #(* amp (aget table (int (* % tbl-len)))) (phsr))))))


(defn oscili
  "Linear-interpolating oscillator with table (defaults to sine wave table)"
  ([amp freq]
   (oscili amp freq sine-table 0))
  ([amp freq table]
   (oscili amp freq table 0))
  ([amp freq ^doubles table phase]
   (let [phsr (vphasor (arg freq) (arg phase))
         out (create-buffer)
         tbl-len (alength table)]
      (fn ^doubles []
        (map-d out 
               #(let [phs (* % tbl-len)
                      pt0 (int phs)
                      pt1 (mod (inc pt0) tbl-len)  
                      frac (if (zero? pt0) 
                             phs
                             (rem phs pt0))
                      v0  (aget table pt0)
                      v1  (aget table pt1)]
                 (* amp 
                   (+ v0 (* frac (- v1 v0))))) 
               (phsr))))))


(defn oscil3
  "Cubic-interpolating oscillator with table (defaults to sine wave table) (not yet implemented!)"
  ([amp freq]
   (oscil3 amp freq sine-table 0))
  ([amp freq table]
   (oscil3 amp freq table 0))
  ([amp freq ^doubles table phase]
   (let [phsr (vphasor (arg freq) (arg phase))
         out (create-buffer)
         tbl-len (alength table)]
      (fn ^doubles []
        (map-d out #(* amp (aget table (int (* % tbl-len)))) (phsr))))))
