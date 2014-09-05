(defproject kunstmusik/pink "0.1.0-SNAPSHOT"
  :jvm-opts ;["-server" "-Xmx2g" "-XX:-UseParallelGC"]
  ["-server"
   "-Xms256m" "-Xmx1g"           ; Minimum and maximum sizes of the heap
   "-XX:+UseG1GC"
   "-XX:MaxGCPauseMillis=10"     ; Specify a target of 20ms for max gc pauses
   "-XX:MaxNewSize=257m"         ; Specify the max and min size of the new
   "-XX:NewSize=256m"            ;  generation to be small
   "-XX:+UseTLAB"                ; Uses thread-local object allocation blocks. This
   ;  improves concurrency by reducing contention on
   ;  the shared heap lock.
   "-XX:MaxTenuringThreshold=0" ; Makes the full NewSize available to every NewGC 
   ]
  :description "A library for music research, composition, and performance."

  :url "http://github.com/kunstmusik/pink"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]]

  :profiles  { 
              :dev  {
                     :global-vars  {*warn-on-reflection* true}
                     :dependencies [[criterium "0.4.2"]] 
                     :plugins [[codox "0.8.8"]] } 

              :profiling {
                     :plugins [[lein-nodisassemble "0.1.3"]] }      
              }

  ;:main 
  )
