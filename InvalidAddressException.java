public class InvalidAddressException extends MemoryException {
    private final int address;
    private final int bytesNeeded;
    private final int allocatedBoundary;
    private final int capacity;

    public InvalidAddressException(int address, int bytesNeeded, int allocatedBoundary, int capacity) {
        super(String.format(
            "Invalid memory access! Address %d with %d bytes needed, but allocated boundary is %d (capacity: %d)",
            address, bytesNeeded, allocatedBoundary, capacity
        ));
        this.address = address;
        this.bytesNeeded = bytesNeeded;
        this.allocatedBoundary = allocatedBoundary;
        this.capacity = capacity;
    }

    public int getAddress() {
        return address;
    }

    public int getBytesNeeded() {
        return bytesNeeded;
    }

    public int getAllocatedBoundary() {
        return allocatedBoundary;
    }

    public int getCapacity() {
        return capacity;
    }
}

