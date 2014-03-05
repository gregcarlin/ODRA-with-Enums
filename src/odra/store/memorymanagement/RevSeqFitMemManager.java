package odra.store.memorymanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.DatabaseRuntimeException;
import odra.store.io.IHeap;
import odra.system.Sizes;
import odra.system.config.ConfigDebug;
import odra.system.config.ConfigServer;

/**
 * This class contains a simple memory allocator built in accordance with the
 * sequential-fit algorithm.
 * 
 * @author raist, tkowals
 */

public class RevSeqFitMemManager extends AbstractMemoryManager implements IMemoryManager {

   /**
    * free memory counter offset
    */
   private int hfree;

   /**
    * entry offset (first block representing database entry object)
    */
   private int hentry;

   /**
    * heap end offset
    */
   private int hend;

   /**
    * heap start offset
    */
   private int hstart;

   /**
    * memory size (auxiliary attribute to count memory utilization)
    */
   private int memory;

   /**
    * auxiliary free memory counter
    */
   private int free;

   /**
    * minimal amount of free memory (trick to avoid big degree fragmentation)
    */
   private int minfree;

   /**
    * Initializes the memory allocator using an object representing the space
    * the allocator is supposed to manage.
    */
   public RevSeqFitMemManager(IHeap heap, int minfree) throws DatabaseException {
      super(heap);
      this.hfree = heap.getStartOffset();
      this.hentry = hfree + Sizes.INTVAL_LEN;
      this.hstart = hentry + Sizes.INTVAL_LEN;
      this.hend = heap.getStartOffset() + heap.getUserSpaceLength() - 1;
      memory = hend - hstart - MBHEAD_LEN;
      this.free = this.heap.readInteger(hfree);
      this.minfree = (int) (((double) minfree / 100) * memory);
   }

   public RevSeqFitMemManager(IHeap heap) throws DatabaseException {
      this(heap, 0);
   }

