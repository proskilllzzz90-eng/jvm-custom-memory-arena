public class OutOfMemoryException extends MemoryException {
    private final int requestedSize;
    private final int available;
    private final int capacity;
    private final int currentOffset;

    public OutOfMemoryException(int requestedSize, int available, int capacity, int currentOffset) {
        super(String.format(
            "Out of memory! Requested %d bytes, but only %d bytes available (capacity: %d, used: %d)",
            requestedSize, available, capacity, currentOffset
        ));
        this.requestedSize = requestedSize;
        this.available = available;
        this.capacity = capacity;
        this.currentOffset = currentOffset;
    }

    public int getRequestedSize() {
        return requestedSize;
    }

    public int getAvailable() {
        return available;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }
}

