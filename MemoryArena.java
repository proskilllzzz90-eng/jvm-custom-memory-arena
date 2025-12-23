public class MemoryArena {
    public final byte[] memory;
    private int offset = 0;
    private int alignmentWaste = 0;

    public MemoryArena(int size) {
        memory = new byte[size];
    }

    public int align(int addr, int alignment) {
        if (alignment <= 0) {
            return addr;
        }
        int remainder = addr % alignment;
        if (remainder == 0) {
            return addr;
        }
        return addr + (alignment - remainder);
    }

    public int alloc(int size) {
        if (offset + size > memory.length) {
            throw new OutOfMemoryException(size, remaining(), capacity(), offset);
        }
        int start = offset;
        offset += size;
        return start;
    }

    public int allocAligned(int size, int alignment) {
        int alignedOffset = align(offset, alignment);
        int waste = alignedOffset - offset;
        
        if (alignedOffset + size > memory.length) {
            throw new OutOfMemoryException(size, remaining(), capacity(), offset);
        }
        
        alignmentWaste += waste;
        offset = alignedOffset + size;
        return alignedOffset;
    }

    public void reset() {
        offset = 0;
        alignmentWaste = 0;
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
        checkAddr(addr, 1);
        memory[addr] = x;
    }

    public byte getByte(int addr) {
        checkAddr(addr, 1);
        return memory[addr];
    }

    public void putInt(int addr, int x) {
        int[] bytes = {(x >>> 24) & 0xFF, (x >>> 16) & 0xFF, (x >>> 8) & 0xFF, (x >>> 0) & 0xFF};
        checkAddr(addr, 4);
        for (int i = 0; i < 4; i++) {
            memory[addr + i] = (byte) bytes[i];
        }
    }

    public int getInt(int addr) {
        checkAddr(addr, 4);
        int reconstruct = (memory[addr] & 0xFF) << 24 | (memory[addr + 1] & 0xFF) << 16 | (memory[addr + 2] & 0xFF) << 8 | (memory[addr + 3] & 0xFF);
        return reconstruct;
    }

    public boolean checkAddr(int addr, int bytesNeeded) {
        if (addr >= 0 && addr + bytesNeeded <= offset) {
            return true;
        }
        throw new InvalidAddressException(addr, bytesNeeded, offset, capacity());
    }

    public int getAlignmentWaste() {
        return alignmentWaste;
    }

    public void resetAlignmentWaste() {
        alignmentWaste = 0;
    }
}
