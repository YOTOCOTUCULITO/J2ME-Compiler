import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import java.util.Vector;

public class ZombieGameMidlet extends MIDlet {
    private Display display;
    private GameCanvas gameCanvas;

    public ZombieGameMidlet() {
        gameCanvas = new GameCanvas();
        display = Display.getDisplay(this);
    }

    public void startApp() {
        display.setCurrent(gameCanvas);
        gameCanvas.start();
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {
        gameCanvas.stop();
    }
}

class GameCanvas extends Canvas implements Runnable {
    private boolean running;
    private int width, height;
    private Player player;
    private Vector enemies;
    private Vector projectiles;
    private int lives = 3;
    private long lastUpdate;
    private int enemySpawnInterval = 2000; // En milisegundos
    private int difficulty = 2;
    private int enemySpeed = 2;

    public GameCanvas() {
        width = getWidth();
        height = getHeight();
        player = new Player(width / 2, height - 50);
        enemies = new Vector();
        projectiles = new Vector();
        lastUpdate = System.currentTimeMillis();
    }

    public void start() {
        running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public void run() {
        long lastEnemySpawn = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            if (now - lastUpdate >= 16) { // ~60 FPS
                update();
                repaint();
                lastUpdate = now;
            }
            if (now - lastEnemySpawn >= enemySpawnInterval) {
                spawnEnemy();
                lastEnemySpawn = now;
            }
            try {
                Thread.sleep(10); // Para limitar la velocidad de la ejecución
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void paint(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, width, height);
        player.draw(g);
        drawProjectiles(g);
        drawEnemies(g);
        drawLives(g);
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        switch (gameAction) {
            case LEFT:
                player.moveLeft();
                break;
            case RIGHT:
                player.moveRight();
                break;
            case UP:
                player.moveUp();
                break;
            case DOWN:
                player.moveDown();
                break;
            case FIRE:
                player.shoot(projectiles);
                break;
        }
    }

    private void update() {
        player.update();
        updateProjectiles();
        updateEnemies();
        checkCollisions();
    }

    private void drawLives(Graphics g) {
        g.setColor(255, 0, 0);
        for (int i = 0; i < lives; i++) {
            g.fillRect(10 + (i * 15), 10, 10, 10);
        }
    }

    private void drawProjectiles(Graphics g) {
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = (Projectile) projectiles.elementAt(i);
            p.draw(g);
        }
    }

    private void drawEnemies(Graphics g) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = (Enemy) enemies.elementAt(i);
            e.draw(g);
        }
    }

    private void updateProjectiles() {
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = (Projectile) projectiles.elementAt(i);
            p.update();
            if (p.isOffScreen(width, height)) {
                projectiles.removeElementAt(i);
                i--;
            }
        }
    }

    private void updateEnemies() {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = (Enemy) enemies.elementAt(i);
            e.update();
            if (e.isOffScreen(height)) {
                enemies.removeElementAt(i);
                i--;
            }
        }
    }

    private void spawnEnemy() {
        int x = (int) (Math.random() * (width - 30));
        enemies.addElement(new Enemy(x, 0, 30, 30, enemySpeed));
    }

    private void checkCollisions() {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = (Enemy) enemies.elementAt(i);
            if (player.collidesWith(e)) {
                enemies.removeElementAt(i);
                i--;
                lives--;
                if (lives <= 0) {
                    gameOver();
                    return;
                }
            }

            for (int j = 0; j < projectiles.size(); j++) {
                Projectile p = (Projectile) projectiles.elementAt(j);
                if (p.collidesWith(e)) {
                    enemies.removeElementAt(i);
                    projectiles.removeElementAt(j);
                    i--;
                    break;
                }
            }
        }
    }

    private void gameOver() {
        running = false;
        // Muestra un mensaje o haz cualquier otra cosa para indicar el fin del juego
    }
}

class Player {
    private int x, y;
    private int width = 20;
    private int height = 20;
    private int speed = 5;

    public Player(int startX, int startY) {
        x = startX;
        y = startY;
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }

    public void moveRight() {
        x += speed;
        if (x > 220) x = 220; // 240 - width
    }

    public void moveUp() {
        y -= speed;
        if (y < 0) y = 0;
    }

    public void moveDown() {
        y += speed;
        if (y > 300) y = 300; // 320 - height
    }

    public void shoot(Vector projectiles) {
        projectiles.addElement(new Projectile(x + width / 2 - 2, y));
    }

    public void update() {
        // Lógica de actualización del jugador
    }

    public void draw(Graphics g) {
        g.setColor(255, 255, 255);
        g.fillRect(x, y, width, height);
    }

    public boolean collidesWith(Enemy e) {
        return x < e.getX() + e.getWidth() && x + width > e.getX() &&
               y < e.getY() + e.getHeight() && y + height > e.getY();
    }
}

class Projectile {
    private int x, y;
    private int width = 4;
    private int height = 10;
    private int speed = 7;

    public Projectile(int startX, int startY) {
        x = startX;
        y = startY;
    }

    public void update() {
        y -= speed;
    }

    public void draw(Graphics g) {
        g.setColor(255, 0, 0);
        g.fillRect(x, y, width, height);
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return y < 0;
    }

    public boolean collidesWith(Enemy e) {
        return x < e.getX() + e.getWidth() && x + width > e.getX() &&
               y < e.getY() + e.getHeight() && y + height > e.getY();
    }
}

class Enemy {
    private int x, y;
    private int width, height;
    private int speed;

    public Enemy(int startX, int startY, int width, int height, int speed) {
        x = startX;
        y = startY;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void update() {
        y += speed;
    }

    public void draw(Graphics g) {
        g.setColor(0, 255, 0);
        g.fillRect(x, y, width, height);
    }

    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
