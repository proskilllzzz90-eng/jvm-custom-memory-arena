public class MemoryArena {
    public final byte[] memory;
    private int offset = 0;

    public MemoryArena(int size) {
        memory = new byte[size];
    }

    public int alloc(int size) {
        if (offset + size > memory.length) {
            throw new RuntimeException("Out of memory!");
        }
        int start = offset;
        offset += size;
        return start;
    }

    public void reset() {
        offset = 0;
    }

    public int capacity() {
        return memory.length;
    }

    public int used() {
        return offset;
    }

    public int remaining() {
        return memory.length - offset;
    }

    public void putByte(int addr, byte x) {
        memory[addr] = x;
    }

    public byte getByte(int addr) {
        return memory[addr];
    }

    //big endian approach
    public void putInt(int addr, int x) {
        int[] bytes = {(x >>> 24) & 0xFF, (x >>> 16) & 0xFF, (x >>> 8) & 0xFF, (x >>> 0) & 0xFF};

        for (int i = 0; i < 4; i++) {
            memory[addr + i] = (byte) bytes[i];
        }
    }

    public int getInt(int addr) {
        int reconstruct = (memory[addr] & 0xFF) << 24 | (memory[addr + 1] & 0xFF) << 16 | (memory[addr + 2] & 0xFF) << 8 | (memory[addr + 3] & 0xFF);
        return reconstruct;
    }
}
