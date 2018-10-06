(ns hara.core.base.error)

;; ## Errors
;;
;; If we place too much importance on exceptions, exception handling code
;; starts littering through the control code. Most internal code
;; do not require definition of exception types as exceptions are
;; meant for the programmer to look at and handle.
;;
;; Therefore, the exception mechanism should get out of the way
;; of the code. The noisy `try .... catch...` control structure
;; can be replaced by a `suppress` statement so that errors can be
;; handled separately within another function or ignored completely.
;;

(defmacro suppress
  "Suppresses any errors thrown in the body.
 
   (suppress (throw (ex-info \"Error\" {}))) => nil
 
   (suppress (throw (ex-info \"Error\" {})) :error) => :error
 
   (suppress (throw (ex-info \"Error\" {}))
             (fn [e]
               (.getMessage e))) => \"Error\""
  {:added "3.0"}
  ([body]
   `(try ~body (catch Throwable ~'t)))
  ([body catch-val]
   `(try ~body (catch Throwable ~'t
                 (cond (fn? ~catch-val)
                       (~catch-val ~'t)
                       :else ~catch-val)))))