   /**
    * Initializes the memory allocator by setting up the first free memory
    * block. The block spans the whole user space.
    */
   public void initialize() {
      try {
         setHeader(hstart, FREE, 0, hend + 1);
         this.heap.writeInteger(this.hfree, this.memory);
         this.free = this.memory;
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

   /**
    * Allocate nbytes of memory and return the address of the block
    */
   public final int malloc(int nbytes) throws DatabaseException {
      assert nbytes > 0 : "malloc of " + nbytes;
      if (nbytes > hend - hstart - MBHEAD_LEN) 
    	  throw new DatabaseException("Not enough free space (" + nbytes + " bytes)");
      
      if (nbytes < MBMINIMAL_LEN) {
         nbytes = MBMINIMAL_LEN;
      }

      if (minfree > free - nbytes - MBHEAD_LEN) {
         throw new DatabaseException("Out of minimal free space (" + nbytes + " bytes)");
      }

      int bstart = hstart;
      int bstatus;
      int blength;

      while (bstart <= hend) {
         bstatus = getState(bstart);

         if (bstatus == FREE) {
            blength = getLength(bstart);

            if (blength >= nbytes) {
               if (blength - nbytes - MBHEAD_LEN >= MBMINIMAL_LEN) {
                  int currnext = getNext(bstart);
                  int newstart = currnext - nbytes - MBHEAD_LEN;

                  setNext(bstart, newstart);

                  setHeader(newstart, OCCUPIED, bstart, currnext);

                  if (currnext <= hend) {
                     setPrevious(currnext, newstart);
                  }

                  bstart = newstart;

                  // free memory control
                  this.heap.writeInteger(this.hfree, this.free -= MBHEAD_LEN);
               } else {
                  setState(bstart, OCCUPIED);
               }

               this.heap.writeInteger(this.hfree, this.free -= getLength(bstart));
               return bstart + MBHEAD_LEN;
            }
         }

         bstart = getNext(bstart);
      }

      ConfigServer.getLogWriter().getLogger().log(java.util.logging.Level.WARNING, "Out of memory");

      throw new DatabaseException("Not enough free space (" + nbytes + " bytes)");
   }

   public int malloc(int offset, int nbytes) throws DatabaseException {
		
	   offset -= MBHEAD_LEN;
	   
	   int bstatus = getState(offset);

       if (bstatus == FREE) {
          int blength = getLength(offset);

          if (blength >= nbytes) {
             if (blength - nbytes - MBHEAD_LEN >= MBMINIMAL_LEN) {
                int currnext = getNext(offset);
                int newstart = currnext - nbytes - MBHEAD_LEN;

                setNext(offset, newstart);

                setHeader(newstart, OCCUPIED, offset, currnext);

                if (currnext <= hend) {
                   setPrevious(currnext, newstart);
                }

                offset = newstart;

                // free memory control
                this.heap.writeInteger(this.hfree, this.free -= MBHEAD_LEN);
             } else {
                setState(offset, OCCUPIED);
             }

             this.heap.writeInteger(this.hfree, this.free -= getLength(offset));
             return offset + MBHEAD_LEN;
          }
       }

       throw new DatabaseException("Not enough free space (" + nbytes + " bytes to malloc " + offset +" offset)");
	}
   
   /**
    * Helper for falloc and realloc operations. Perform realloc if the existing
    * block can be used in order to allocate memory and preserve memory block
    * contents 1) if the old block is larger or equal to the new block, returns
    * the true 2) if the new block is larger than the old block and if the next
    * block is free then if both blocks merged are larger or equal to the new
    * block performs merging and returns true 3) otherwise return false
    */

   public final boolean staticRealloc(int offset, int nbytes) throws DatabaseException {
      assert nbytes > 0 : "staticRealloc of " + nbytes;
      if (nbytes > hend - hstart - MBHEAD_LEN) 
    	  return false;

      if (nbytes < MBMINIMAL_LEN) {
         nbytes = MBMINIMAL_LEN;
      }
      int start = offset - MBHEAD_LEN;
      int currlen = getLength(start);

      if (currlen >= nbytes) {
         if (currlen - MBHEAD_LEN - nbytes >= MBMINIMAL_LEN) {
            int currnext = getNext(start);

            int newstart = offset + nbytes;

            setNext(start, newstart);
            setHeader(newstart, OCCUPIED, start, currnext);

            if (currnext <= hend) {
               setPrevious(currnext, newstart);
            }
            
            // free memory control
            this.free(newstart + MBHEAD_LEN);
         }
         return true;
      }

      int next = getNext(start);

      if (next <= hend && getState(next) == FREE) {
         int blen = getLength(start) + (MBHEAD_LEN + getLength(next));

         if (blen >= nbytes) {
            // merge current + right
            int nextnext = getNext(next);

            // free memory control
            this.heap.writeInteger(this.hfree, this.free -= getLength(next));

            if (blen - MBHEAD_LEN - nbytes >= MBMINIMAL_LEN) {
               int newnext = start + MBHEAD_LEN + nbytes;

               setNext(start, newnext);
               setHeader(newnext, FREE, start, nextnext);

               if (nextnext <= hend) {
                  setPrevious(nextnext, newnext);
               }

               // free memory control
               this.heap.writeInteger(this.hfree, this.free += getLength(newnext));
            } else {
               setNext(start, nextnext);

               if (nextnext <= hend) {
                  setPrevious(nextnext, start);
               }
            }

            return true;
         }
      }

      return false;
   }

   /**
    * "Fast" memory allocator. Strives to use the existing block in order to
    * allocate memory. 1) if the old block is larger or equal to than the new
    * block, returns the old block 2) if the new block is larger than the old
    * block, checks if the next block is free. if the block is free, both blocks
    * are merged and split (if necessary) 3) if the new block is larger than the
    * old block and the next block is used, frees the old block and allocates a
    * new one
    */
   public final int falloc(int offset, int nbytes) throws DatabaseException {
	  assert nbytes > 0 : "falloc of " + nbytes;

      if (staticRealloc(offset, nbytes)) {
         return offset;
      }
      free(offset);
      return malloc(nbytes * 2);
   }

   /**
    * Memory reallocator. Strives to use the existing block in order to allocate
    * memory and preserve memory block contents 1) if the old block is larger or
    * equal to the new block, returns the old block 2) if the new block is
    * larger than the old block, checks if the next block is free. if the block
    * is free, both blocks are merged and split (if necessary) 3) if the new
    * block is larger than the old block and the next block is used, allocates a
    * new one, copies the old block contents into a new block and frees the old
    * block
    */
   public final int realloc(int offset, int nbytes) throws DatabaseException {
      assert nbytes > 0 : "realloc of " + nbytes;

      if (staticRealloc(offset, nbytes)) {
         return offset;
      }

      int noffset = malloc(nbytes * 2);

      setData(noffset, getData(offset));
      free(offset);
      return noffset;
   }

   /**
    * @return data from memory block
    */
   public final byte[] getData(int offset) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend;
         }

         int start = offset - MBHEAD_LEN;
         int oldlen = getLength(start);

         byte[] buf = new byte[oldlen];
         this.heap.read(offset, buf);
         return buf;
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("getData", ex);
      }
   }

   /**
    * Sets a data of memory blocks
    */
   public final void setData(int offset, byte[] buf) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend;
         }
         this.heap.write(offset, buf);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("setDAta", ex);
      }
   }

   /**
    * Makes the memory block starting at current free and merges it with its
    * adjacent blocks (if possible).
    * @return address of free memory block
    */
   public final int free(int offset) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart : offset;
         }

         offset -= MBHEAD_LEN;

         if (ConfigDebug.ASSERTS) { 
        	 assert getState(offset) == OCCUPIED : "free space (" + getState(offset) + ") cannot be made free";
         }

         // System.out.println("free: " + offset + " (" + (getNext(offset) -
         // offset
         // - MBHEAD_LEN) + ")");

         int prev = getPrevious(offset);
         int next = getNext(offset);

         int pstate = prev == 0 ? OCCUPIED : getState(prev);
         int nstate = next > hend ? OCCUPIED : getState(next);

         setState(offset, FREE);

         // free memory control
         this.heap.writeInteger(this.hfree, this.free += getLength(offset));

         if (pstate == OCCUPIED) {
            if (nstate == FREE) {
               // merge current + right
               setNext(offset, getNext(next));

               if (getNext(next) <= hend) {
                  setPrevious(getNext(next), offset);
               }

               // free memory control
               this.heap.writeInteger(this.hfree, this.free += MBHEAD_LEN);
            }
         } else if (nstate == OCCUPIED) {
            // merge left + current
            setNext(prev, next);

            if (next <= hend) {
               setPrevious(next, prev);
            }

            // free memory control
            this.heap.writeInteger(this.hfree, this.free += MBHEAD_LEN);
            
            return prev + MBHEAD_LEN;
         } else {
            // merge left + current + right
            setNext(prev, getNext(next));

            if (getNext(next) <= hend) {
               setPrevious(getNext(next), prev);
            }

            // free memory control
            this.heap.writeInteger(this.hfree, this.free += 2 * MBHEAD_LEN);
            
            return prev + MBHEAD_LEN;
         }
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("free", ex);
      }
      
      return offset + MBHEAD_LEN;
   }

   /**
    * Set memory block header
    */

   private final void setHeader(int offset, byte state, int prev, int next) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend && (state == FREE || state == OCCUPIED)
                     && (prev >= hstart || prev == 0) && prev <= hend : offset;
         }
         int _offset = offset + MB_STATE_POS;
         this.heap.writeByte(_offset, state);
         this.heap.writeInteger(_offset += Sizes.BYTEVAL_LEN, prev);
         this.heap.writeInteger(_offset += Sizes.INTVAL_LEN, next);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("setHeader", ex);
      }
   }

   /**
    * Sets a new state of the memory block
    */
   private final void setState(int offset, byte state) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend && (state == FREE || state == OCCUPIED);
         }
         this.heap.writeByte(offset + MB_STATE_POS, state);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("setState", ex);
      }
   }

   /**
    * Sets a previous element in the list of memory blocks
    */
   private final void setPrevious(int offset, int prev) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend && (prev >= hstart || prev == 0) && prev <= hend : offset
                     + " " + prev;
         }
         this.heap.writeInteger(offset + MB_PREVIOUS_POS, prev);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("setPrevious", ex);
      }
   }

   /**
    * Sets a next element in the list of memory blocks
    */
   private final void setNext(int offset, int next) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend;
         }
         this.heap.writeInteger(offset + MB_NEXT_POS, next);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("setNext", ex);
      }
   }

   /**
    * @return state of a memory block (used or free)
    */
   private final byte getState(int offset) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend : offset;
         }
         return this.heap.readByte(offset + MB_STATE_POS);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("getState", ex);
      }
   }

   /**
    * @return previous memory block in the list
    */
   private final int getPrevious(int offset) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend;
         }
         return this.heap.readInteger(offset + MB_PREVIOUS_POS);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("getPrevious", ex);
      }
   }

   /**
    * @return next memory block in the list
    */
   private final int getNext(int offset) {
      try {
         if (ConfigDebug.ASSERTS) {
            assert offset >= hstart && offset <= hend;
         }
         return this.heap.readInteger(offset + MB_NEXT_POS);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("getNext", ex);
      }
   }

   /**
    * @return length of a memory block
    */
   private final int getLength(int offset) {
      if (ConfigDebug.ASSERTS) {
         assert offset >= hstart && offset <= hend;
      }
      return getNext(offset) - offset - MBHEAD_LEN;
   }

   /**
    * @return dumps the heap content, used for debugging purposes.
    */
   public String dump(boolean verbose) {
      StringBuffer buf = new StringBuffer();

      int start = hstart;

      if (verbose)
    	  buf.append("start/length next/previous status\n");
      else 
    	  buf.append("start/length status of the first block\n");
      
      if (verbose) while (start != 0 && start < hend) {
         int status = getState(start);
         int length = getLength(start);
         int next = getNext(start);
         int previous = getPrevious(start);

         if (ConfigDebug.ASSERTS)
            assert previous < start && (start < next || next == 0) : "the heap seems to be broken! "
                     + previous + " " + start + " " + next;

         buf.append(start + "/" + length + " " + next + "/" + previous + " " + status + "\n");

         start = getNext(start);
      } else 
    	 buf.append(start + "/" + getLength(start) + " " + getState(start) + "\n");

      buf.append("Memory usage (free/used/whole): " + free + "/" + (memory - free) + "/" + memory + "\n");

      return buf.toString();
   }

   /**
    * Creates a <code>List</code> of block descriptions.
    * 
    * @return block list
    */
   public List<Object[]> listBlocks() {
      ArrayList<Object[]> list = new ArrayList<Object[]>();

      int start = hstart;
      while (start != 0 && start < hend) {
         int status = getState(start);
         int length = getLength(start);
         int next = getNext(start);
         int previous = getPrevious(start);

         if (ConfigDebug.ASSERTS)
            assert previous < start && (start < next || next == 0) : "the heap seems to be broken! "
                     + previous + " " + start + " " + next;

         boolean free = status == FREE;
         list.add(new Object[] { start, length, MBHEAD_LEN, free });

         start = getNext(start);
      }

      return list;
   }

   /**
    * Returns a total memory size.
    * 
    * @return total memory
    */
   public int getTotalMemory() {
      return memory;
   }

   /**
    * Returns a free memory size.
    * 
    * @return free memory
    */
   public int getFreeMemory() {
      return free;
   }

   /**
    * Returns an used memory size.
    * 
    * @return used memory
    */
   public int getUsedMemory() {
      return memory - free;
   }

   	@Override
   	public Vector<Integer> getObjectsInSequence() throws DatabaseException {
   		Vector<Integer> objs = new Vector<Integer>();

   		int offset = hstart;
        while (offset != 0 && offset < hend) {

           if (getState(offset) == OCCUPIED) 
        	   objs.add(offset + MBHEAD_LEN);
           else {
        	   assert (getPrevious(offset) == 0 ? OCCUPIED : getState(getPrevious(offset))) == OCCUPIED : 
        		   "the heap seems to be invalid (neighbouring blocks " + getPrevious(offset) + " and " + offset +  " are free)! ";
           }  
        	   
           offset = getNext(offset);
        }   		
   		
   		return objs;
   	}
   
   
   // format of the memory block: state:1|previous:4|next:4|data:n

   // positions of various components of the memory block header
   private final static int MB_STATE_POS = 0;

   private final static int MB_PREVIOUS_POS = 1;

   private final static int MB_NEXT_POS = 5;

   private final static int MB_DATA_POS = 9;

   // state of allocation: 0 (free), 1 (occupied)
   private final static byte FREE = 0;

   private final static byte OCCUPIED = 1;

   // length of the memory block header
   private final static int MBHEAD_LEN = MB_NEXT_POS + Sizes.INTVAL_LEN;

   // length of minimal allocated block
   private final static int MBMINIMAL_LEN = 4;

}