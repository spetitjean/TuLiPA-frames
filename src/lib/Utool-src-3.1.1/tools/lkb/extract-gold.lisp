
;(eval-when (compile)
;  (unless (find-package :lkb) (make-package :lkb))
;  (unless (find-package :mrs) (make-package :mrs)))

(eval-when (compile load eval)
  (unless (find-package :utool) (make-package :utool)))

(in-package :utool)

(defun solve (mrs) 
  (handler-case
      (let ((solutions (mrs::make-scoped-mrs mrs)))
	(if (> mrs::*scoping-calls*
	       mrs::*scoping-call-limit*)
	    -1
	  (length solutions)))
    (error (condition) -2)))



(defun write-to-log-file (ostream mrs mrs-id)
  (let ((solutions (solve mrs)))
    (if (< solutions -1)
	(format ostream "~A ill-formed~%" mrs-id)
      (format ostream "~A well-formed (~A solutions)~%" mrs-id solutions))))



(defun extract (result-file prefix output-fn solve)
  (flet 
      ((extract (result-file output-fn log)
	 (with-open-file (istream result-file)
	   (do ((line (read-line istream nil nil)
		      (read-line istream nil nil)))
	       ((null line))
	     (let ((temp (excl:split-regexp "@" line)))
	       (let ((idx1 (nth 0 temp))
		     (idx2 (nth 1 temp))
		     (mrs (mrs::read-mrs-from-string (nth 13 temp))))
		 (let ((mrs-id (format nil "~A-~A" idx1 idx2)))
		   (if log
		       (write-to-log-file log mrs mrs-id))
		   (funcall output-fn mrs mrs-id))))))))
    (if solve
	(with-open-file (logstream (format nil "~A/log" prefix)
			 :direction :output
			 :if-exists :supersede)
	  (extract result-file output-fn logstream))
      (extract result-file output-fn nil))))

(defun extract-prolog (result-file prefix &key solve)
  (flet ((output-mrs (mrs file-id)
	   (let ((mrs-file (format nil "~A/~A.mrs.pl" prefix file-id)))
	     (mrs::output-mrs mrs 'mrs::prolog mrs-file))))
    (extract result-file prefix #'output-mrs solve)))

(defun extract-xml (result-file prefix &key solve)
  (flet ((output-mrs (mrs file-id)
	   (let ((mrs-file (format nil "~A/~A.mrs.xml" prefix file-id)))
	     (mrs::output-mrs mrs 'mrs::mrs-xml mrs-file))))
    (extract result-file prefix #'output-mrs solve)))
