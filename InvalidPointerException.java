public class InvalidPointerException extends MemoryException {
    private final int pointer;
    private final int nodeSize;
    private final int allocatedBoundary;
    private final int capacity;

    public InvalidPointerException(int pointer, int nodeSize, int allocatedBoundary, int capacity) {
        super(String.format(
            "Invalid pointer! Pointer %d (node size: %d) exceeds allocated boundary %d (capacity: %d). Use -1 for null pointer.",
            pointer, nodeSize, allocatedBoundary, capacity
        ));
        this.pointer = pointer;
        this.nodeSize = nodeSize;
        this.allocatedBoundary = allocatedBoundary;
        this.capacity = capacity;
    }

    public int getPointer() {
        return pointer;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public int getAllocatedBoundary() {
        return allocatedBoundary;
    }

    public int getCapacity() {
        return capacity;
    }
}

