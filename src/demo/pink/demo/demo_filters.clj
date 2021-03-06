(ns pink.demo.demo-filters
 (:require [pink.simple :refer :all]
             [pink.event :refer :all] 
             [pink.space :refer [pan]] 
             [pink.oscillators :refer :all]
             [pink.envelopes :refer :all]
             [pink.util :refer :all]
             [pink.noise :refer :all]
             [pink.filters :refer :all]
             [pink.delays :refer [adelay]]
             [pink.config :refer :all]
             ))

(defn setup-filter
  [filterfn & args]
  (pan (mul 0.5 (apply filterfn (white-noise) args))
       0.0))

(defn test-filter
  [filterfn & args]
  (add-audio-events
    (apply event setup-filter 0.0 filterfn args)))

(comment

  (def score 
    (events test-filter 
            [0.0 butterlp (env [0.0 20 5 20000])]
            [5.0 butterhp (env [0.0 20 5 20000])]
            [10.0 butterbp (env [0.0 20 5 20000]) 100]
            [15.0 butterbr (env [0.0 20 5 20000]) 1000]
            ))

  (add-audio-events score)

  (start-engine)

  ;; Individual tests

  (test-filter butterlp (env [0.0 20 10 20000]))
  (test-filter butterlp (env [0.0 20 10 20000]))
  (test-filter butterhp (env [0.0 20 5 20000]))
  (test-filter butterbp (env [0.0 20 5 20000]) 100 )
  (test-filter butterbr (env [0.0 20 5 20000]) 1000 )
  (test-filter butterbr (env [0.0 20 5 20000]) 100 )
  (test-filter moogladder (env [0.0 20 10 20000]) 0.1 )
  (test-filter biquad-lpf (env [0.0 20 5 20000]) 0.4 )
  (test-filter biquad-hpf (env [0.0 20 5 20000]) 0.1 )
  (test-filter biquad-bpf (env [0.0 20 5 20000]) 0.9 )
  (test-filter biquad-notch (env [0.0 20 5 20000]) 0.6 )
  (test-filter biquad-peaking (env [0.0 20 5 20000]) 0.6 12 )
  (test-filter biquad-peaking (env [0.0 20 5 20000]) 0.9 -24)
  (test-filter biquad-lowshelf (env [0.0 20 5 20000]) 0.6 12)
  (test-filter biquad-lowshelf (env [0.0 20 5 20000]) 0.9 -24)
  (test-filter biquad-highshelf (env [0.0 20 5 20000]) 0.6 12 )
  (test-filter biquad-highshelf (env [0.0 20 5 20000]) 0.9 -24)

  (doseq [_ (range 5)] 
    (add-afunc
      (let [pch (+ 60 (rand-int 400))] 
        (let-s [;ampenv (xar 0.025 1.5)
                ampenv (env [0.0 0.0 0.025 1.0 0.025 0.9 1.0 0.9 2.0 0.0])
                cutenv (env [0.0 (* 6.0 pch) 0.025 (* 3.0 pch) 3.025 (* 3.0 pch)])
                ] 
          (pan (mul 0.5 ampenv
                    (moogladder (sum (mul 0.9 (blit-saw pch)) 
                                     (mul 0.2 (blit-saw (* pch 1.5)))) 
                                (sum (* pch 3) (mul (* pch 4) ampenv)) 
                                ;cutenv
                                ;(* pch 5)
                                0.85 

                                ))
               (- 1 (/ (rand-int 200) 100.0))))))) 

  ;(add-afunc (mul 0.5 (butterlp (white-noise) (env [0.0 20 5 20000]))))

  (add-afunc
    (with-signals [[hp _ _ _] (statevar (white-noise) (env [0.0 20 5 10000]) 0.8)]
      (pan hp 0.0)))

  (add-afunc
    (with-signals [[_ lp _ _] (statevar (white-noise) (env [0.0 20 5 10000]) 0.8)]
      (pan lp 0.0)))

  (add-afunc
    (with-signals [[_ _ bp _] (statevar (white-noise) (env [0.0 20 5 10000]) 0.8)]
      (pan bp 0.0)))

  (add-afunc
    (with-signals [[_ _ _ br] (statevar (white-noise) (env [0.0 20 5 10000]) 0.8)]
      (pan br 0.0)))


  (defn example [freq] 
    (let-s [ramp-env (env [0.0 0.0 5.0 1.0 10.0 0.0])
            e (adsr140 
                (sine2 (sum 3.0 (mul ramp-env 15.0))) 
                0 
                0.04 0.02 0.9 0.15)] 
      (-> 
        (sum (blit-saw freq)
                  (blit-saw (mul freq 1.002581)))
        (mul e ramp-env)
        (moogladder (sum 1000 (mul 500 6 e ramp-env)) 0.6)
        (pan 0.0)
        )))

  (add-afunc (example 660.0))
  (add-afunc (example 550.0))
  (add-afunc (example 880.0))
  (add-afunc (example 440.0))
  (add-afunc (example 1320.0))
  (add-afunc (example 1000.0))

  (defn example2 [freq] 
    (let-s [ramp-env (env [0.0 0.0 5.0 1.0 10.0 0.0])
            e (adsr140 
                (sine2 (sum 3.0 (mul ramp-env 15.0))) 
                0 
                0.04 0.02 0.9 0.15)] 
      (let [s (sum (blit-saw freq) (blit-saw (mul freq 1.002581)))
            source (mul e ramp-env s)
            filtered (statevar source (sum 400 (mul 400 6 e ramp-env)) 0.5)]
        (with-signals [[hp lp _ _] filtered] 
          (->
            (sum (mul (sub 1.0 ramp-env) lp) 
                 (mul ramp-env hp))
            (pan 0.0))))))

  (add-afunc (example2 100.0))
  (add-afunc (example2 400.0))
  (add-afunc (example2 600.0))
  (add-afunc (example2 900.0))
  (add-afunc (example2 1200.0))
  (add-afunc (example2 1800.0))

  (add-afunc
    (let [asig (shared (blit-saw (exp-env [0.0 2000 3.0 20])))
            l (comb asig 3.5 0.1)
            r (comb asig 3.5 0.02)
            ^"[[D" out (create-buffers 2)]
      (fn []
        (let [a (l) b (r)]
          (when (and a b)
            (aset out 0 a)
            (aset out 1 b)
            out)))))

(add-afunc
  (let [asig (shared (blit-saw (exp-env [0.0 2000 3.0 20])))
        l (combinv asig 3.5 0.1)
        r (combinv asig 3.5 0.02)
        ^"[[D" out (create-buffers 2)]
    (fn []
      (let [a (l) b (r)]
        (when (and a b)
          (aset out 0 a)
          (aset out 1 b)
          out)))))


  (defn example3 [freq] 
    (let-s [ramp-env (env [0.0 0.0 5.0 1.0 10.0 0.0])
            e (adsr140 
                (unirect (env [0.0 8.0 10.0 1.0]) (env [0.0 0.6 10.0 0.1])) 
                0 
                0.04 0.02 0.9 0.15)] 
      (-> 
        (sum (blit-saw freq)
                  (blit-saw (mul freq 1.002581)))
        (mul e ramp-env)
        (moogladder (sum 1000 (mul 500 6 e ramp-env)) 0.6)
        (pan 0.0)
        )))


  (add-afunc (example3 1200.0))
  (add-afunc (example3 1800.0))

  (add-afunc
    (->
     (blit-saw 220) 
     (lpf18 (env [0.0 10000 5 20]) 0.5 1)
     (pan 0.0)))

  (add-afunc
    (->
     (white-noise) 
     (zdf-1pole (env [0.0 10000 5 20]))
     (pan 0.0)))

  (add-afunc
    (->
     (white-noise) 
     (zdf-1pole (env [0.0 20 5 10000]) 1)
     (pan 0.0)))

  (add-afunc
    (->
     (white-noise) 
     (zdf-1pole (env [0.0 20 5 10000]) 2)
     (pan 0.0)))
 

  (add-afunc
    (->
     (white-noise) 
     (zdf-2pole (env [0.0 10000 5 20]) 4)
     (pan 0.0)))

  (add-afunc
    (->
     (white-noise) 
     (zdf-2pole (exp-env [0.0 20 5 10000]) 4 1)
     (pan 0.0)))

  (add-afunc
    (->
     (white-noise) 
     (zdf-2pole (exp-env [0.0 20 5 10000]) 4 2)
     (pan 0.0)))


  (add-afunc
    (->
     (white-noise) 
     (zdf-ladder (exp-env [0.0 5000 5 20]) 25)
     (pan 0.0)))

  (add-afunc
    (->
     (white-noise) 
     (k35-lpf (exp-env [0.0 5000 0.5 1000]) 9.95)
     (pan 0.0)))
  
  (add-afunc
    (->
     (white-noise) 
     (k35-hpf (exp-env [0.0 10000 5 20]) 10)
     (pan 0.0)))

  (add-afunc
    (->
     (dust2 (exp-env [0.0 20000 4 10 1 10])) 
     (zdf-ladder (exp-env [0.0 4000 5 3000]) 10)
     (pan 0.0)))

  (add-afunc 
    (with-duration 0.5 
      (let [e (shared (adsr 0.01 0.2 0.09 0.15))]
        (->
          (sum (blit-saw 220) (blit-square 110)) 
          (k35-hpf 400 7)
          (k35-lpf 
            (sum 110 (mul 3000 e)) 9.5)
          ;(zdf-2pole (sum 200 (mul 3000 e)) 8)
          ;(get-channel 0)
          (mul e 0.25)
          (pan 0.0)
          ))))

  (add-afunc 
    (with-duration 0.25 
      (let [e (shared (adsr 0.01 0.00 1.0 0.15))]
        (->
          (blit-saw 110) 
          (diode-ladder (sum 200 (mul (adsr 0.01 0.25 0.0001 0.25) 6000)) 
                        10 :norm 4)
          (mul e)
          (pan 0.0)
          ))))
)

