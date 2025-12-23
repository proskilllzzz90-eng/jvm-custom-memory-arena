# JVM Custom Memory Arena

This project is a from-scratch, educational implementation of a manual memory arena in Java, built to understand how low-level memory, pointers, and data structures actually work beneath high-level language abstractions.

 

## Motivation

After ~4 years of programming in Java, a persistent and sizable chunk of its functionality as a language still feels opaque and abstract because of how it manages things under the hood:

- Objects appear without visible allocation
- Memory is automatically managed
- References feel disconnected from actual memory

This project aims to remove that abstraction.

Everything here is built on top of a single `byte[]`, and all structures, safety measures, and meaning are implemented manually.

The design emphasizes explicit control and fail-fast behavior so that incorrect assumptions about memory usage surface immediately.

 

## Project Overview

At its core, this project implements a **manual memory arena**:

- Memory is allocated explicitly
- All reads and writes are bounds-checked
- Data structures are constructed by defining layouts over raw bytes
- Pointers are represented as integer offsets
- Invalid memory access fails immediately

The project incrementally builds higher-level behavior on top of this foundation in a controlled and well-defined manner.

 

## Core Concepts Implemented

### 1. Memory Arena

A contiguous `byte[]` with a single allocation pointer (`offset`).

```
Memory Arena (128 bytes capacity)
┌─────────────────────────────────────────────────────────┐
│ 0x00 │ 0x01 │ 0x02 │ ... │ 0x0B │ 0x0C │ ... │ 0x7F     │
├──────┴──────┴──────┴─────┴──────┴──────┴─────┴──────────┤
│ ←─── Allocated (12 bytes) ───→ │ ←─── Free ────────────→│
│                                ↑                        │
│                            offset = 12                  │
└─────────────────────────────────────────────────────────┘

Rules: Allocate before use | Bounds-checked access | Reset clears all
```

 

### 2. Strict Allocation Model

All access validated against allocation boundary. Invalid access → immediate exception.

```
Valid Access:          Invalid Access:
┌─────────────┐        ┌─────────────┐
│ Allocated   │        │ Allocated   │
│   [data]    │        │   [data]    │
│             │        │             │
│ ←─✓─→       │        │ ←─✓─→  ✗──→ │
└─────────────┘        └─────────────┘
   offset=12              offset=12
                          (tries to read at 20)
```

 

### 3. Primitive Storage (Big Endian)

All primitives stored with explicit byte-level encoding.

```
Type Sizes:           Big-Endian Encoding Example (int 0x12345678):
┌─────────┬──────┐   ┌─────────────────────────────────────────┐
│ byte    │  1B  │   │ Value: 0x12345678                       │
│ boolean │  1B  │   │                                         │
│ short   │  2B  │   │ Memory Layout:                          │
│ char    │  2B  │   │ ┌──────┬──────┬──────┬──────┐           │
│ int     │  4B  │   │ │ 0x12 │ 0x34 │ 0x56 │ 0x78 │           │
│ long    │  8B  │   │ └──────┴──────┴──────┴──────┘           │
└─────────┴──────┘   │   MSB ────────────────→ LSB             │
                     │   (Most Significant Byte First)         │
                     └─────────────────────────────────────────┘


Example: storing an `int`:

Storage (big-endian approach):   
(x >>> 24) & 0xFF  →  byte[0]
(x >>> 16) & 0xFF  →  byte[1]
(x >>>  8) & 0xFF  →  byte[2]
(x >>>  0) & 0xFF  →  byte[3]

- An `int` occupies 4 bytes
- Values are stored in big-endian order: `[byte0][byte1][byte2][byte3]`
- Bit shifting: `(x >>> 24) & 0xFF` for most significant byte
- Reconstruction: `(byte0 << 24) | (byte1 << 16) | (byte2 << 8) | byte3`

This makes primitive representation and endianness explicit instead of implicit.
```
 

### 4. Struct-like Layouts (Nodes)

On top of raw memory, the project defines structured layouts through the `NodeStore` class.

A Node is defined as:

```
Node (8 bytes total):
+------------------+
| value (int)      |  offset + 0
+------------------+
| next (int)       |  offset + 4
+------------------+
```


 

### 5. Pointers and Sentinel Values

Pointers = integer offsets. `-1` = null sentinel.

```
Pointer States:
┌─────────────┬──────────────────────────────────────┐
│ Valid       │ Points to allocated memory           │
│   ptr = 8   │ ┌────┐                               │
│             │ │ 8  │→ [Node at address 8]          │
├─────────────┼──────────────────────────────────────┤
│ Null        │ Sentinel value                       │
│   ptr = -1  │ ┌────┐                               │
│             │ │ -1 │→ (end of list)                │
├─────────────┼──────────────────────────────────────┤
│ Invalid     │ Throws InvalidPointerException       │
│   ptr = 999 │ ┌────┐                               │
│             │ │999 │→ ✗ Out of bounds              │
└─────────────┴──────────────────────────────────────┘
```

 

