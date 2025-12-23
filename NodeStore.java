public class NodeStore {
    private final MemoryArena arena;
    private static final int NODE_SIZE = 8;
    private static final int VALUE_OFFSET = 0;
    private static final int NEXT_OFFSET = 4;

    public NodeStore(MemoryArena arena) {
        this.arena = arena;
    }

    public int createNode(int val) {
        int nodeAddr = arena.alloc(NODE_SIZE);
        arena.putInt(nodeAddr + VALUE_OFFSET, val);
        arena.putInt(nodeAddr + NEXT_OFFSET, -1);
        return nodeAddr;
    }

    public int createNodeAligned(int val, int alignment) {
        int nodeAddr = arena.allocAligned(NODE_SIZE, alignment);
        arena.putInt(nodeAddr + VALUE_OFFSET, val);
        arena.putInt(nodeAddr + NEXT_OFFSET, -1);
        return nodeAddr;
    }

    public int getValue(int nodeAddr) {
        checkNodePtr(nodeAddr);
        return arena.getInt(nodeAddr + VALUE_OFFSET);
    }

    public void setValue(int nodeAddr, int val) {
        checkNodePtr(nodeAddr);
        arena.putInt(nodeAddr + VALUE_OFFSET, val);
    }

    public void setNext(int nodeAddr, int nextAddr) {
        checkNodePtr(nodeAddr);
        checkNodePtr(nextAddr);
        arena.putInt(nodeAddr + NEXT_OFFSET, nextAddr);
    }

    public int getNext(int nodeAddr) {
        checkNodePtr(nodeAddr);
        return arena.getInt(nodeAddr + NEXT_OFFSET);
    }

    public void printList(int headAddr) {
        if (headAddr == -1) {
            return;
        }
        if (checkNodePtr(headAddr)) {
            System.out.print(getValue(headAddr) + " ");
            int current = headAddr;
            while (getNext(current) != -1) {
                current = getNext(current);
                checkNodePtr(current);
                System.out.print(getValue(current) + " ");
            }
        }
    }

    public boolean checkNodePtr(int ptr) {
        if (ptr == -1) {
            return true;
        }
        if (ptr >= 0 && ptr + NODE_SIZE <= arena.used()) {
            return true;
        }
        throw new InvalidPointerException(ptr, NODE_SIZE, arena.used(), arena.capacity());
    }

    public int getNodeSize() {
        return NODE_SIZE;
    }
}
