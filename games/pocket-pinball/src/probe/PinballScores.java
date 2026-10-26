package probe;

final class PinballScores {
    int first, second, third, fourth, fifth;
    int active = -1, sequence;
    int get(int index) {
        return index == 0 ? first : index == 1 ? second : index == 2 ? third : index == 3 ? fourth : fifth;
    }
    void set(int index, int value) {
        if (index == 0) first = value;
        else if (index == 1) second = value;
        else if (index == 2) third = value;
        else if (index == 3) fourth = value;
        else fifth = value;
    }
    static int readInt(int address) {
        return GameBoy.saveRead(address) | (GameBoy.saveRead(address + 1) << 8)
            | (GameBoy.saveRead(address + 2) << 16) | (GameBoy.saveRead(address + 3) << 24);
    }
    static void writeInt(int address, int value) {
        for (int i = 0; i < 4; i++) GameBoy.saveWrite(address + i, (value >>> (i << 3)) & 255);
    }
    static int checksum(int base) {
        int hash = 19229;
        for (int i = 1; i < 28; i++) hash = (hash * 31 + GameBoy.saveRead(base + i)) & 65535;
        return hash;
    }
    static boolean valid(int base) {
        if (GameBoy.saveRead(base) != 165 || GameBoy.saveRead(base + 1) != 80
                || GameBoy.saveRead(base + 2) != 66 || GameBoy.saveRead(base + 3) != 1) return false;
        int sequence = readInt(base + 4);
        if (sequence < 0 || sequence > 1073741823) return false;
        int hash = checksum(base), stored = readInt(base + 28);
        if ((stored & 65535) != hash || ((stored >>> 16) & 65535) != (hash ^ 65535)) return false;
        int previous = 99999;
        for (int i = 0; i < 5; i++) {
            int score = readInt(base + 8 + (i << 2));
            if (score < 0 || score > previous) return false;
            previous = score;
        }
        return true;
    }
    @NoInline void load() {
        GameBoy.saveOpen();
        boolean a = valid(0), b = valid(32);
        int slot = -1;
        if (a) slot = 0;
        if (b) {
            int difference = (readInt(36) - readInt(4)) & 1073741823;
            if (!a || (difference > 0 && difference < 536870912)) slot = 1;
        }
        first = second = third = fourth = fifth = sequence = 0; active = slot;
        if (slot >= 0) {
            int base = slot << 5;
            sequence = readInt(base + 4);
            for (int i = 0; i < 5; i++) set(i, readInt(base + 8 + (i << 2)));
        }
        GameBoy.saveClose();
    }
    @NoInline boolean submit(int score) {
        if (score <= 0 || score <= fifth) return false;
        if (score > 99999) score = 99999;
        int place = 0;
        while (place < 5 && score <= get(place)) place++;
        if (place == 5) return false;
        for (int i = 4; i > place; i--) set(i, get(i - 1));
        set(place, score);
        int next = active == 0 ? 1 : 0, base = next << 5;
        int nextSequence = (sequence + 1) & 1073741823;
        GameBoy.saveOpen();
        GameBoy.saveWrite(base, 0);  
        GameBoy.saveWrite(base + 1, 80); GameBoy.saveWrite(base + 2, 66); GameBoy.saveWrite(base + 3, 1);
        writeInt(base + 4, nextSequence);
        for (int i = 0; i < 5; i++) writeInt(base + 8 + (i << 2), get(i));
        int hash = checksum(base);
        writeInt(base + 28, hash | ((hash ^ 65535) << 16));
        GameBoy.saveWrite(base, 165);  
        GameBoy.saveClose();
        active = next; sequence = nextSequence;
        return true;
    }
}
