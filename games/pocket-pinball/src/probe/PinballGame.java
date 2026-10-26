package probe;

public final class PinballGame {
    static final int READY = 0, PLAY = 1, OVER = 3;
    static final int BALL_TILE = 16, FLIPPER_TILE = 17, LAMP_TILE = 19;
    static final int VIEW_HEIGHT = 128, WORLD_HEIGHT = 256, MAX_SCROLL = WORLD_HEIGHT - VIEW_HEIGHT;
    static final int FLIPPER_Y = 236, DRAIN_Y = 259, PLUNGER_Y = 236;
    static final int TITLE_SCREEN = 0, GAME_SCREEN = 1;
    static int frames, screen;

    static final class Camera {
        int y = MAX_SCROLL;
        void follow(int ballY, int state) {
            if (state == READY || state == OVER) { y = MAX_SCROLL; return; }
            int target = y;
            if (ballY - y < 40) target = ballY - 40;
            else if (ballY - y > 88) target = ballY - 88;
            if (target < 0) target = 0;
            if (target > MAX_SCROLL) target = MAX_SCROLL;
            int delta = target - y;
            if (delta > 6) delta = 6;
            if (delta < -6) delta = -6;
            y += delta;
        }
    }

    static final class Ball {
        int x, y, vx, vy, spin, gravityPhase;
        boolean lane;
        void ready() { x = 148 << 4; y = PLUNGER_Y << 4; vx = 0; vy = 0; spin = gravityPhase = 0; lane = true; }
    }
    static final class Bumper {
        final int x, y, bit;
        int cooldown, shown;
        Bumper(int x, int y, int bit) { this.x = x; this.y = y; this.bit = bit; }
        void hit(Table table) {
            Ball ball = table.ball;
            int dx = (ball.x >> 4) - x, dy = (ball.y >> 4) - y;
            int ax = abs(dx), ay = abs(dy);
            if (ax > 11 || ay > 11 || ax + ay > 16) return;
            table.contact = 1;
            if (ax >= 5 && ay >= 5) {
                int separation = ((19 - ax - ay) >> 1) << 4;
                ball.x += dx < 0 ? -separation : separation;
                ball.y += dy < 0 ? -separation : separation;
                int outward = (dx < 0 ? -ball.vx : ball.vx) + (dy < 0 ? -ball.vy : ball.vy);
                if (outward < 0) {
                    int oldX = ball.vx;
                    if ((dx < 0) == (dy < 0)) { ball.vx = -ball.vy; ball.vy = -oldX; }
                    else { ball.vx = ball.vy; ball.vy = oldX; }
                }
                if (abs(ball.vx) + abs(ball.vy) < 32) {
                    ball.vx += dx < 0 ? -8 : 8; ball.vy += dy < 0 ? -8 : 8;
                }
            } else if (ax > ay) {
                ball.x = (x + (dx < 0 ? -12 : 12)) << 4;
                int speed = max(24, abs(ball.vx));
                if (speed > 44) speed = 44;
                ball.vx = dx < 0 ? -speed : speed;
            } else {
                ball.y = (y + (dy < 0 ? -12 : 12)) << 4;
                int speed = max(24, abs(ball.vy));
                if (speed > 44) speed = 44;
                ball.vy = dy < 0 ? -speed : speed;
            }
            if (cooldown == 0) {
                table.score += 100; table.lights |= bit; table.bumperHits++; cooldown = 10;
            }
        }
    }

