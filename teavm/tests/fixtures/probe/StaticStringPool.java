package probe;
import dev.java2gb.api.*;
public final class StaticStringPool {
    @PoolCapacity(2) static final class Cell {
        final String text;
        final int number;
        Cell(String text,int number) { this.text=text; this.number=number; }
    }
    static Cell kept, latest;
    static String banner = "STATIC";
    @NoInline static int step(int number,String text) {
        Cell temporary = new Cell(text,number);
        latest = temporary;
        return temporary.text.charAt(number%3) + temporary.number + kept.text.length() + banner.length();
    }
    public static int compute() {
        kept = new Cell("KEEP",7);
        int result=0;
        for (int i=0;i<300;i++) result=result*31+step(i,"ABC");
        return result + latest.text.length();
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}
