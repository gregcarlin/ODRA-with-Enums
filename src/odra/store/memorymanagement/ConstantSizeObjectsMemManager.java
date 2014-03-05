package odra.store.memorymanagement;

import java.util.HashSet;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.DatabaseRuntimeException;

import odra.store.io.IHeap;
import odra.system.Sizes;

/**
 * This class contains a memory allocator designed for storing objects equal or
 * less then given objsize. Should NOT be used to store string or binary objects
 * or lists of backward and child references. Advantage of this memory manager
 * is constant time memory allocation and lack of any form of fragmentation.
 * 
 * @author tkowals
 */

public class ConstantSizeObjectsMemManager extends AbstractMemoryManager {

   private int hcount; // object counter offset

   private int hentry; // entry offset (first block representing database entry

   // object)

   private int hend; // heap end offset

   private int hstart; // heap start offset

   private int hfstack; // position of free space informing stack

   private int count; // auxiliary objects counter for speed improvement

   private int fstacksize; // auxiliary stack size counter for speed improvement

   private int maxobjsize; // size of objects store

   private int capacity; // how many objects can be stored

   /**
    * Initializes the memory allocator using an object representing the space
    * the allocator is supposed to manage.
    */
   public ConstantSizeObjectsMemManager(IHeap heap, int maxobjsize) throws DatabaseException {
	  super(heap);
      this.hcount = heap.getStartOffset();
      this.hentry = hcount + Sizes.INTVAL_LEN;
      this.hstart = hentry + Sizes.INTVAL_LEN;
      this.hend = heap.getStartOffset() + heap.getUserSpaceLength() - 1;
      this.maxobjsize = maxobjsize;
      this.capacity = (hend - hstart - Sizes.INTVAL_LEN) / (maxobjsize + Sizes.INTVAL_LEN);
      this.hfstack = hstart + capacity * maxobjsize;

      count = this.heap.readInteger(this.hcount);
      fstacksize = this.heap.readInteger(this.hfstack);

   }

   /**
    * Initializes the memory allocator by setting up the first free memory
    * block. The block spans the whole user space.
    */
   public void initialize() {
      try {
         this.heap.writeInteger(this.hcount, 0);
         count = 0;
         this.heap.writeInteger(this.hfstack, 0);
         fstacksize = 0;
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("initialize", ex);
      }
   }

   /**
    * @return position where the first block will be allocated
    */
   public int getEntryOffset() {
      try {
         return this.heap.readInteger(this.hentry);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("getEntryOffset", ex);
      }
   }

   /**
    * Sets the address of a new entry (i.e. first memory block) of the heap
    */
   public void setEntryOffset(int value) {
      try {
         this.heap.writeInteger(this.hentry, value);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("setEntryOffset", ex);
      }
   }

   public int free(int offset) {
      try {
         assert offset >= this.hstart && offset < this.hfstack : offset;
         this.heap.writeInteger(this.hfstack, this.fstacksize += Sizes.INTVAL_LEN);
         this.heap.writeInteger(this.hfstack + this.fstacksize, offset);
         this.heap.writeInteger(this.hcount, --this.count);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("free", ex);
      }
      return offset;
   }

   public int malloc(int nbytes) throws DatabaseException {
      assert nbytes <= maxobjsize : "ConstantSizeObjectsMemManager requested to allocate " + nbytes
               + " while limit is " + maxobjsize;
      if (hstart + (count + 1) * maxobjsize > hfstack) {
         throw new DatabaseException("Out of memory for objects");
      }

      this.heap.writeInteger(this.hcount, ++this.count);

      if (fstacksize == 0) {
         return this.hstart + (this.count - 1) * this.maxobjsize;
      }

      int offset = this.heap.readInteger(this.hfstack + this.fstacksize);
      this.heap.writeInteger(this.hfstack, this.fstacksize -= Sizes.INTVAL_LEN);

      return offset;
   }

   public int malloc(int offset, int nbytes) throws DatabaseException {
	   assert false : "malloc for the given offset is not implemented for ConstantSizeObjectsMenManager";
   	   return 0;
   }
   
   public int falloc(int offset, int nbytes) throws DatabaseException {
      throw new DatabaseException(
               "ERROR - memory manager used does not supports storing dynamically managed objects");
   }

   public int realloc(int offset, int nbytes) throws DatabaseException {
      throw new DatabaseException(
               "ERROR - memory manager used does not supports storing dynamically managed objects");
   }

   public boolean staticRealloc(int offset, int nbytes) throws DatabaseException {
      throw new DatabaseException(
               "ERROR - memory manager used does not supports storing dynamically managed objects");
   }

   
	public byte[] getData(int offset) throws DatabaseException {
		
		throw new DatabaseException(
        	"ERROR - memory manager used does not supports storing dynamically managed objects");
	
	}

	public void setData(int offset, byte[] buf) throws DatabaseException {
		
		throw new DatabaseException(
        	"ERROR - memory manager used does not supports storing dynamically managed objects");
		
	}
   
   /**
    * Returns a total memory size.
    * 
    * @return total memory
    */
   public int getTotalMemory() {
      return capacity * (maxobjsize + Sizes.INTVAL_LEN);
   }

   /**
    * Returns a free memory size.
    * 
    * @return free memory
    */
   public int getFreeMemory() {
      return (capacity - count) * (maxobjsize + Sizes.INTVAL_LEN);
   }

   /**
    * Returns an used memory size.
    * 
    * @return used memory
    */
   public int getUsedMemory() {
      return count * (maxobjsize + Sizes.INTVAL_LEN);
   }

   public String dump(boolean verbose) {
      try {
         StringBuffer buf = new StringBuffer();

         int stack = this.hfstack + this.fstacksize;

         if (verbose) while (stack > this.hfstack) {
            int freeoffset = this.heap.readInteger(stack);
            assert freeoffset >= this.hstart && freeoffset < this.hfstack : "the object heap seems to be broken! "
                     + freeoffset + "beyond (" + hstart + ", " + hfstack + ")";
            buf.append(freeoffset + " ");

            stack -= Sizes.INTVAL_LEN;
         }
         buf.append(")\n");

         return new StringBuffer("(free/objects stored/capacity): " + (capacity - count) + "/"
                  + count + "/" + capacity + "\nfree segments list : (").append(buf).toString();
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("dump", ex);
      }
   }

@Override
public Vector<Integer> getObjectsInSequence() throws DatabaseException {
	
	Vector<Integer> objs = new Vector<Integer>(count);

	HashSet<Integer> freespaces = new HashSet<Integer>();
		
	int stack = this.hfstack + this.fstacksize;

	
    while (stack > this.hfstack) {
       freespaces.add(this.heap.readInteger(stack));
       stack -= Sizes.INTVAL_LEN;
    }

    int offset = hstart; 
    for(int i = 0; i < count; ) {
    	if (!freespaces.contains(offset)) {
    		objs.add(offset);
    		i++;
    	}
    	offset += maxobjsize; 
    }
	
	return objs;
}

   // TODO Add return memory utilization method (for expanding)
}