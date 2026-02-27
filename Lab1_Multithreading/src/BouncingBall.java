import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class BouncingBall implements Runnable {
    private static final int MAX_RADIUS = 40;
    private static final int MIN_RADIUS = 3;
    private static final int MAX_SPEED = 15;

    private Field field;
    private int radius;
    private Color color;
    private double x;
    private double y;
    private int speed;
    private double speedX;
    private double speedY;

    public BouncingBall(Field field) {
        this.field = field;

        radius = new Double(Math.random() * (MAX_RADIUS - MIN_RADIUS)).intValue() + MIN_RADIUS;

        speed = new Double(Math.round(5 * MAX_SPEED / radius)).intValue();
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }

        double angle = Math.random() * 2 * Math.PI;
        speedX = 3 * Math.cos(angle);
        speedY = 3 * Math.sin(angle);

        // Цвет мяча выбирается случайно
        color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());

        // Начальное положение мяча случайно
        x = Math.random() * (field.getSize().getWidth() - 2 * radius) + radius;
        y = Math.random() * (field.getSize().getHeight() - 2 * radius) + radius;

        // Создаем новый поток, передавая ссылку на класс, реализующий Runnable (т.е. на себя)
        Thread thisThread = new Thread(this);
        // Запускаем поток
        thisThread.start();
    }

    // Метод run() исполняется внутри потока. Когда он завершает работу, то завершится и поток
    public void run() {
        try {
            while (true) {
                field.canMove(this);

                field.handleObstacleCollision(this);

                // Логика для столкновений с границами экрана
                if (x + speedX <= radius) {
                    speedX = -speedX;
                    x = radius;
                } else if (x + speedX >= field.getWidth() - radius) {
                    speedX = -speedX;
                    x = field.getWidth() - radius;
                } else if (y + speedY <= radius) {
                    speedY = -speedY;
                    y = radius;
                } else if (y + speedY >= field.getHeight() - radius) {
                    speedY = -speedY;
                    y = field.getHeight() - radius;
                } else {
                    x += speedX;
                    y += speedY;
                }

                Thread.sleep(16 - speed);
            }
        } catch (InterruptedException ex) {
            // Если нас прервали, то ничего не делаем и просто выходим (завершаемся)
        }
    }

    public void paint(Graphics2D canvas) {
        canvas.setColor(color);
        Ellipse2D.Double ball = new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius);
        canvas.draw(ball);
        canvas.fill(ball);
    }

    public void invertSpeedX() {
        speedX = -speedX;
    }

    public void invertSpeedY() {
        speedY = -speedY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }
}