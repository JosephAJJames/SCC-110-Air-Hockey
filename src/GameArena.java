import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.lang.Class;
import java.lang.reflect.*;

/**
 * This class provides a simple window in which grahical objects can be drawn.
 * @author DJ
 */
public class GameArena extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener
{
	// Size of playarea
	private JFrame frame;
	private int player1Score = 0;

	private int player2Score = 0;

	private int arenaWidth;
	private int arenaHeight;

	private boolean exiting = false;

	private ArrayList<Object> things = new ArrayList<Object>();

	private HashMap<String, Color> colours = new HashMap<>();

	private boolean up = false;
	private boolean down = false;
	private boolean left = false;
	private boolean right = false;
	private boolean up2 = false;
	private boolean down2 = false;
	private boolean left2 = false;
	private boolean right2 = false;
	private boolean shift = false;
	private boolean space = false;
	private boolean esc = false;
	private boolean enter = false;
	private boolean[] alphasPressed = new boolean[26];
	private final static int asciiAZOffset = 65;
	private final static int alphabetLength = 25;
	private boolean leftMouse = false;
	private boolean rightMouse = false;
	private int mouseX = 0;
	private int mouseY = 0;

	private BufferedImage buffer;
	private Graphics2D graphics;
	private Map<RenderingHints.Key, Object> renderingHints;
	private boolean rendered = false;
	private Image backgroundImage = null;

	private char KorM = 'm';

	/**
	 * Create a view of a GameArena.
	 *
	 * @param width The width of the playing area, in pixels.
	 * @param height The height of the playing area, in pixels.
	 */
	public GameArena(int width, int height)
	{
		this.init(width, height, true);
	}

	/**
	 * Create a view of a GameArena.
	 *
	 * @param width The width of the playing area, in pixels.
	 * @param height The height of the playing area, in pixels.
	 * @param createWindow Defines if a window should be created to host this GameArena. @see getPanel.
	 */
	public GameArena(int width, int height, boolean createWindow)
	{
		this.init(width, height, createWindow);
	}


	public boolean getKorM(char charter) //returns true or false if the passed in character matches KorM
	{
		if (charter == KorM) {
			return true;
		} else {
			return false;
		}
	}

	public void setKorM(char character) //sets KorM's value
	{
		this.KorM = character;
	}

	/**
	 * Internal initialisation method - called by constructor methods.
	 */
	private void init(int width, int height, boolean createWindow)
	{
		if (createWindow)
		{
			this.frame = new JFrame();
			frame.setTitle("Let's Play!");
			frame.setSize(width, height);
			frame.setResizable(false);
			frame.setBackground(Color.BLACK);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(this);
			frame.setVisible(true);
		}

		this.setSize(width, height);

		// Add standard colours.
		colours.put("BLACK", Color.BLACK);
		colours.put("BLUE", Color.BLUE);
		colours.put("CYAN", Color.CYAN);
		colours.put("DARKGREY", Color.DARK_GRAY);
		colours.put("GREY", Color.GRAY);
		colours.put("GREEN", Color.GREEN);
		colours.put("LIGHTGREY", Color.LIGHT_GRAY);
		colours.put("MAGENTA", Color.MAGENTA);
		colours.put("ORANGE", Color.ORANGE);
		colours.put("PINK", Color.PINK);
		colours.put("RED", Color.RED);
		colours.put("WHITE", Color.WHITE);
		colours.put("YELLOW", Color.YELLOW);

		// Setup graphics rendering hints for quality
		renderingHints = new HashMap<>();
		renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		try {
			Class rh = Class.forName("java.awt.RenderingHints");
			RenderingHints.Key key = (RenderingHints.Key) rh.getField("KEY_RESOLUTION_VARIANT").get(null);
			Object value = rh.getField("VALUE_RESOLUTION_VARIANT_DPI_FIT").get(null);
			renderingHints.put(key, value);
		}
		catch (Exception e){}

		Thread t = new Thread(this);
		t.start();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		if (frame != null)
			frame.addKeyListener(this);
	}

