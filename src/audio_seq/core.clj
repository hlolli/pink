(ns audio-seq.core
  (:import (javax.sound.sampled
                       AudioFormat
                       AudioFormat$Encoding
                       AudioFileFormat
                       AudioFileFormat$Type
                       AudioInputStream
                       AudioSystem
           DataLine$Info SourceDataLine)
        (java.nio ByteBuffer)
        (java.io File ByteArrayInputStream)))

(def af (AudioFormat. 44100 16 1 true true))

(def buffer-size 1024)

(def ^:dynamic *sr* 44100)


;(def audio-stream (AudioInputStream/getAudioInputStream
;(def f (File/createTempFile "tmp" ".raw" (File. "."))) 
;(println (bean f))

(def ^:const PI Math/PI)

(defn ^doubles phasor [^double freq ^double phase]
  (let [phase-incr (/ freq  *sr*)]
    (iterate #(let [p (+ % phase-incr)] (if (> p 1) (dec p) p)) phase)))

(defn ^doubles sine-osc [freq phase]
    (map #(Math/sin (* 2 PI %)) (phasor freq phase)))


(def ^doubles sine-table  
  (double-array 
    (map #(Math/sin (* 2.0 PI (/ % 4096.0))) 
                    (take 4096 (iterate inc 0)))))

(defn ^doubles sine-osc2 [freq phase]
  (map #(aget ^doubles sine-table ^int (int ^double (* 4095.0 %))) (phasor freq phase))) 

(defn amix 
  ([] [])
  ([& a] 
     (let [len (count a)]
       (if (= len 1)
         (first a) 
         (apply map + a)))))

(defn amul
  ([] [])
  ([& a] 
     (let [len (count a)]
       (if (= len 1)
         (first a) 
         (apply map * a)))))

(defn env-lin 
  [& xs]
    (lazy-seq 
      (let [[p1 p2 ] xs cnt 0]
         (when p2 
           (cons p1 (apply env-lin (rest xs )))))))
   

(def audio-block
  (map #(* 0.25 %)
  (amix 
    (sine-osc 440.0 0)
    (sine-osc 660.0 0)
    (sine-osc 990.0 0)
    (sine-osc 1220.0 0)
    ))
  )

;(def audio-block
;    (sine-osc 660.0 0)
;    )

; JAVASOUND CODE

(defn open-line [audio-format]
  (let [#^SourceDataLine line (AudioSystem/getSourceDataLine audio-format)]
    (doto line 
    (.open audio-format)
    (.start))))

(defn run-audio-block [a-block]
  (let [#^SourceDataLine line (open-line af)
        audio-block a-block]
    (let [cnt (/ (* *sr* 5.0) buffer-size)
        buffer (ByteBuffer/allocate buffer-size)]
      (loop [c cnt 
         [x & xs] (partition (/ buffer-size 2) audio-block)] 
       (when (and (> c 0) x)
         (loop [[a & b] x]
           (when a
             (.putShort buffer (.shortValue (* Short/MAX_VALUE a)))
             (recur b))) 
         (.write line (.array buffer) 0 buffer-size)
         (.clear buffer)
      (recur (dec c) xs ))))
    (.close line)))

(defn audio-block2 []
  (map #(* 0.01 %) 
       (apply amix 
              (map #(sine-osc (* % 60) 0) 
                   (take 30 (iterate inc 1))))))
        
(defn demo [] (run-audio-block audio-block))
(defn demo2 [] (run-audio-block (audio-block2)))
;(def audio-out-proxy 
;  (proxy [AudioInputStream]
;    (