    static final class Target {
        final int x, y, bit;
        int down, painted = -1;
        Target(int x, int y, int bit) { this.x = x; this.y = y; this.bit = bit; }
        void hit(Table table, int previousY) {
            if (down != 0) return;
            Ball ball = table.ball;
            int dx = (ball.x >> 4) - x, dy = (ball.y >> 4) - y;
            if (abs(dx) > 11 || abs(dy) > 7) return;
            table.contact = 1;
            if (previousY <= y - 8 || previousY >= y + 8 || 8 - abs(dy) <= 12 - abs(dx)) {
                int speed = max(8, abs(ball.vy) - (abs(ball.vy) >> 2));
                if (dy < 0) { ball.y = (y - 9) << 4; ball.vy = -speed; }
                else { ball.y = (y + 9) << 4; ball.vy = speed; }
            } else {
                int speed = max(8, abs(ball.vx) - (abs(ball.vx) >> 2));
                if (dx < 0) { ball.x = (x - 13) << 4; ball.vx = -speed; }
                else { ball.x = (x + 13) << 4; ball.vx = speed; }
            }
            down = 1; table.targetHits++; table.score += 150; table.targetMask |= bit;
            if (table.targetMask == 3) {
                table.score += 500; table.targetBanks++; table.targetResetClock = 90;
            }
        }
        void draw() {
            if (painted == down) return;
            int tile = down == 0 ? 21 : 23;
            GameBoy.backgroundPair((x - 8) >> 3, (y - 4) >> 3, tile);
            painted = down;
        }
    }
    static final class Kicker {
        final int x, y, direction;
        int cooldown, shown;
        Kicker(int x, int y, int direction) { this.x = x; this.y = y; this.direction = direction; }
        void hit(Table table) {
            Ball ball = table.ball;
            int dx = abs((ball.x >> 4) - x), dy = abs((ball.y >> 4) - y);
            if (dx > 12 || dy > 12 || dx + dy > 18) return;
            table.contact = 1;
            ball.x = (x + (direction > 0 ? 14 : -14)) << 4;
            if (cooldown != 0) {
                ball.vx = direction > 0 ? max(8, abs(ball.vx)) : -max(8, abs(ball.vx));
                if (ball.vy > 0) ball.vy = -(ball.vy - (ball.vy >> 2));
                return;
            }
            ball.vx = direction > 0 ? 28 : -28;
            ball.y = (y - 4) << 4; ball.vy = -48;
            cooldown = 12; table.kickerHits++; table.score += 75;
        }
        void draw(int index, int cameraY) {
            if (cooldown > 0 && shown != cameraY + 1) {
                GameBoy.sprite(index, x - 4, y - cameraY - 4); shown = cameraY + 1;
            } else if (cooldown == 0 && shown != 0) {
                GameBoy.hideSprites(index, index + 1); shown = 0;
            }
        }
    }
    static final class Table {
        final Ball ball = new Ball();
        final Camera camera = new Camera();
        final Bumper left = new Bumper(40, 56, 1);
        final Bumper middle = new Bumper(80, 88, 2);
        final Bumper right = new Bumper(120, 56, 4);
        final Target upperTarget = new Target(56, 132, 1);
        final Target lowerTarget = new Target(104, 164, 2);
        final Kicker leftKicker = new Kicker(32, 192, 1);
        final Kicker rightKicker = new Kicker(120, 192, -1);
        int score, lives, bonusHits, lights, state, lastButtons, topCooldown;
        int bumperHits, flipperHits, launches, drains, nudges, nudgeClock;
        int leftSwing, rightSwing, contact;
        int targetHits, targetBanks, targetMask, targetResetClock, kickerHits;
        int gate, gateClock, paintedGate = -1;
        int plunger, paintedPlunger = -1, paintedTurn = -1;
        int leftUp, rightUp, paintedLeft, paintedRight, paintedCamera;
        int hudScoreSnapshot, hudScorePainted, hudRemainder;
        int hudStatus, hudLampMask, hudLives, hudBonuses;
        Table() { reset(); }
        void reset() {
            score = 0; lives = 3; bonusHits = 0; lights = 0; state = READY;
            lastButtons = 0; topCooldown = 0; bumperHits = 0; flipperHits = 0;
            launches = 0; drains = 0; nudges = 0; nudgeClock = 0; leftSwing = rightSwing = 0;
            left.cooldown = middle.cooldown = right.cooldown = 0;
            targetHits = targetBanks = kickerHits = 0; resetPieces();
            leftUp = rightUp = 0; paintedLeft = paintedRight = paintedCamera = -1; camera.y = MAX_SCROLL;
            hudScoreSnapshot = 0; hudScorePainted = -1; hudRemainder = 0;
            hudStatus = hudLampMask = hudLives = hudBonuses = -1; gate = gateClock = plunger = 0; ball.ready();
        }
        void resetPieces() {
            targetMask = targetResetClock = 0;
            upperTarget.down = lowerTarget.down = 0;
            leftKicker.cooldown = rightKicker.cooldown = 0;
        }
        void tickPieces() {
            if (topCooldown > 0) topCooldown--;
            if (left.cooldown > 0) left.cooldown--;
            if (middle.cooldown > 0) middle.cooldown--;
            if (right.cooldown > 0) right.cooldown--;
            if (leftKicker.cooldown > 0) leftKicker.cooldown--;
            if (rightKicker.cooldown > 0) rightKicker.cooldown--;
            if (targetResetClock > 0 && --targetResetClock == 0) {
                upperTarget.down = lowerTarget.down = 0; targetMask = 0;
            }
        }
        void step(int buttons) {
            contact = 0;
            int pressed = buttons & ~lastButtons;
            if ((pressed & GameBoy.START) != 0) { reset(); lastButtons = buttons; return; }
            leftUp = buttons & (GameBoy.LEFT | GameBoy.B);
            rightUp = buttons & (GameBoy.RIGHT | GameBoy.A);
            if (leftSwing > 0) leftSwing--;
            if (rightSwing > 0) rightSwing--;
            if ((pressed & (GameBoy.LEFT | GameBoy.B)) != 0) leftSwing = 6;
            if ((pressed & (GameBoy.RIGHT | GameBoy.A)) != 0) rightSwing = 6;
            if (leftUp == 0) leftSwing = 0;
            if (rightUp == 0) rightSwing = 0;
            if (nudgeClock > 0) nudgeClock--;
            if (state == READY) {
                if (plunger == 0) {
                    if ((pressed & (GameBoy.DOWN | GameBoy.A)) != 0) plunger = 8;
                } else {
                    plunger--;
                    int compression = (8 - plunger) >> 1;
                    if (compression > 3) compression = 3;
                    ball.y = (PLUNGER_Y + (compression << 1)) << 4;
                    if (plunger == 0) {
                        state = PLAY; ball.y = PLUNGER_Y << 4;
                        ball.vy = -92; ball.vx = 0; launches++;
                    }
                }
            } else if (state == PLAY) {
                tickPieces();
                if (!ball.lane && (pressed & GameBoy.UP) != 0 && nudgeClock == 0) {
                    ball.vy -= 16; ball.vx += (frames & 1) == 0 ? 10 : -10;
                    nudgeClock = 45; nudges++;
                }
                physics();
                ball.spin += abs(ball.vx) + abs(ball.vy);
            }
            camera.follow(ball.y >> 4, state);
            lastButtons = buttons;
        }
        void launch() {
            if ((ball.y >> 4) < 72) {
                if (++gateClock == 3) { gateClock = 0; if (gate < 3) gate++; }
            }
            if (ball.vx == 0) {
                if ((ball.y >> 4) > 40) { ball.vy++; ball.y += ball.vy; }
                if ((ball.y >> 4) <= 40) {
                    ball.vy = 0;
                    if (gate == 3) ball.vx = -24;
                }
            } else if (gate == 3) {
                ball.x += ball.vx;
                if (ball.x <= (132 << 4)) { ball.lane = false; ball.vy = 8; }
            }
        }
        void physics() {
            if (ball.lane) { launch(); return; }
            if (gate > 0 && ++gateClock == 3) { gateClock = 0; gate--; }
            int previousY = ball.y >> 4;
            ball.gravityPhase ^= 1;
            if (ball.gravityPhase == 0 && ball.vy < 40) ball.vy++;
            if (ball.vy > 40) ball.vy = 40;
            ball.x += ball.vx; ball.y += ball.vy;
            int x = ball.x >> 4, y = ball.y >> 4;
            if (x < 18) { contact = 1; ball.x = 18 << 4; ball.vx = abs(ball.vx) - (abs(ball.vx) >> 3); }
            if (x > 132) { contact = 1; ball.x = 132 << 4; ball.vx = -(abs(ball.vx) - (abs(ball.vx) >> 3)); }
            if (y < 23) { contact = 1; ball.y = 23 << 4; ball.vy = abs(ball.vy) - (abs(ball.vy) >> 2); }
            if (y < 44 && abs(x - 80) <= 11) {
                contact = 1; ball.y = 44 << 4; ball.vy = max(24, abs(ball.vy));
                if (topCooldown == 0) {
                    if (lights == 7) { score += 1000; bonusHits++; lights = 0; }
                    else score += 25;
                    topCooldown = 10;
                }
                x = ball.x >> 4; y = 44;
            }
            if (y >= 45 && y <= 67) {
                if (x <= 52) left.hit(this);
                else if (x >= 108) right.hit(this);
            } else if (y >= 77 && y <= 99 && x >= 68 && x <= 92) middle.hit(this);
            x = ball.x >> 4; y = ball.y >> 4;
            if (y >= 124 && y <= 172) {
                if (y < 148) upperTarget.hit(this, previousY);
                else lowerTarget.hit(this, previousY);
            } else if (y >= 180 && y <= 204) {
                if (x < 56) leftKicker.hit(this);
                else if (x > 96) rightKicker.hit(this);
            }
            x = ball.x >> 4; y = ball.y >> 4;
            if (y > 208 && y < 237) {
                int edge = 18 + y - 208;
                if (x < edge) { contact = 1; ball.x = edge << 4; ball.vx = max(14, ball.vx); ball.vy -= max(0, ball.vy) >> 2; score += 5; }
                if (x > 150 - edge) { contact = 1; ball.x = (150 - edge) << 4; ball.vx = -max(14, -ball.vx); ball.vy -= max(0, ball.vy) >> 2; score += 5; }
            }
            x = ball.x >> 4;
            if (ball.vy > 0) {
                if (x >= 39 && x <= 76) flipper(true, x, y, previousY);
                if (x >= 84 && x <= 121) flipper(false, x, y, previousY);
            }
            if (ball.vx > 48) ball.vx = 48;
            if (ball.vx < -48) ball.vx = -48;
            if (ball.vy > 40) ball.vy = 40;
            if ((ball.y >> 4) > DRAIN_Y) {
                lives--; drains++; lights = 0; state = lives == 0 ? OVER : READY;
                ball.ready(); resetPieces(); gate = gateClock = 0;
            }
        }
        void flipper(boolean leftSide, int x, int y, int previousY) {
            boolean raised = (leftSide ? leftUp : rightUp) != 0;
            boolean swinging = raised && (leftSide ? leftSwing : rightSwing) > 0;
            int offset = leftSide ? x - 44 : 116 - x;
            int surface = FLIPPER_Y + (raised ? -(offset >> 1) : offset >> 2);
            int swept = swinging ? FLIPPER_Y + (offset >> 2) : surface;
            if (y + 3 < surface || previousY > swept + 4 || y > swept + 4) return;
            contact = 1;
            ball.y = (surface - 4) << 4;
            if (swinging) {
                ball.vy = -(60 + (offset >> 2));
                int sideways = 8 + (offset >> 1);
                ball.vx = leftSide ? sideways : -sideways;
                score += 10; flipperHits++;
            } else {
                int speed = raised ? ball.vy - (ball.vy >> 2) : ball.vy >> 1;
                ball.vy = -max(6, speed); ball.vx = leftSide ? 8 : -8;
            }
        }
    }

