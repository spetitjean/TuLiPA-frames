
;; integration of utool into lkb (needs Allegro CL)

;(eval-when (compile)
;  (unless (find-package :lkb) (make-package :lkb))
;  (unless (find-package :mrs) (make-package :mrs)))

(eval-when (compile load eval)
  (unless (find-package :utool) (make-package :utool)))

(in-package :utool)

(defvar *utool-port* 2802)
(defvar *utool-host* "localhost")

(defun collect-solutions (acc elt)
  (cond ((consp elt)
	 (case (car elt)
	   (|solution|
	    (apply #'collect-solution acc elt))
	   (t
	    (reduce #'collect-solutions elt :initial-value acc))))
	(t acc)))

(defun collect-solution (acc _ _ solution)
  (cons (read-from-string solution) acc))

(defun parse-xml (istream)
  (let ((*package* (find-package :utool)))
    (xml:parse-xml istream :content-only istream)))

(defun make-scoped-mrs (mrs)
  (let ((utool (socket:make-socket :remote-host *utool-host* 
				   :remote-port *utool-port*)))
    (format utool "<utool cmd=\"solve\" output-codec=\"plugging-lkb\">")
    (format utool "<usr codec=\"mrs-prolog\" string=\"")
    (mrs::output-mrs1 mrs 'mrs::prolog utool)
    (format utool "\"/>")
    (format utool "</utool>")

    (socket:shutdown utool :direction :output)

    (collect-solutions nil (parse-xml utool))))

(in-package :mrs)

(defun make-scoped-mrs (mrs)
  (utool::make-scoped-mrs mrs))
