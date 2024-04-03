import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class Cell extends JPanel implements KeyListener, MouseListener,
		Runnable {

	public static final int ERROR_OVERLINE = 1;
	public static final int SPACE_OVERLINE = 2;
	private final int fontSize = 30;
	private final int annotationFontSize = 10;
	private final Font font = new Font("SansSerif", Font.BOLD, fontSize);
	private final Font annotationFont = new Font("SansSerif", Font.PLAIN,
			annotationFontSize);
	private final Color color = Color.black;
	private final Color highlightColor = new Color(200, 200, 200);
	private final Color annotationColor = new Color(50, 50, 50);
	private final Color unHighlightColor = Color.white;
	private final Color lockedColor = new Color(220, 220, 255);
	private final Color errorColor = new Color(255, 200, 200);
	private final Color errorLockedColor = new Color(255, 100, 255);
	private final Color overLineColor = new Color(255, 255, 0);
	private final Color errorOverLineColor = new Color(255, 0, 0);
	private final int selectedFlashPause = 500;

	private SudokuApplet s;
	private String text;
	private boolean[] annotations = new boolean[9];
	private boolean selected, selectedAnnotation, locked, enabled, error,
			highlighted, overlined, threadAlive;
	private int overlinetype;

	public void copyIn(Cell input) {
		unselect();
		text = "" + input.text.charAt(0);
		for (int i = 0; i <= 8; i++)
			annotations[i] = input.annotations[i];
		selected = input.selected;
		selectedAnnotation = input.selectedAnnotation;
		locked = input.locked;
		// enabled = input.enabled;
		error = input.error;
		highlighted = input.highlighted;
		overlined = input.overlined;
		threadAlive = input.threadAlive;
		overlinetype = input.overlinetype;
		repaint();
	}

	public void copyOut(Cell output) {
		output.text = "" + text.charAt(0);
		for (int i = 0; i <= 8; i++)
			output.annotations[i] = annotations[i];
		output.selected = selected;
		output.selectedAnnotation = selectedAnnotation;
		output.locked = locked;
		// output.enabled = enabled;
		output.error = error;
		output.highlighted = highlighted;
		output.overlined = overlined;
		output.threadAlive = threadAlive;
		output.overlinetype = overlinetype;
	}

	public Cell() {
	}

	public Cell(String initialText, SudokuApplet s) {
		text = initialText;
		this.s = s;
		selected = false;
		locked = false;
		enabled = true;
		error = false;
		overlined = false;
		threadAlive = false;
		setFocusable(false);
		addKeyListener(this);
		addMouseListener(this);
	}

	public void paintComponent(Graphics g) {
		// choose background color
		if (overlined)
			if (overlinetype == Cell.ERROR_OVERLINE)
				g.setColor(errorOverLineColor);
			else
				g.setColor(overLineColor);
		else if (selected & highlighted)
			g.setColor(highlightColor);
		else if (error)
			if (locked)
				g.setColor(errorLockedColor);
			else
				g.setColor(errorColor);
		else if (locked)
			g.setColor(lockedColor);
		else
			g.setColor(unHighlightColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		// display annotation highlight
		if (selectedAnnotation & highlighted) {
			g.setColor(highlightColor);
			g.fillRect(0, 0, getWidth(), annotationFontSize + 3);
		}
		// display digit
		g.setFont(font);
		g.setColor(color);
		g.drawString("" + text.charAt(0), getWidth() / 2 - fontSize / 4,
				getHeight() / 2 + fontSize / 2);
		g.setColor(annotationColor);
		g.setFont(annotationFont);
		g.drawString(annotations(), 2, annotationFontSize + 1);
	}

	public void keyPressed(KeyEvent k) {
		if (!locked & enabled) {
			char key = k.getKeyChar();
			int code = k.getKeyCode();
			String oldText = "" + text.charAt(0);
			boolean boardChangedFlag = false;
			if (selected) {
				if (key >= '1' & key <= '9')
					if (key == text.charAt(0))
						text = " ";
					else
						text = "" + key;
				else if (key == ' ' | code == KeyEvent.VK_BACK_SPACE
						| code == KeyEvent.VK_DELETE)
					text = " ";
				if (!text.equals(oldText))
					boardChangedFlag = true;
				unselect();
			} else if (selectedAnnotation) {
				if (key >= '1' & key <= '9') {
					int a = (int) key - 49;
					if (annotations[a])
						annotations[a] = false;
					else
						annotations[a] = true;
					boardChangedFlag = true;
				} else if (code == KeyEvent.VK_BACK_SPACE
						| code == KeyEvent.VK_DELETE) {
					for (int i = 0; i <= 8; i++)
						annotations[i] = false;
					boardChangedFlag = true;
				} else
					unselect();
			}
			repaint();
			if (boardChangedFlag)
				s.boardChanged();
		}
	}

	public void mousePressed(MouseEvent e) {
		if (!locked & enabled) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (!selected) {
					s.unselectAllCells();
					select();
				} else
					unselect();
				repaint();
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				if (!selectedAnnotation) {
					s.unselectAllCells();
					selectAnnotation();
				} else
					unselect();
				repaint();
			}
		}
	}

	public void clear() {
		overlined = false;
		if (!locked) {
			text = " ";
			repaint();
		}
		selected = false;
		error = false;
		repaint();
	}

	public void clearAnnotations() {
		for (int i = 0; i <= 8; i++)
			annotations[i] = false;
		repaint();
	}

	public String annotations() {
		String annotationString = "";
		for (int i = 0; i <= 8; i++)
			if (annotations[i])
				annotationString += (char) (49 + i);
		return annotationString;
	}

	public void setValue(int value) {
		overlined = false;
		if (value > 0 & value <= 9)
			text = (char) (value + 48) + "";
		else
			text = " ";
		repaint();
	}

	public int getValue() {
		int value = text.charAt(0) - 48;
		if (value < 1 | value > 9)
			value = 0;
		return value;
	}

	public void setError(boolean error) {
		this.error = error;
		overlined = false;
		repaint();
	}

	public boolean error() {
		return error;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled)
			selected = false;
		repaint();
	}

	public void lock() {
		locked = true;
		selected = false;
		selectedAnnotation = false;
		overlined = false;
		setToolTipText(null);
		repaint();
	}

	public void unlock() {
		locked = false;
		overlined = false;
		setToolTipText(s.cellTip);
		repaint();
	}

	public boolean isLocked() {
		return locked;
	}

	public void overline(int type) {
		overlinetype = type;
		overlined = true;
	}

	public void run() {
		threadAlive = true;
		highlighted = true;
		while (selected | selectedAnnotation) {
			try {
				Thread.sleep(selectedFlashPause);
			} catch (InterruptedException e) {
			}
			if (selected | selectedAnnotation) {
				if (highlighted)
					highlighted = false;
				else
					highlighted = true;
			} else
				highlighted = false;
			repaint();
		}
		threadAlive = false;
	}

	public void select() {
		if (!locked & enabled) {
			overlined = false;
			selected = true;
			if (!threadAlive)
				(new Thread(this)).start();
			setFocusable(true);
			requestFocusInWindow();
			repaint();
		}
	}

	public void selectAnnotation() {
		if (!locked & enabled) {
			overlined = false;
			selectedAnnotation = true;
			selected = false;
			if (!threadAlive)
				(new Thread(this)).start();
			setFocusable(true);
			requestFocusInWindow();
			repaint();
		}
	}

	public void unselect() {
		if (!locked & enabled) {
			overlined = false;
			selected = false;
			selectedAnnotation = false;
			setFocusable(false);
			repaint();
		}
	}

	public boolean isSelected() {
		return selected;
	}

	// EXTRA LISTENER STUBS...

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	public String toString()
	{
		return text.charAt(0)==' '?"0":""+text.charAt(0);
	}
}
