package probe;

public final class PoolShooter {
    @PoolCapacity(5) static final class Bullet {
        int x, y;
        Bullet next;
        Bullet(int x) { this.x = x; y = 116; }
    }
    @PoolCapacity(2) static final class Enemy {
        int x, y;
        Enemy next;
        Enemy(int x) { this.x = x; y = 8; }
    }
    static Bullet bullets;
    static Enemy enemies;
    static int playerX = 76, frame, shots, hits, retired, skipped;
    static int bulletCount, enemyCount, spawnClock = 3, fireClock, wave;
    @NoInline static Bullet fire(int x) { return new Bullet(x); }
    @NoInline static Enemy spawn(int x) { return new Enemy(x); }

    static int abs(int x) { return x < 0 ? -x : x; }
    static void update(int buttons) {
        if ((buttons & GameBoy.LEFT) != 0 && playerX > 0) playerX -= 2;
        if ((buttons & GameBoy.RIGHT) != 0 && playerX < 152) playerX += 2;
        if (spawnClock == 0) {
            spawnClock = 90;
            if (enemyCount < 1) {
                Enemy fresh = spawn((wave++ & 1) == 0 ? 76 : 116);
                fresh.next = enemies; enemies = fresh; enemyCount++;
            }
        }
        spawnClock--;
        Enemy previousEnemy = null, enemy = enemies;
        while (enemy != null) {
            enemy.y++;
            if (enemy.y > 140) {
                if (previousEnemy == null) enemies = enemy.next;
                else previousEnemy.next = enemy.next;
                enemyCount--; retired++;
            } else previousEnemy = enemy;
            enemy = enemy.next;
        }
        Bullet previous = null, bullet = bullets;
        while (bullet != null) {
            bullet.y -= 3;
            boolean remove = bullet.y < 0;
            previousEnemy = null; enemy = enemies;
            while (!remove && enemy != null) {
                if (abs(bullet.x - enemy.x) < 7 && abs(bullet.y - enemy.y) < 7) {
                    if (previousEnemy == null) enemies = enemy.next;
                    else previousEnemy.next = enemy.next;
                    enemyCount--; hits++; remove = true;
                }
                previousEnemy = enemy; enemy = enemy.next;
            }
            if (remove) {
                if (previous == null) bullets = bullet.next;
                else previous.next = bullet.next;
                bulletCount--; retired++;
            } else previous = bullet;
            bullet = bullet.next;
        }
        if (fireClock == 0) {
            fireClock = 6;
            if ((buttons & GameBoy.A) != 0) {
                if (bulletCount < 4) {
                    Bullet fresh = fire(playerX);
                    fresh.next = bullets; bullets = fresh;
                    bulletCount++; shots++;
                } else skipped++;  
            }
        }
        fireClock--;
        frame++;
    }
    static void draw() {
        GameBoy.sprite(0, playerX, 124);
        int index = 1;
        Bullet bullet = bullets;
        while (bullet != null) {
            GameBoy.sprite(index++, bullet.x, bullet.y);
            bullet = bullet.next;
        }
        Enemy enemy = enemies;
        while (enemy != null) {
            GameBoy.sprite(index++, enemy.x, enemy.y);
            enemy = enemy.next;
        }
        GameBoy.hideSprites(index, 7);
    }
    public static void main(String[] args) {
        GameBoy.init();
        for (int row = 0; row < 8; row++) {
            int bits = row < 4 ? (0x18 << row) | (0x18 >> row) : 0x7e;
            GameBoy.tileRow(row, bits, bits);
        }
        GameBoy.show();
        while (true) {
            GameBoy.waitFrame();
            update(GameBoy.buttons());
            draw();
        }
    }
}