### 6. Data Structures Built from Raw Memory

Linked list using raw memory addresses:

```
Linked List: [10] → [20] → [30] → null

Memory Layout:
┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│ 0x00│ 0x00│ 0x00│ 0x0A│ 0x00│ 0x00│ 0x00│ 0x08│  Node 1 (addr=0)
│ value=10  │ next=8 ────────────────┐          │
└────────────────────────────────────┼──────────┘
                                     │
┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│ 0x00│ 0x00│ 0x00│ 0x14│ 0x00│ 0x00│ 0x00│ 0x10│  Node 2 (addr=8)
│ value=20  │ next=16 ───────────────┐          │
└────────────────────────────────────┼──────────┘
                                     │
┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│ 0x00│ 0x00│ 0x00│ 0x1E│ 0xFF│ 0xFF│ 0xFF│ 0xFF│  Node 3 (addr=16)
│ value=30  │ next=-1 (null)                    │
└───────────────────────────────────────────────┘
```

 

## Safety Mechanisms

### Exception Error Hierarchy 

```
MemoryException (base)
├── OutOfMemoryException
│   └── "Requested X bytes, only Y available (capacity: Z, used: W)"
├── InvalidAddressException
│   └── "Address X with Y bytes needed, boundary is Z (capacity: W)"
└── InvalidPointerException
    └── "Pointer X exceeds boundary Y (node size: Z, capacity: W)"
```

### Validation Flow

```
Memory Access Request
        │
        ├─→ checkAddr(addr, size)
        │   ├─ addr >= 0?
        │   ├─ addr + size <= offset?
        │   └─→ Access granted
        │
        └─→ checkNodePtr(ptr)
            ├─ ptr == -1? (essentially null)
            ├─ ptr + NODE_SIZE <= offset? 
            └─→ Valid pointer
```

### Memory Alignment

The arena supports aligned allocation:
- `align(addr, alignment)`: Calculates next aligned address
- `allocAligned(size, alignment)`: Allocates memory at aligned boundaries
- Tracks alignment waste for memory efficiency analysis

```
Unaligned Allocation:        Aligned Allocation (4-byte):
┌──────────────────┐          ┌─────────────────┐
│ [3 bytes]        │          │ [3 bytes][pad]  │
│ offset=3         │          │ offset=3        │
│                  │          │   ↓             │
│ alloc(4) → 3? ❌ │          │ align(3,4)=4    │
└──────────────────┘          │ alloc(4) → 4 ✅ │
                              └─────────────────┘
                              Waste: 1 byte
```

This demonstrates how real systems handle memory alignment requirements.

 

## Scope and Design Constraints

- Not a production allocator
- Not optimized for performance
- Not concurrent
- Not using JVM internals or Unsafe
- Not a garbage collector replacement

The implementation prioritizes clarity, correctness, and explicit control over performance or feature completeness.

 

## Current Capabilities

```
┌─────────────────────────────────────────────────────────┐
│ Memory Management                                       │
│ • alloc(size) / allocAligned(size, alignment)           │
│ • Alignment waste tracking                              │
│ • Bounds checking on all operations                     │
│ • reset() clears arena                                  │
├─────────────────────────────────────────────────────────┤
│ Primitive Types (Big-Endian)                            │
│ • byte (1B) • short (2B) • int (4B) • long (8B)         │
│ • char (2B, UTF-16) • boolean (1B)                      │
├─────────────────────────────────────────────────────────┤
│ Data Structures                                         │
│ • NodeStore: separated node logic                       │
│ • Node layout: [value:4B][next:4B]                      │
│ • Linked list with pointer validation                   │
├─────────────────────────────────────────────────────────┤
│ Error Handling                                          │
│ • Custom exceptions with diagnostic context             │
│ • Fail-fast on invalid operations                       │
└─────────────────────────────────────────────────────────┘
```

 

## Planned Next Steps

### Phase 3: Structured Data Types
- Fixed-size arrays (`ArrayStore`)
- Dynamic arrays/vectors with growth
- String storage with UTF-16 encoding
- Hash table implementation (advanced)

### Phase 4: Advanced Memory Management
- Memory regions/segments
- Free list allocator (optional)
- Memory statistics and visualization
- Memory layout diagram generation

### Phase 5: Data Structure Operations
- Linked list operations (insert, delete, find, reverse)
- Stack implementation
- Queue implementation

### Phase 6: Testing & Documentation
- Comprehensive unit tests
- Memory layout visualization tools
- Interactive examples and demos

### Future: Frontend
- Interactive web application
- Visual memory allocation demonstration
- Educational visualization of memory operations
- Other under-the-hood Java operations

 

 