    static int abs(int n) { return n < 0 ? -n : n; }
    static int max(int a, int b) { return a > b ? a : b; }

    static void letter(int x, int y, int c) { GameBoy.window(x, y, 48 + c - 'A'); }
    static void digit(int x, int y, int n) { GameBoy.window(x, y, 32 + n); }
    static int statusTile(Table t, int x) {
        String message = t.state == READY ? "DOWN TO LAUNCH" : t.state == OVER ? "GAME OVER"
            : t.lights == 7 ? "TOP BONUS LIT" : "LIGHT THREE BUMPERS";
        return GameBoy.textTile(message, x, true);
    }

    static int hudCursor, hudTick;
    static void hud(Table t) {
        int status = (t.state << 1) | (t.lights == 7 ? 1 : 0);
        if (status != t.hudStatus) { t.hudStatus = status; hudCursor = 0; }
        if (hudCursor < 20) {
            GameBoy.window(hudCursor, 1, statusTile(t, hudCursor)); hudCursor++;
        }
        int phase = hudTick++ & 15;
        if (phase == 0) {
            t.hudScoreSnapshot = t.score > 99999 ? 99999 : t.score;
            t.hudRemainder = t.hudScoreSnapshot;
        }
        if (phase < 5 && t.hudScoreSnapshot != t.hudScorePainted) {
            int divisor = phase == 0 ? 10000 : phase == 1 ? 1000 : phase == 2 ? 100 : phase == 3 ? 10 : 1;
            int remaining = t.hudRemainder, value = 0;
            while (remaining >= divisor) { remaining -= divisor; value++; }
            t.hudRemainder = remaining;
            digit(4 + phase, 0, value);
            if (phase == 4) t.hudScorePainted = t.hudScoreSnapshot;
        } else if (phase == 5 && t.bonusHits != t.hudBonuses) {
            digit(15, 0, t.bonusHits > 9 ? 9 : t.bonusHits); t.hudBonuses = t.bonusHits;
        } else if (phase == 6 && t.lives != t.hudLives) {
            digit(18, 0, t.lives); t.hudLives = t.lives;
        } else if (phase >= 7 && phase <= 9) {
            int bit = 1 << (phase - 7);
            if (((t.hudLampMask ^ t.lights) & bit) != 0) {
                int x = phase == 7 ? 4 : phase == 8 ? 9 : 14;
                int y = phase == 8 ? 6 : 3;
                GameBoy.background(x, y, (t.lights & bit) != 0 ? 19 : 20);
                t.hudLampMask = (t.hudLampMask & ~bit) | (t.lights & bit);
            }
        }
    }
    static void draw(Table t) {
        Ball b = t.ball;
        int cameraY = t.camera.y;
        GameBoy.scroll(0, cameraY);
        if (t.state == OVER) GameBoy.hideSprites(0, 1);
        else {
            GameBoy.sprite(0, (b.x >> 4) - 4, (b.y >> 4) - cameraY - 4);
            int turn = (b.spin >> 7) & 3;
            if (turn != t.paintedTurn) {
                GameBoy.spriteFlip(0, turn ^ (turn >> 1)); t.paintedTurn = turn;
            }
        }
        int spring = t.state == READY ? ((b.y >> 4) - PLUNGER_Y) >> 1 : 0;
        if (spring != t.paintedPlunger) {
            GameBoy.background(18, 30, spring == 0 ? 18 : 80 + spring);
            t.paintedPlunger = spring;
        }
        boolean scrolled = cameraY != t.paintedCamera;
        boolean changed = scrolled || t.leftUp != t.paintedLeft || t.rightUp != t.paintedRight;
        if (changed) {
            if (scrolled || t.leftUp != t.paintedLeft)
                GameBoy.spriteChain(1, 40, FLIPPER_Y - 3 - cameraY, 8, t.leftUp != 0 ? -4 : 2, 4);
            if (scrolled || t.rightUp != t.paintedRight)
                GameBoy.spriteChain(5, 112, FLIPPER_Y - 3 - cameraY, -8, t.rightUp != 0 ? -4 : 2, 4);
            t.paintedLeft = t.leftUp; t.paintedRight = t.rightUp; t.paintedCamera = cameraY;
        }
        flash(9, t.left, cameraY); flash(10, t.middle, cameraY); flash(11, t.right, cameraY);
        t.upperTarget.draw(); t.lowerTarget.draw();
        if (t.gate != t.paintedGate) {
            int tiles = t.gate == 0 ? 0x030303 : t.gate == 1 ? 0x034e03 : t.gate == 2 ? 0x50004f : 0;
            GameBoy.backgroundColumn(17, 3, tiles);
            t.paintedGate = t.gate;
        }
        t.leftKicker.draw(12, cameraY); t.rightKicker.draw(13, cameraY);
    }
    static void flash(int index, Bumper b, int cameraY) {
        if (b.cooldown > 0 && b.shown != cameraY + 1) {
            GameBoy.sprite(index, b.x - 4, b.y - cameraY - 4); b.shown = cameraY + 1;
        } else if (b.cooldown == 0 && b.shown != 0) {
            GameBoy.hideSprites(index, index + 1); b.shown = 0;
        }
    }
    static void beginGame(Table table) {
        GameBoy.init(); GameBoy.viewport(VIEW_HEIGHT); GameBoy.hud(VIEW_HEIGHT);
        PinballArt.board();
        GameBoy.spriteTile(0, BALL_TILE);
        for (int i = 1; i < 9; i++) GameBoy.spriteTile(i, FLIPPER_TILE);
        for (int i = 9; i < 14; i++) GameBoy.spriteTile(i, LAMP_TILE);
        for (int i = 0; i < 20; i++) GameBoy.window(i, 1, statusTile(table, i));
        for (int i = 4; i <= 8; i++) digit(i, 0, 0);
        digit(15, 0, 0); digit(18, 0, 3);
        GameBoy.background(4, 3, 20); GameBoy.background(9, 6, 20); GameBoy.background(14, 3, 20);
        table.hudStatus = table.hudLampMask = table.hudBonuses = 0; table.hudLives = 3; hudCursor = 20;
        draw(table); GameBoy.display();
    }
    public static void main(String[] args) {
        Table table = new Table();
        PinballScores scores = new PinballScores();
        scores.load();
        GameBoy.init(); PinballArt.load();
        PinballTitle.show(scores, 0);
        int lastButtons = GameBoy.buttons();
        while (true) {
            GameBoy.waitFrame();
            int buttons = GameBoy.buttons();
            if (screen == TITLE_SCREEN) {
                frames++;
                if ((buttons & ~lastButtons & (GameBoy.START | GameBoy.A)) != 0) {
                    table.reset(); table.lastButtons = buttons;
                    beginGame(table); screen = GAME_SCREEN;
                }
            } else {
                table.step(buttons);
                frames++;
                if (table.state == OVER) {
                    scores.submit(table.score);
                    PinballTitle.show(scores, table.score); screen = TITLE_SCREEN;
                } else {
                    draw(table);
                    if (table.contact == 0) hud(table);
                }
            }
            lastButtons = buttons;
        }
    }
}
