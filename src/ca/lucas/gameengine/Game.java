package ca.lucas.gameengine;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import ca.lucas.gameengine.entities.MerchantShip;
import ca.lucas.gameengine.entities.Player;
import ca.lucas.gameengine.entities.Ship;
import ca.lucas.gameengine.gfx.Screen;
import ca.lucas.gameengine.gfx.SpriteSheet;
import ca.lucas.gameengine.level.Level;
 
public class Game extends Canvas implements Runnable {
 
    private static final long serialVersionUID = 1L;
 
    public static final int WIDTH = 160;
    public static final int HEIGHT = WIDTH/ 12*9;
    public static final int SCALE = 5; // Window size.
    public static final String NAME = "Game";
 
    private JFrame frame;
 
    public boolean running = false; // Run the game.
    public int tickCount = 0;
 
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
                    BufferedImage.TYPE_INT_RGB); // Image that loads the sprite sheet
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
                    .getData();
    private int[] colours = new int[6 * 6 * 6]; // Six different shades for each color (R G B)
 
    private Screen screen;
    public InputHandler input;
    public Level level;
    public Player player;
    public MerchantShip merchantShip;
 
    public Game() {
    	// Set the canvas size
        setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
 
        frame = new JFrame(NAME);
 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close completely the frame
        frame.setLayout(new BorderLayout()); // Creating the canvas
        frame.add(this, BorderLayout.CENTER); // The canvas has to be in the center of the frame
        frame.pack(); // Set the frame to the canvas size.
 
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
 
    public void init() {
        int index = 0;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                	
                	// 255 for visible colors and 5 because are 6 shades.
                    int rr = (r * 255 / 5);
                    int gg = (g * 255 / 5);
                    int bb = (b * 255 / 5);
 
                    // '<<' is used to access individual bit information on a single integer.
                    // in colours[index++] has 4 bytes (aa, rr, gg and bb) and each byte has 8 bits
                    // so, for change the right byte you have to use '<<'
                    colours[index++] = rr << 16 | gg << 8 | bb;
                }
            }
        }
 
        screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/SpriteSheet.png"));
        input = new InputHandler(this);
        level = new Level("/levels/GameLevel.png");
        player = new Player(level, 50, 50, input);
        merchantShip = new MerchantShip(level, 100, 100,screen);
        level.addEntity(player);
        level.addEntity(merchantShip);
    }
 
    // Synchronized is used to create an applet
    public synchronized void start() {
        running = true;
        new Thread(this).start();
    }
 
    public synchronized void stop() {
        running = false;
    }
 
    // Game loop.
    public void run() {
        long lastTime = System.nanoTime();
        long lastTimer = System.currentTimeMillis(); // The current time.
        double nsPerTick = 1000000000D / 60D; // How many nanoseconds are in one tick
        double delta = 0; // How many nanoseconds has gone by. Once hit one milliseconds, the game has to render
        int ticks = 0;
        int frames = 0;
 
        init();
 
        while (running) {
            long now = System.nanoTime(); 
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            boolean shouldRender = true;
 
            while (delta >= 1) {
                ticks++;
                tick();
                delta -= 1;
                shouldRender = false;
            }
//                try {
//                        Thread.sleep(2);
//                } catch (InterruptedException e) {
//                        e.printStackTrace();
//                }
            if (shouldRender) {
                frames++;
                render();
            }
            
            // 1 second = 1000 milliseconds 
            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
                System.out.println(ticks + " ticks , " + frames + " frames per second");
                frames = 0;
                ticks = 0;
            }
        }
    }
 
    // Tick update the logic of the game
    public void tick() {
    	tickCount++;
    	level.tick();
    }
 
    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
                createBufferStrategy(3);
                return;
        }
 
        int xOffset = player.x - (screen.width / 2);
        int yOffset = player.y - (screen.height / 2);
 
        level.renderTiles(screen, xOffset, yOffset);
 
        // Debugging for fonts 
        // Verifying if the screen has the map width 
//            for (int x = 0; x < level.width; x++) {
//                    int colour = Color.get(-1, -1, -1, 000);
//                    if (x % 10 == 0 && x != 0) {
//                            colour = Color.get(-1, -1, -1, 500);
//                    }
        			  // Write the font on desired position
//                    Font.render((x % 10) + "", screen, 0 + (x * 8), 0, colour, 0x00, 1);
//            }
            
        level.renderEntities(screen);
 
        for (int y = 0; y < screen.height; y++) {
        	for (int x = 0; x < screen.width; x++) {
        		int ColourCode = screen.pixels[x + y * screen.width];
        		if (ColourCode < 255) {
        			pixels[x + y * WIDTH] = colours[ColourCode];
        		}
        	}
		}
 
        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show();
    }
 
    public static void main(String[] args) {
            new Game().start();
    }
}