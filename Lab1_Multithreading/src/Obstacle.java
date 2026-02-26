import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Obstacle {
    private int x, y, width, height;
    private Color color;

    // Конструктор
    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
    }

    // Рисуем препятствие
    public void paint(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    // Метод для обновления позиции препятствия
    public void move(int mouseX, int mouseY) {
        this.x = mouseX - width / 2;
        this.y = mouseY - height / 2;
    }

    // Получаем прямоугольник для столкновений
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
