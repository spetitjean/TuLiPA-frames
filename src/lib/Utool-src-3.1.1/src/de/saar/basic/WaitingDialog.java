package de.saar.basic;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * This class represents a <code>JDialog</code> with a Generic progress bar.
 * When the dialog appears (probably before a presumably longer task
 * starts), a indeterminated progress bar appears.
 * 
 * When the task is meant to be finished and the dialog is notified,
 * the progress bar will stop at its maximum length and an "OK" button
 * will be enabled to end the dialog.
 * 
 * @author Michaela Regneri
 *
 */
public class WaitingDialog extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = -8877214779756381415L;
	
	private final int tasklength = 1000;
	private JProgressBar progressBar;
	private JButton ok;
	private JPanel dialogPane;
		
	/**
	 * A new <code>WaitingDialog</code> initalised
	 * with its parent frame and the text to appear
	 * as dialog title.
	 * 
	 * @param text the dialog title
	 * @param owner the parent component
	 */
	public WaitingDialog(String text, Frame owner) {
		super(owner, text, false);
		dialogPane = new JPanel();
		progressBar = new JProgressBar(0, tasklength);
		
		// the OK-Button to press after printing is done
		// (it will close the dialog)
		ok = new JButton("OK");
		ok.setActionCommand("ok");
		
		// listener for the button 
		ok.addActionListener(this);
		
		progressBar.setStringPainted(true); 
		dialogPane.add(progressBar,BorderLayout.CENTER);
		dialogPane.add(ok,BorderLayout.SOUTH);
		dialogPane.doLayout();
		add(dialogPane);
		pack();
		validate();
		
//		 locating the panel centered
		setLocation((owner.getWidth() - getWidth())/2,
				(owner.getHeight() - getHeight())/2); 
		

	}
	
	/**
	 * To be called before the task starts.
	 * This will make the dialog visible and start the
	 * indeterminate progress bar.
	 * While the task is running, the "OK" button will
	 * be disabled.
	 *
	 */
	public void beginTask() {
	
				progressBar.setString(""); 
				progressBar.setIndeterminate(true);
				ok.setEnabled(false);
				setVisible(true);		
		
	}
	
	/**
	 * To be called when the task is finished.
	 * This will stop the progress bar end enable
	 * the ok button. Whenn OK is pressed, the dialog window
	 * will disappear.
	 */
	public void endTask() {
		progressBar.setMaximum(100);
		progressBar.setIndeterminate(false);
		progressBar.setValue(100);
		progressBar.setString("Finished.");
		
		// new text
		setTitle("Done!");
		
		dialogPane.validate();
		
		// enabling the button that closes
		// the dialog pane.
		ok.setEnabled(true);
	}

	
	/**
	 * This closes the window when the OK button has
	 * been pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("ok")) {
			setVisible(false);
		}
		
	}
	
}
