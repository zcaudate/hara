(ns hara.string.base.ansi
  (:require [hara.string.base.common :as common]))

(def ^:dynamic *custom*
  {:warn      :yellow
   :info      :blue
   :error     :red
   :critical  :magenta
   :highlight :green})

(defonce +colors+
  {30 :grey
   31 :red
   32 :green
   33 :yellow
   34 :blue
   35 :magenta
   36 :cyan
   37 :white
   90 :bright-grey
   91 :bright-red
   92 :bright-green
   93 :bright-yellow
   94 :bright-blue
   95 :bright-magenta
   96 :bright-cyan
   97 :bright-white})

(defonce +highlights+
  {40 :on-grey
   41 :on-red
   42 :on-green
   43 :on-yellow
   44 :on-blue
   45 :on-magenta
   46 :on-cyan
   47 :on-white})

(defonce +attributes+
  {0  :reset
   1  :bold
   2  :dark
   3  :italic
   4  :underline
   5  :blink
   7  :reverse-color
   8  :concealed
   22 :normal})

(defonce +lookup+
  (reduce-kv (fn [out k v] (assoc out v k))
             {}
             (merge +colors+ +highlights+ +attributes+)))

(defn encode-raw
  "encodes the raw ansi modifier codes to string
 
   (encode-raw [30 20])
   => \"[30;20m\""
  {:added "3.0"}
  [codes]
  (str "\033[" (common/join codes ";") "m"))

(defn encode
  "encodes the ansi characters for modifiers
   (encode :bold)
   => \"[1m\"
 
   (encode :red)
   => \"[31m\""
  {:added "3.0"}
  [& modifiers]
  (let [codes (mapv (fn [modifier]
                      (or (+lookup+ modifier)
                          (+lookup+ (get *custom* modifier))
                          (throw (ex-info "Modifier not available." {:input modifier}))))
                    modifiers)]
    (encode-raw codes)))

(defn style
  "styles the text according to the modifiers
 
   (style \"hello\" [:bold :red])
   => \"[1;31mhello[0m\""
  {:added "3.0"}
  [text modifiers]
  (str (apply encode modifiers) text (encode :reset)))

(defn- ansi-form [modifier]
  (let [prefix (encode modifier)
        func  (-> modifier name symbol)]
    `(defn ~func [& ~'args]
       (-> ~'args
           (->> (map (fn [~'x] (str ~prefix ~'x)))
                (common/join))
           (str ~(encode :reset))))))

(defn define-ansi-forms
  "defines ansi forms given by the lookups
 
   ;; Text:
   ;; [blue cyan green grey magenta red white yellow]
 
   (blue \"hello\")
   => \"[34mhello[0m\"
 
   ;; Background:
   ;; [on-blue on-cyan on-green on-grey
   ;;  on-magenta on-red on-white on-yellow]
 
   (on-white \"hello\")
   => \"[47mhello[0m\"
 
   ;; Attributes:
   ;; [blink bold concealed dark reverse-color underline]
 
   (blink \"hello\")
   => \"[5mhello[0m\""
  {:added "3.0"}
  []
  (->> (dissoc +lookup+ :reset)
       (keys)
       (mapv (comp eval ansi-form))))

(define-ansi-forms)
