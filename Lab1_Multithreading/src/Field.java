import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Field extends JPanel {
    private boolean paused;
    private ArrayList<BouncingBall> balls = new ArrayList<>(10);
    private Obstacle obstacle;  // Препятствие
    private Timer repaintTimer = new Timer(10, new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            repaint();
        }
    });

    public Field() {
        setBackground(Color.WHITE);
        repaintTimer.start();
        obstacle = new Obstacle(300, 200, 100, 50); // Начальная позиция и размеры препятствия

        // Слушаем движение мыши для перемещения препятствия
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                obstacle.move(e.getX(), e.getY());
            }
        });
    }

    public void addBall() {
        balls.add(new BouncingBall(this));
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public synchronized void canMove(BouncingBall ball) throws InterruptedException {
        if (paused) {
            wait();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D canvas = (Graphics2D) g;

        // Рисуем все мячи
        for (BouncingBall ball : balls) {
            ball.paint(canvas);
        }

        // Рисуем препятствие
        obstacle.paint(g);
    }

    // Метод для проверки столкновения с препятствием
    public boolean checkObstacleCollision(BouncingBall ball) {
        Rectangle ballBounds = new Rectangle((int)ball.getX() - ball.getRadius(), (int)ball.getY() - ball.getRadius(), ball.getRadius()*2, ball.getRadius()*2);
        return ballBounds.intersects(obstacle.getBounds());
    }
}