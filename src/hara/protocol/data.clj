(ns hara.protocol.data)

(defprotocol ICurried
  (-arity [f]))
  
(defprotocol IFunctor
  (-fmap
    [fv g]
    [fv g fvs]))
        
(defprotocol IApplicative
  (-pure [av v])
  (-fapply [ag av] [ag av avs]))

(defprotocol IMonad
  (-bind [mv g] [mv g mvs])
  (-join [mv]))

(defprotocol IMagma
  (-op [x y] [x y ys]))

(defprotocol IMonoid
  (-id [m]))

(defprotocol IFoldable
  (-fold [fd])
  (-foldmap [fd g]))

(defprotocol ITraversable
  (-traverse [f tv]))
