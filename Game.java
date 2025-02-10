import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class app {
    public static void main(String[] args) {
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Flappy_Bird flappyBird = new Flappy_Bird();
        frame.add(flappyBird);
        frame.pack();
        frame.setVisible(true);
        flappyBird.requestFocus();
    }
}

class Flappy_Bird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    Image backgroundImg;
    Image birdImg;
    Image toppipeImg;
    Image bottompipeImg;

    int birdX = (boardWidth - 44) / 8;
    int birdY = (boardHeight - 44) / 2;
    int birdHeight = 44;
    int birdWidth = 44;

    class Bird {
        int x = birdX;
        int y = birdY;
        int height = birdHeight;
        int width = birdWidth;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    int pipeWidth = 64;
    int pipeHeight = 512;
    int pipeGap = boardHeight / 4;

    class Pipe {
        int x;
        int y;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img, int x, int y) {
            this.img = img;
            this.x = x;
            this.y = y;
        }
    }

    Bird bird;
    int velocityY = 0;
    int gravity = 1;
    int velocityX = -4;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    public Flappy_Bird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = loadImage("BG.jpeg");
        birdImg = loadImage("/Bird.png");
        toppipeImg = loadImage("/OB_UP.jpeg");
        bottompipeImg = loadImage("/OB_DOWN.jpeg");

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    private void placePipes() {
        int randomPipeY = (int) (-pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        Pipe topPipe = new Pipe(toppipeImg, boardWidth, randomPipeY);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottompipeImg, boardWidth, randomPipeY + pipeHeight + pipeGap);
        pipes.add(bottomPipe);
    }

    private Image loadImage(String path) {
        URL resourceURL = getClass().getResource(path);
        if (resourceURL == null) {
            System.out.println("Could not load resource: " + path);
            return null;
        }
        return new ImageIcon(resourceURL).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
            g.drawString("Press Space to Restart", 10, 200);
        } else {
            g.drawString("Score: " + (int) score, 10, 35);
        }
    }

    private void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        if (bird.y >= boardHeight - bird.height) {
            gameOver = true;
        }

        for (Iterator<Pipe> it = pipes.iterator(); it.hasNext(); ) {
            Pipe pipe = it.next();
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }

            // Remove pipes once they go off-screen
            if (pipe.x + pipe.width < 0) {
                it.remove();
            }
        }
    }

    private boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        } else {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                resetGame();
            } else {
                velocityY = -9;
            }
        }
    }

    private void resetGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        placePipesTimer.start();
        gameLoop.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}