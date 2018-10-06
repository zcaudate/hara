(ns hara.protocol.mail)

(defprotocol IMailer
  (-send-mail [mailer message])
  (-send-bulk [mailer messages]))

(defprotocol IMailbox
  ;;(-has-new-mail [mailbox])
  (-init-mail   [mailbox messages])
  (-count-mail  [mailbox])
  ;;(-update-mail [mailbox i add delete])
  (-get-mail    [mailbox i])
  (-list-mail   [mailbox])
  (-clear-mail  [mailbox]))

(defmulti -create-mailer
  "creates a mailer for use with components"
  {:added "3.0"}
  :type)

(defmulti -create-mailbox
  "creates a mailbox for use with components"
  {:added "3.0"}
  :type)