	public void run() {
		try {
			while (!exiting) {
				this.repaint();
				Thread.sleep(10);
			}
		} catch (InterruptedException iex) {}

		if (frame != null)
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Update the size of the GameArena.
	 *
	 * @param width the new width of the window in pixels.
	 * @param height the new height of the window in pixels.
	 */
	public void setSize(int width, int height)
	{
		this.arenaWidth = width;
		this.arenaHeight = height;

		super.setSize(width,height);

		if (frame != null)
			frame.setSize(arenaWidth + frame.getInsets().left + frame.getInsets().right, arenaHeight + frame.getInsets().top + frame.getInsets().bottom);


	}

	/**
	 * Set the background of the GameArena window to the specified image file.
	 * The image will be scaled to fit the size of the GameArena
	 *
	 * @param filename the filename of an image to use.
	 */
	public void setBackgroundImage(String filename)
	{
		Toolkit t=Toolkit.getDefaultToolkit();
		backgroundImage = t.getImage(filename);
	}

	/**
	 * Retrieves the JPanel on which this gameArena is drawn, so that it can be integrated into
	 * a users application.
	 *
	 * n.b. This should only be called if this GameArena was constructed without its own JFrame
	 *
	 * @return the JPanel containing this GameArena.
	 */
	public JPanel getPanel()
	{
		return this;
	}
	/**
	 * Close this GameArena window.
	 *
	 */
	public void exit()
	{
		this.exiting = true;
	}

	/**
	 * A method called by the operating system to draw onto the screen - <p><B>YOU DO NOT (AND SHOULD NOT) NEED TO CALL THIS METHOD.</b></p>
	 */
	public void paint (Graphics gr)
	{
		Graphics2D window = (Graphics2D) gr;

		if (!rendered)
		{
			this.setSize(arenaWidth, arenaHeight);

			// Create a buffer the same size of the window, which we can reuse from frame to frame to improve performance.
			buffer = new BufferedImage(arenaWidth, arenaHeight, BufferedImage.TYPE_INT_ARGB);
			graphics = buffer.createGraphics();
			graphics.setRenderingHints(renderingHints);

			// Remember that we've completed this initialisation, so that we don't do it again...
			rendered = true;
		}

		if (frame == null)
		{
			// Find the JFrame we have been added to, and attach a KeyListner
			frame = (JFrame) SwingUtilities.getWindowAncestor(this);

			if (frame != null)
				frame.addKeyListener(this);
		}

		window.setRenderingHints(renderingHints);

		synchronized (this)
		{
			if (!this.exiting)
			{
				graphics.clearRect(0,0, arenaWidth, arenaHeight);

				if (backgroundImage != null)
					graphics.drawImage(backgroundImage, 0, 0, arenaWidth, arenaHeight, 0, 0, backgroundImage.getWidth(null), backgroundImage.getHeight(null), null);

				for (Object o : things)
				{
					if (o instanceof Ball)
					{
						Ball b = (Ball) o;
						graphics.setColor(this.getColourFromString(b.getColour()));
						graphics.fillOval((int)(b.getXPosition() - b.getSize()/2), (int)(b.getYPosition() - b.getSize()/2), (int)b.getSize(), (int)b.getSize());
					}

					if (o instanceof Rectangle)
					{
						Rectangle r = (Rectangle) o;
						graphics.setColor(this.getColourFromString(r.getColour()));
						graphics.fillRect((int)r.getXPosition(), (int)r.getYPosition(), (int)r.getWidth(), (int)r.getHeight());
					}

					if (o instanceof Line)
					{
						Line l = (Line) o;
						graphics.setColor(this.getColourFromString(l.getColour()));
						graphics.setStroke(new BasicStroke((float)l.getWidth()));

						float sx = (float)l.getXStart();
						float sy = (float)l.getYStart();
						float ex = (float)l.getXEnd();
						float ey = (float)l.getYEnd();

						if (l.getArrowSize() > 0)
						{
							float arrowRatio = (float) (1.0 - ((l.getWidth() * l.getArrowSize()) / l.getLength()));
							ex = sx + ((ex - sx) * arrowRatio);
							ey = sy + ((ey - sy) * arrowRatio);
							graphics.fillPolygon(l.getArrowX(), l.getArrowY(), 3);
						}
						graphics.draw(new Line2D.Float(sx,sy,ex,ey));
					}

					if (o instanceof Text)
					{
						Text t = (Text) o;
						graphics.setFont(new Font("SansSerif", Font.BOLD, t.getSize()));
						graphics.setColor(this.getColourFromString(t.getColour()));
						graphics.drawString(t.getText(),(float)t.getXPosition(), (float)t.getYPosition());
					}
				}
			}

			window.drawImage(buffer, this.getInsets().left, this.getInsets().top, this);
		}
	}

	//
	// Shouldn't really handle colour this way, but the student's haven't been introduced
	// to constants properly yet, hmmm....
	//
	private Color getColourFromString(String col)
	{
		Color c = colours.get(col.toUpperCase());

		if (c == null && col.startsWith("#"))
		{
			int r = Integer.valueOf( col.substring( 1, 3 ), 16 );
			int g = Integer.valueOf( col.substring( 3, 5 ), 16 );
			int b = Integer.valueOf( col.substring( 5, 7 ), 16 );

			c = new Color(r,g,b);
			colours.put(col.toUpperCase(), c);
		}

		if (c == null)
			c = Color.WHITE;

		return c;
	}

	/**
	 * Adds a given Object to the drawlist, maintaining z buffering order.
	 *
	 * @param o the object to add to the drawlist.
	 */
	private void addThing(Object o, int layer)
	{
		boolean added = false;

		if (exiting)
			return;

		synchronized (this)
		{
			if (things.size() > 100000)
			{
				System.out.println("\n\n");
				System.out.println(" ********************************************************* ");
				System.out.println(" ***** Only 100000 Objects Supported per Game Arena! ***** ");
				System.out.println(" ********************************************************* ");
				System.out.println("\n");
				System.out.println("-- Joe\n\n");

				this.exit();
			}
			else
			{
				// Try to insert this object into the list.
				for (int i=0; i<things.size(); i++)
				{
					int l = 0;
					Object obj = things.get(i);

					if (obj instanceof Ball)
						l = ((Ball)obj).getLayer();

					if (obj instanceof Rectangle)
						l = ((Rectangle)obj).getLayer();

					if (obj instanceof Line)
						l = ((Line)obj).getLayer();

					if (obj instanceof Text)
						l = ((Text)obj).getLayer();

					if (layer < l)
					{
						things.add(i,o);
						added = true;
						break;
					}
				}

				// If there are no items in the list with an equivalent or higher layer, append this object to the end of the list.
				if (!added)
					things.add(o);
			}
		}
	}

	/**
	 * Remove an object from the drawlist.
	 *
	 * @param o the object to remove from the drawlist.
	 */
	private void removeObject(Object o)
	{
		synchronized (this)
		{
			things.remove(o);
		}
	}

	/**
	 * Adds a given Ball to the GameArena.
	 * Once a Ball is added, it will automatically appear on the window.
	 *
	 * @param b the ball to add to the GameArena.
	 */
	public void addBall(Ball b)
	{
		this.addThing(b, b.getLayer());
	}

	/**
	 * Adds a given Rectangle to the GameArena.
	 * Once a rectangle is added, it will automatically appear on the window.
	 *
	 * @param r the rectangle to add to the GameArena.
	 */
	public void addRectangle(Rectangle r)
	{
		this.addThing(r, r.getLayer());
	}

	/**
	 * Adds a given Line to the GameArena.
	 * Once a Line is added, it will automatically appear on the window.
	 *
	 * @param l the line to add to the GameArena.
	 */
	public void addLine(Line l)
	{
		this.addThing(l, l.getLayer());
	}

	/**
	 * Adds a given Text object to the GameArena.
	 * Once a Text object is added, it will automatically appear on the window.
	 *
	 * @param t the text object to add to the GameArena.
	 */
	public void addText(Text t)
	{
		this.addThing(t, t.getLayer());
	}


	/**
	 * Remove a Rectangle from the GameArena.
	 * Once a Rectangle is removed, it will no longer appear on the window.
	 *
	 * @param r the rectangle to remove from the GameArena.
	 */
	public void removeRectangle(Rectangle r)
	{
		this.removeObject(r);
	}

	/**
	 * Remove a Ball from the GameArena.
	 * Once a Ball is removed, it will no longer appear on the window.
	 *
	 * @param b the ball to remove from the GameArena.
	 */
	public void removeBall(Ball b)
	{
		this.removeObject(b);
	}

	/**
	 * Remove a Line from the GameArena.
	 * Once a Line is removed, it will no longer appear on the window.
	 *
	 * @param l the line to remove from the GameArena.
	 */
	public void removeLine(Line l)
	{
		this.removeObject(l);
	}

	/**
	 * Remove a Text object from the GameArena.
	 * Once a Text object is removed, it will no longer appear on the window.
	 *
	 * @param t the text object to remove from the GameArena.
	 */
	public void removeText(Text t)
	{
		this.removeObject(t);
	}

	/**
	 * Removes every object that has ever been added to the GameArena. Nothing
	 * should appear on the GameArena window after this has executed.
	 */
	public void clearGameArena() {
		synchronized(this) {
			things.clear();
		}
	}

	/**
	 * Pause for a 1/50 of a second.
	 * This method causes your program to delay for 1/50th of a second. You'll find this useful if you're trying to animate your application.
	 *
	 */
	public void pause()
	{
		try { Thread.sleep(20); }
		catch (Exception e) {};
	}

	/**
	 *	Internal method to check if ASCII code is A-Z.
	 *	@param asciiVal the ascii character value to check if it is within A-Z ascii.
	 *	@return true or false if the ascii character value is A-Z ascii.
	 */
	private boolean isAsciiAZ(int asciiVal)
	{
		if (asciiVal >= asciiAZOffset && asciiVal <= asciiAZOffset+alphabetLength)
			return true;
		return false;
	}

	/**
	 *	Check if an ASCII A-Z or a-z letter key has been pressed.
	 *	@param key the ASCII A-Z or a-z char to check if it has been pressed.
	 * 	@return whether or not that char has been pressed.
	 */
	public boolean letterPressed(char key)
	{
		if (Character.isLetter(key))
			if(isAsciiAZ((int) Character.toUpperCase(key)))
				return alphasPressed[(int) Character.toUpperCase(key) -asciiAZOffset];
		return false;
	}

	public void moving(Mallet myMallet1, Mallet myMallet2, GameArena table, Puck myPuck) //compelted input detection for keyboard control
	{
		if (letterPressed('w')) { //upwards
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel(), myMallet1.getVelocity().getY_vel() - 1));
		} else if (letterPressed('a')) { //left
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel() - 1, myMallet1.getVelocity().getY_vel()));
		} else if (letterPressed('s')) { //down
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel(), myMallet1.getVelocity().getY_vel() + 1));
		} else if (letterPressed('d')) { //right
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel() + 1, myMallet1.getVelocity().getY_vel()));
		} else if (letterPressed('w') && letterPressed('a')) {
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel() - 1, myMallet1.getVelocity().getY_vel() - 1));
		} else if (letterPressed('s') && letterPressed('a')) {
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel() - 1, myMallet1.getVelocity().getY_vel() + 1));
		} else if (letterPressed('s') && letterPressed('d')) {
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel() + 1, myMallet1.getVelocity().getY_vel() + 1));
		} else if (letterPressed('w') && letterPressed('d')) {
			myMallet1.setVelocity(new Vector(myMallet1.getVelocity().getX_vel() + 1, myMallet1.getVelocity().getY_vel() - 1));
		} else if (letterPressed('k')) {
			table.setKorM('k');
		} else if (letterPressed('m')) {
			table.setKorM('m');
		} else if (upPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel(), myMallet2.getVelocity().getY_vel() - 1));
		} else if (leftPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel() - 1, myMallet2.getVelocity().getY_vel()));
		} else if (downPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel(), myMallet2.getVelocity().getY_vel() + 1));
		} else if (rightPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel() + 1, myMallet2.getVelocity().getY_vel()));
		} else if (upPressed() && leftPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel() - 1, myMallet2.getVelocity().getY_vel() - 1));
		} else if (downPressed() && leftPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel() - 1, myMallet2.getVelocity().getY_vel() + 1));
		} else if (downPressed() && rightPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel() + 1, myMallet2.getVelocity().getY_vel() + 1));
		} else if (upPressed() && rightPressed()) {
			myMallet2.setVelocity(new Vector(myMallet2.getVelocity().getX_vel() + 1, myMallet2.getVelocity().getY_vel() - 1));
		}
		if (touching(myPuck, myMallet1)) {
			myPuck.setVelocity(myMallet1.getVelocity());
		}
		if (touching(myPuck, myMallet2)){
			myPuck.setVelocity(myMallet2.getVelocity());
		}
		myMallet1.updatePosition();
		myMallet2.updatePosition();
	}


	public void keyPressed(KeyEvent e)
	{
		keyAction(e,true);
	}

	public void keyAction(KeyEvent e,boolean yn)
	{
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_UP)
			up2 = yn;
		if (code == KeyEvent.VK_DOWN)
			down2 = yn;
		if (code == KeyEvent.VK_LEFT)
			left2 = yn;
		if (code == KeyEvent.VK_RIGHT)
			right2 = yn;

		if (code == KeyEvent.VK_W)
			up = yn;
		if (code == KeyEvent.VK_S)
			down = yn;
		if (code == KeyEvent.VK_A)
			left = yn;
		if (code == KeyEvent.VK_D)
			right = yn;

		if (code == KeyEvent.VK_SPACE)
			space = yn;
		if (code == KeyEvent.VK_SHIFT)
			shift = yn;
		if (code == KeyEvent.VK_ESCAPE)
			esc = yn;
		if (code == KeyEvent.VK_ENTER)
			enter = yn;
		//ASCII A-Z check
		if (isAsciiAZ(code))
			alphasPressed[code-asciiAZOffset] = yn;
	}

	//Player2 getter functions
	public boolean up2Pressed() {
		return up2Pressed();
	}

	public void keyReleased(KeyEvent e){
		keyAction(e,false);
	}


	public void keyTyped(KeyEvent e)
	{
	}


	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			this.leftMouse = true;

		if (e.getButton() == MouseEvent.BUTTON3)
			this.rightMouse = true;
	}

	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			this.leftMouse = false;

		if (e.getButton() == MouseEvent.BUTTON3)
			this.rightMouse = false;
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseDragged(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
	}

	/**
	 * Gets the width of the GameArena window, in pixels.
	 * @return the width in pixels
	 */
	public int getArenaWidth()
	{
		return arenaWidth;
	}

	/**
	 * Gets the height of the GameArena window, in pixels.
	 * @return the height in pixels
	 */
	public int getArenaHeight()
	{
		return arenaHeight;
	}

	/**
	 * Determines if the user is currently pressing the cursor up button.
	 * @return true if the up button is pressed, false otherwise.
	 */
	public boolean upPressed()
	{
		return up;
	}

	/**
	 * Determines if the user is currently pressing the cursor down button.
	 * @return true if the down button is pressed, false otherwise.
	 */
	public boolean downPressed()
	{
		return down;
	}

	/**
	 * Determines if the user is currently pressing the cursor left button.
	 * @return true if the left button is pressed, false otherwise.
	 */
	public boolean leftPressed()
	{
		return left;
	}

	/**
	 * Determines if the user is currently pressing the cursor right button.
	 * @return true if the right button is pressed, false otherwise.
	 */
	public boolean rightPressed()
	{
		return right;
	}

	/**
	 * Determines if the user is currently pressing the space bar.
	 * @return true if the space bar is pressed, false otherwise.
	 */
	public boolean spacePressed()
	{
		return space;
	}

	/**
	 * Determines if the user is currently pressing the Esc button.
	 * @return true if the esc button is pressed, false otherwise.
	 */
	public boolean escPressed()
	{
		return esc;
	}

	/**
	 * Determines if the user is currently pressing the enter button.
	 * @return true if the enter button is pressed, false otherwise.
	 */
	public boolean enterPressed()
	{
		return enter;
	}

	/**
	 * Determines if the user is currently pressing the shift key.
	 * @return true if the shift key is pressed, false otherwise.
	 */
	public boolean shiftPressed()
	{
		return shift;
	}

	/**
	 * Determines if the user is currently pressing the left mouse button.
	 * @return true if the left mouse button is pressed, false otherwise.
	 */
	public boolean leftMousePressed()
	{
		return leftMouse;
	}

	/**
	 * Determines if the user is currently pressing the right mouse button.
	 * @return true if the right mouse button is pressed, false otherwise.
	 */
	public boolean rightMousePressed()
	{
		return rightMouse;
	}

	/**
	 * Gathers location informaiton on the mouse pointer.
	 * @return the current X coordinate of the mouse pointer in the GameArena.
	 */
	public int getMousePositionX()
	{
		return mouseX;
	}

	/**
	 * Gathers location informaiton on the mouse pointer.
	 * @return the current Y coordinate of the mouse pointer in the GameArena.
	 */
	public int getMousePositionY()
	{
		return mouseY;
	}

	public void followMouse(Mallet mallet) {
		mallet.setXPosition(mouseX);
		mallet.setYPosition(mouseY);
	}

	public boolean touching(Puck myPuck, Mallet myMallet) //if the puck and the passed in mallet are touching
	{
		int distance = (int) Math.sqrt(Math.pow((myMallet.getXPosition() - myPuck.getXPosition()), 2) + Math.pow((myMallet.getYPosition() - myPuck.getYPosition()), 2));
		//System.out.print(distance);
		//System.out.print(" " + 0.5 * (myMallet.getSize() + myPuck.getSize()));
		//System.out.println("");
		if (distance <=  0.5 * (myPuck.getSize() + myMallet.getSize())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean colliding(Puck myPuck, Mallet myMallet)//checks whether the puck is in either of the goals
	{
		if (touching(myPuck, myMallet) && touching(myPuck, myMallet)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean collidingp(int oneOrTwo, Puck myPuck, Goal goal, GameArena table) //checks whether the puck is in either of the goals
	{
		if (oneOrTwo == 2) {
			if (myPuck.getXPosition() > goal.getXPosition()+goal.getWidth() && myPuck.getYPosition() > goal.getYPosition() && myPuck.getYPosition() < goal.getYPosition() + goal.getHeight() ) {
				table.setPlayer2Score(table.getPlayer2Score() + 1);
				return true;
			}
		} else {
			if (myPuck.getXPosition() < goal.getXPosition()+goal.getWidth() && myPuck.getYPosition() > goal.getYPosition() && myPuck.getYPosition() < goal.getYPosition() + goal.getHeight() ) {
				table.setPlayer1Score(table.getPlayer1Score() + 1);
				return true;
			}
		}
		return false;
	}

	public int getPlayer1Score() //returns player 1's score
	{
		return player1Score;
	}

	public void setPlayer1Score(int score) //sets player 1's score
	{
		player1Score = score;
	}

	public int getPlayer2Score() //returns player 2's score
	{
		return player2Score;
	}

	public void setPlayer2Score(int score) //sets the player2's score
	{
		player2Score = score;
	}

	public boolean inGoal(Goal goal, Ball ball, GameArena table) //returns true or false based on the puck is in either goal
	{
		if (((ball.getXPosition() > 790) && (ball.getYPosition() > 200) && (400 > ball.getYPosition())) || ((ball.getXPosition() < 200) && (ball.getYPosition() < 10) && (ball.getYPosition() > 0))) {
			if (goal.getTeam() == 1) {
				table.setPlayer2Score(table.getPlayer2Score() + 1);
				return true;
			} else {
				table.setPlayer1Score(table.getPlayer1Score() + 1);
				return true;
			}
		}
		return false;
	}

	public Vector getDisplacment(Vector[] vectorArray) //calculates the displacment Vector for the measure velocity method in the Puck class
	{
		Vector displacment = new Vector(0, 0);
		displacment.setX_vel(vectorArray[1].getX_vel() - vectorArray[0].getX_vel());
		displacment.setY_vel(vectorArray[1].getY_vel() - vectorArray[0].getY_vel());
		return displacment;
	}

	public void start(Mallet player1, Mallet player2, Puck myPuck) //sets all the assets to the starting position on the screen
	{
		player1.setXPosition(700);
		player1.setYPosition(400);
		player2.setXPosition(300);
		player2.setYPosition(400);
		myPuck.setXPosition(400);
		myPuck.setYPosition(400);
		myPuck.setVelocity(new Vector(0, 0));
	}
}