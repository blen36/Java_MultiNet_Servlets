import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Field extends JPanel {
    private boolean paused;
    private ArrayList<BouncingBall> balls = new ArrayList<>(10);
    private Obstacle obstacle;
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

        for (BouncingBall ball : balls) {
            ball.paint(canvas);
        }

        obstacle.paint(g);
    }

    // Метод для проверки столкновения с препятствием
    public void handleObstacleCollision(BouncingBall ball) {

        Rectangle obstacleBounds = obstacle.getBounds();

        double ballLeft = ball.getX() - ball.getRadius();
        double ballRight = ball.getX() + ball.getRadius();
        double ballTop = ball.getY() - ball.getRadius();
        double ballBottom = ball.getY() + ball.getRadius();

        // Проверка столкновения
        if (obstacleBounds.intersects(
                ballLeft, ballTop,
                ball.getRadius() * 2,
                ball.getRadius() * 2)) {

            // Определяем минимальное пересечение по осям
            double overlapLeft = ballRight - obstacleBounds.x;
            double overlapRight = obstacleBounds.x + obstacleBounds.width - ballLeft;
            double overlapTop = ballBottom - obstacleBounds.y;
            double overlapBottom = obstacleBounds.y + obstacleBounds.height - ballTop;

            double minOverlapX = Math.min(overlapLeft, overlapRight);
            double minOverlapY = Math.min(overlapTop, overlapBottom);

            if (minOverlapX < minOverlapY) {
                ball.invertSpeedX();
            } else {
                ball.invertSpeedY();
            }
        }
    }
}