(ns hara.protocol.block)

(defprotocol IBlock
  (-type   [_])        ;; returns one of *type*
  (-tag    [_])        ;; returns semantic representation
  (-string [_])        ;; convert node to string form
  (-length [_])        ;; length of the node
  (-width  [_])        ;; rows taken up by node
  (-height [_])        ;; cols, or length of last row
  (-prefixed [_])        ;; length of prefix
  (-suffixed [_])
  (-verify [_])        ;; check to see if the node contains valid data 
  )

(defprotocol IBlockModifier
  (-modify [_ accumulator input]))

(defprotocol IBlockExpression
  (-value  [_])         ;; returns the value
  (-value-string  [_])  ;; returns the string to be read in order to give a value 
  )

(defprotocol IBlockContainer
  (-children  [_])           ;; returns one of *type*
  (-replace-children  [_ children])   ;; returns one of *type*
  ;(-linebreaks  [_])         ;; returns number of linebreaks in the container
  ;(-last-row-length  [_])    ;; returns the last row length
  )
