package probe;
import dev.java2gb.api.*;
public final class PinballFeel {
    static void place(PinballGame.Table t,int x,int y,int vx,int vy) {
        PinballPhysics.place(t,x,y,vx,vy); t.ball.gravityPhase = 0;
    }
    public static int compute() {
        PinballGame.Table t = new PinballGame.Table();
        int checks = 0;
        place(t,80,112,0,0);
        for (int i=0;i<20;i++) t.step(0);
        if (t.ball.vy == 10 && t.ball.y == (112<<4)+100) checks |= 1;
        place(t,80,112,0,0);
        int fall = 0;
        while (t.ball.y < (212<<4) && fall < 100) { t.step(0); fall++; }
        if (fall == 80 && t.ball.vy == 40) checks |= 2;
        place(t,80,112,0,56); t.step(0);
        if (t.ball.vy == 40 && t.ball.y == (112<<4)+40) checks |= 4;
        place(t,18,112,-32,8); t.step(0);
        if (t.ball.vx == 28 && t.ball.vy == 8 && t.ball.x == 18<<4) checks |= 8;
        t.reset(); place(t,40,46,8,32); t.step(0);
        if (t.ball.vx == 8 && t.ball.vy == -32 && t.bumperHits == 1) checks |= 16;
        t.left.cooldown = 8; place(t,40,46,8,32); t.step(0);
        if (t.ball.vy == -32 && t.bumperHits == 1 && t.score == 100) checks |= 32;
        t.reset(); place(t,48,48,-24,8); t.step(0);
        if (t.ball.vx == 8 && t.ball.vy == -24 && t.ball.vx+t.ball.vy == -16) checks |= 64;
        if (PinballGame.abs((t.ball.x>>4)-40)+PinballGame.abs((t.ball.y>>4)-56) >= 18) checks |= 128;
        t.reset(); place(t,56,124,8,32); t.step(0);
        if (t.ball.vx == 8 && t.ball.vy == -24 && t.upperTarget.down == 1) checks |= 256;
        t.reset(); t.leftKicker.cooldown = 8; place(t,32,184,0,24); t.step(0);
        if (t.ball.vx == 8 && t.ball.vy == -18 && t.kickerHits == 0 && t.score == 0) checks |= 512;
        t.reset(); place(t,60,237,0,8); t.step(GameBoy.LEFT);
        if (t.flipperHits == 1 && t.ball.vy == -64 && t.ball.vx == 16) checks |= 1024;
        t.reset(); place(t,60,247,0,16); t.step(GameBoy.LEFT);
        if (t.flipperHits == 0 && t.ball.vy >= 0) checks |= 2048;
        t.reset(); place(t,80,112,0,0); t.step(GameBoy.LEFT);
        for (int i=0;i<6;i++) t.step(GameBoy.LEFT);
        place(t,60,224,0,32); t.step(GameBoy.LEFT);
        if (t.flipperHits == 0 && t.ball.vy == -24 && t.score == 0) checks |= 4096;
        t.reset(); place(t,60,221,0,16);
        for (int i=0;i<4;i++) t.step(GameBoy.LEFT);
        if (t.flipperHits == 1 && t.ball.vy == -64) checks |= 8192;
        boolean outward = true;
        for (int i=0;i<8;i++) {
            int dx = i==0 || i==2 ? 0 : i==1 ? 10 : i==3 ? -10 : i<6 ? 7 : -7;
            int dy = i==1 || i==3 ? 0 : i==0 ? -10 : i==2 ? 10 : (i==4 || i==7) ? -7 : 7;
            int vx = dx>0 ? -16 : dx<0 ? 16 : 0;
            int vy = dy>0 ? -16 : dy<0 ? 16 : 0;
            t.reset(); t.left.cooldown = 8; place(t,40+dx,56+dy,vx,vy); t.step(0);
            int normal = (dx<0 ? -t.ball.vx : dx>0 ? t.ball.vx : 0)
                + (dy<0 ? -t.ball.vy : dy>0 ? t.ball.vy : 0);
            int ax = PinballGame.abs((t.ball.x>>4)-40), ay = PinballGame.abs((t.ball.y>>4)-56);
            if (normal<=0 || !(ax>11 || ay>11 || ax+ay>16) || t.score!=0) outward = false;
        }
        if (outward) checks |= 16384;
        t.reset(); place(t,46,231,0,32); t.step(GameBoy.LEFT);
        int base = -t.ball.vy;
        t.reset(); place(t,70,219,0,32); t.step(GameBoy.LEFT);
        if (base == 60 && -t.ball.vy == 66 && -t.ball.vy > base) checks |= 32768;
        t.reset(); place(t,40,46,0,24); t.step(0);
        for (int i=0;i<10;i++) t.step(0);
        if (t.bumperHits == 1 && t.score == 100) checks |= 65536;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}
