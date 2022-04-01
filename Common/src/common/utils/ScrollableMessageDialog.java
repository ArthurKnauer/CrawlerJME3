package common.utils;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author VTPlusAKnauer
 */
public class ScrollableMessageDialog {

	private ScrollableMessageDialog() {
	}

	public static void showException(String title, Exception ex) {
		StringWriter stringWriter = new StringWriter();
		ex.printStackTrace(new PrintWriter(stringWriter));

		ScrollableMessageDialog.show(title, stringWriter.toString());
	}

	public static void show(String title, String text) {
		JTextArea textArea = new JTextArea(text);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane.setPreferredSize(new Dimension(1024, 600));
		JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.ERROR_MESSAGE);
	}

}
