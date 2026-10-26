package probe;
public final class ArrayGraph {
    static final class State { private final int[] data = new int[4]; }
    @NoInline static int read(State state) { return state.data[0] + state.data[3]; }
    @NoInline static int scratch(int[] data) { return data[0]; }
    @Mutable
    public static int compute() {
        State state = new State();
        state.data[0] = 7;
        int[] temporary = new int[4];
        temporary[0] = 99;
        return scratch(temporary) + read(state);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}
