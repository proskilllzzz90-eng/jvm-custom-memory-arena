public class Main {
    public static void main(String[] args) {
        
        testBasicAllocation();
        testAlignmentFeatures();
        testNodeStore();
        testErrorMessages();
        testBigEndianEncoding();
    }

    static void testBasicAllocation() {
        System.out.println("Test 1: Basic Allocation");
        MemoryArena arena = new MemoryArena(128);
        
        int a = arena.alloc(4);
        int b = arena.alloc(4);
        int c = arena.alloc(4);
        
        System.out.println("Allocated 3 blocks of 4 bytes each:");
        System.out.println("  Address of a: " + a);
        System.out.println("  Address of b: " + b);
        System.out.println("  Address of c: " + c);
        System.out.println("  Total capacity: " + arena.capacity());
        System.out.println("  Used: " + arena.used());
        System.out.println("  Remaining: " + arena.remaining());
        
        arena.reset();
        int d = arena.alloc(4);
        System.out.println("\nAfter reset, allocated d at address: " + d);
        System.out.println("  Used after reset: " + arena.used());
        System.out.println();
    }

    static void testAlignmentFeatures() {
        System.out.println("Test 2: Memory Alignment");
        MemoryArena arena = new MemoryArena(128);
        
        System.out.println("Allocating 3 bytes (unaligned):");
        int addr1 = arena.alloc(3);
        System.out.println("  Address: " + addr1 + " (offset: " + addr1 + ")");
        System.out.println("  Alignment waste so far: " + arena.getAlignmentWaste());
        
        System.out.println("\nAllocating 4 bytes with 4-byte alignment:");
        int addr2 = arena.allocAligned(4, 4);
        System.out.println("  Address: " + addr2 + " (aligned to 4-byte boundary)");
        System.out.println("  Alignment waste: " + arena.getAlignmentWaste() + " bytes");
        System.out.println("  Used: " + arena.used());
        
        System.out.println("\nAllocating another 4 bytes with 4-byte alignment:");
        int addr3 = arena.allocAligned(4, 4);
        System.out.println("  Address: " + addr3 + " (already aligned, no waste)");
        System.out.println("  Alignment waste: " + arena.getAlignmentWaste() + " bytes");
        System.out.println("  Used: " + arena.used());
        
        System.out.println("\nTesting align() helper:");
        System.out.println("  align(0, 4) = " + arena.align(0, 4));
        System.out.println("  align(1, 4) = " + arena.align(1, 4));
        System.out.println("  align(3, 4) = " + arena.align(3, 4));
        System.out.println("  align(4, 4) = " + arena.align(4, 4));
        System.out.println("  align(5, 4) = " + arena.align(5, 4));
        System.out.println("  align(7, 4) = " + arena.align(7, 4));
        System.out.println("  align(8, 4) = " + arena.align(8, 4));
        System.out.println();
    }

    static void testNodeStore() {
        System.out.println("Test 3: NodeStore (Separated Node Logic)");
        MemoryArena arena = new MemoryArena(128);
        NodeStore nodeStore = new NodeStore(arena);
        
        System.out.println("Creating 3 nodes:");
        int node1 = nodeStore.createNode(10);
        int node2 = nodeStore.createNode(20);
        int node3 = nodeStore.createNode(30);
        
        System.out.println("  Node 1 address: " + node1 + ", value: " + nodeStore.getValue(node1));
        System.out.println("  Node 2 address: " + node2 + ", value: " + nodeStore.getValue(node2));
        System.out.println("  Node 3 address: " + node3 + ", value: " + nodeStore.getValue(node3));
        System.out.println("  Node size: " + nodeStore.getNodeSize() + " bytes");
        
        System.out.println("\nForming linked list:");
        nodeStore.setNext(node1, node2);
        nodeStore.setNext(node2, node3);
        System.out.println("  Node 1 -> Node 2 -> Node 3");
        System.out.println("  Node 1's next pointer: " + nodeStore.getNext(node1));
        System.out.println("  Node 2's next pointer: " + nodeStore.getNext(node2));
        System.out.println("  Node 3's next pointer: " + nodeStore.getNext(node3));
        
        System.out.println("\nTraversing list:");
        System.out.print("  List contents: ");
        nodeStore.printList(node1);
        System.out.println();
        
        System.out.println("\nTesting aligned node creation:");
        arena.reset();
        NodeStore alignedNodeStore = new NodeStore(arena);
        arena.alloc(3);
        int alignedNode = alignedNodeStore.createNodeAligned(42, 4);
        System.out.println("  After allocating 3 bytes, created aligned node at: " + alignedNode);
        System.out.println("  Alignment waste: " + arena.getAlignmentWaste() + " bytes");
        System.out.println();
    }

    static void testErrorMessages() {
        System.out.println("Test 4: Enhanced Error Messages");
        
        System.out.println("\nTest 4.1: Out of Memory Exception");
        try {
            MemoryArena arena = new MemoryArena(10);
            arena.alloc(5);
            arena.alloc(6);
        } catch (OutOfMemoryException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  Requested: " + e.getRequestedSize() + " bytes");
            System.out.println("  Available: " + e.getAvailable() + " bytes");
            System.out.println("  Capacity: " + e.getCapacity() + " bytes");
            System.out.println("  Current offset: " + e.getCurrentOffset());
        }
        
        System.out.println("\nTest 4.2: Invalid Address Exception");
        try {
            MemoryArena arena = new MemoryArena(20);
            arena.alloc(10);
            arena.getInt(15);
        } catch (InvalidAddressException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  Address attempted: " + e.getAddress());
            System.out.println("  Bytes needed: " + e.getBytesNeeded());
            System.out.println("  Allocated boundary: " + e.getAllocatedBoundary());
        }
        
        System.out.println("\nTest 4.3: Invalid Pointer Exception");
        try {
            MemoryArena arena = new MemoryArena(20);
            NodeStore nodeStore = new NodeStore(arena);
            int node = nodeStore.createNode(10);
            nodeStore.setNext(node, 99999);
        } catch (InvalidPointerException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  Invalid pointer: " + e.getPointer());
            System.out.println("  Node size: " + e.getNodeSize());
        }
        
        System.out.println("\nTest 4.4: Null pointer handling (should work)");
        try {
            MemoryArena arena = new MemoryArena(20);
            NodeStore nodeStore = new NodeStore(arena);
            int node = nodeStore.createNode(10);
            nodeStore.setNext(node, -1);
            System.out.println("  Successfully set next pointer to -1 (null)");
            System.out.println("  Node's next: " + nodeStore.getNext(node));
        } catch (Exception e) {
            System.out.println("  Unexpected error: " + e.getMessage());
        }
        System.out.println();
    }

    static void testBigEndianEncoding() {
        System.out.println("Test 5: Big-Endian Integer Encoding");
        MemoryArena arena = new MemoryArena(128);
        
        int addr = arena.alloc(4);
        int testValue = 0x12345678;
        arena.putInt(addr, testValue);
        
        System.out.println("Storing value: 0x" + Integer.toHexString(testValue) + " at address " + addr);
        System.out.println("Byte representation (big-endian):");
        System.out.println("  memory[" + addr + "] = 0x" + Integer.toHexString(arena.memory[addr] & 0xFF));
        System.out.println("  memory[" + (addr + 1) + "] = 0x" + Integer.toHexString(arena.memory[addr + 1] & 0xFF));
        System.out.println("  memory[" + (addr + 2) + "] = 0x" + Integer.toHexString(arena.memory[addr + 2] & 0xFF));
        System.out.println("  memory[" + (addr + 3) + "] = 0x" + Integer.toHexString(arena.memory[addr + 3] & 0xFF));
        
        int reconstructed = arena.getInt(addr);
        System.out.println("Reconstructed value: 0x" + Integer.toHexString(reconstructed));
        System.out.println("Match: " + (testValue == reconstructed));
        System.out.println();
        
        System.out.println("=== ALL TESTS COMPLETE ===");
    }
}
