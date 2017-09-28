

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

(defun send-to-utool (writer)
  (let ((utool (socket:make-socket :remote-host *utool-host* 
				   :remote-port *utool-port*)))
    (funcall writer utool)
    (socket:shutdown utool :direction :output)
    (parse-xml utool)))

(defun make-scoped-mrs (mrs)
  (flet ((writer (os)
	   (format os "<utool cmd=\"solve\" output-codec=\"plugging-lkb\">")
	   (format os "<usr codec=\"mrs-prolog\" string=\"")
	   (mrs::output-mrs1 mrs 'mrs::prolog os)
	   (format os "\"/>")
	   (format os "</utool>")))
    (collect-solutions nil (send-to-utool #'writer))))

(defun display-mrs (edge)
  (let ((tree (lkb::deriv-tree-compute-derivation-tree edge))
	(mrs (mrs::extract-mrs edge)))
    (flet ((writer (os)
	     (format os "<utool cmd=\"display\">")
	     (format os "<usr codec=\"mrs-prolog\" name=\"&quot;~A&quot;\" string=\""
		     (prefix tree))
	     (mrs::output-mrs1 mrs 'mrs::prolog os)
	     (format os "\"/>")
	     (format os "</utool>")))
      (send-to-utool #'writer))))



(defun sdrow (acc tree)
  (cond ((stringp (car tree))
	 (cons (car tree) acc))
	(t
	 (reduce #'sdrow (cdddr tree) :initial-value acc))))

(defun prefix (tree)
  (let ((words (reverse (sdrow nil tree))))
    (case (length words)
      (0 "")
      (1 (format nil "~A" (nth 0 words)))
      (t (format nil "~A ~A ..." (nth 0 words) (nth 1 words))))))


(in-package :mrs)

(defvar *solver-internal*
    #'mrs::make-scoped-mrs)
(defvar *solver-utool*
    #'utool::make-scoped-mrs)

(defvar *solver* *solver-internal*)

(defun make-scoped-mrs (mrs)
  (funcall *solver* mrs))


(in-package :lkb)

(define-parse-tree-frame-command (com-multiple-tree-menu)
    ((tree 'prtree :gesture :select))
  (let ((command (clim:menu-choose
		  `(("Show enlarged tree" :value show)
                    ("Highlight chart nodes" :value chart) 
		    ("Partial chart" :value partial-chart)
                    ("Generate" :value generate :active ,*mrs-loaded*)
                    ("MRS" :value mrs :active ,*mrs-loaded*)
                    ("Prolog MRS" :value prolog :active ,*mrs-loaded*)
                    ("RMRS" :value rmrs :active ,*mrs-loaded*)
                    ("Indexed MRS" :value indexed :active ,*mrs-loaded*)
;;; {{{
		    ("[*] Display MRS [utool display]" 
		     :value display-utool :active ,*mrs-loaded*)
		    ("[*] Scoped MRS [utool solve]" 
		     :value scoped-utool :active ,*mrs-loaded*)
                    ("[*] Scoped MRS [use internal solver]" 
		     :value scoped :active ,*mrs-loaded*)
;;; }}}
                    ("Dependencies" :value dependencies :active ,*mrs-loaded*)
                    ("Rephrase" :value rephrase :active ,*mrs-loaded*)
                    ))))
    (when command
      (handler-case
	  (ecase command
	    (show (draw-new-parse-tree (prtree-top tree)
				       "Parse tree" nil 
                                       (parse-tree-frame-current-chart 
                                        clim:*application-frame*)))
            (chart
             (if (or (not (parse-tree-frame-current-chart 
                           clim:*application-frame*))
                     (eql (parse-tree-frame-current-chart 
                           clim:*application-frame*)
                     *chart-generation-counter*))
                 (progn
                   (cond ((and *main-chart-frame* 
                               (eql (clim:frame-state *main-chart-frame*) 
                                    :enabled))
                          nil)
                         ((and *main-chart-frame* 
                               (eql (clim:frame-state *main-chart-frame*) 
                                    :shrunk))
                          (clim:raise-frame *main-chart-frame*))
                         (t (show-chart) 
                            (mp:process-wait-with-timeout "Waiting" 
                                                          5 #'chart-ready)))
                   (display-edge-in-chart
                    (prtree-edge tree)))
               (lkb-beep)))
	    (partial-chart
	     (if (or (not (parse-tree-frame-current-chart 
                           clim:*application-frame*))
                     (eql (parse-tree-frame-current-chart 
                           clim:*application-frame*)
                     *chart-generation-counter*))
                 (multiple-value-bind (root subframe-p)
                   (cond ((and *main-chart-frame* 
                               (eql (clim:frame-state *main-chart-frame*) 
                                    :enabled))
			  (values
			   (chart-window-root *main-chart-frame*)
			   t))
                         ((and *main-chart-frame* 
                               (eql (clim:frame-state *main-chart-frame*) 
                                    :shrunk))
			  (values
			   (chart-window-root *main-chart-frame*)
			   t))
                         (t (values (construct-chart-no-display)
				    nil)))
		   (display-partial-chart root (prtree-edge tree)
					  subframe-p))
               (lkb-beep)))
            ;; funcall avoids undefined function warnings
            (generate (funcall 'really-generate-from-edge (prtree-edge tree)))
            (mrs (funcall 'show-mrs-window (prtree-edge tree)))
            (indexed (funcall 'show-mrs-indexed-window (prtree-edge tree)))
            (prolog (funcall 'show-mrs-prolog-window (prtree-edge tree)))
;;; {{{
            (scoped 
	     (setf mrs::*solver* mrs::*solver-internal*)
	     (funcall 'show-mrs-scoped-window (prtree-edge tree)))
	    (scoped-utool
	     (setf mrs::*solver* mrs::*solver-utool*)
	     (funcall 'show-mrs-scoped-window (prtree-edge tree)))
	    (display-utool
	     (funcall 'utool::display-mrs (prtree-edge tree)))
;;; }}}
            (rmrs (funcall 'show-mrs-rmrs-window (prtree-edge tree)))
            (dependencies 
             (funcall 'show-mrs-dependencies-window (prtree-edge tree)))
            (rephrase
             (let ((symbol (when (find-package :mt)
                             (find-symbol "REPHRASE" :mt))))
               (when (and symbol (fboundp symbol))
                 (funcall symbol (prtree-edge tree))))))
        (storage-condition (condition)
          (with-output-to-top ()
            (format t "~%Memory allocation problem: ~A~%" condition)))
	(error (condition)
	  (with-output-to-top ()
	    (format t "~%Error: ~A~%" condition)))
        (serious-condition (condition)
          (with-output-to-top ()
            (format t "~%Something nasty: ~A~%" condition)))))))
