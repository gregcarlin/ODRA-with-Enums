package odra.util;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import odra.db.DatabaseException;
import odra.db.DatabaseRuntimeException;
import odra.store.io.IHeap;
import odra.store.memorymanagement.RevSeqFitMemManager;
import odra.store.persistence.DataFileHeap;

/**
 * Data block sorter utility class.
 * 
 * @author jacenty
 * @version 2007-01-04
 * @since 2007-01-02
 * 
 * edek: modifications related to transactions, encapsulating access to byte
 * buffer
 */
public class HeapSorter {
   public static final byte FILE_HEADER = 1;

   public static final byte DB_ROOT = 2;

   public static final byte FREE_HEADER = 3;

   public static final byte FREE_DATA = 4;

   public static final byte OCCUPIED_HEADER = 5;

   public static final byte OCCUPIED_DATA = 6;

   /** byte buffer */
   private final IHeap heap;

   /** sorted blocks */
   private final Vector<Object[]> sorted;

   /** byte count */
   private final int byteCount;

   /** start offset */
   private final int startOffset;

   /** current list index */
   private int index = 0;

   /**
    * The constructor.
    * 
    * @param allocator
    *           <code>RevSeqFitMemManager</code>
    * @throws DatabaseException
    */
   public HeapSorter(RevSeqFitMemManager allocator) throws DatabaseException {
      index = 0;

      this.heap = allocator.getHeap();
      byteCount = heap.getSize();
      startOffset = heap.getStartOffset();

      boolean fileInUse = false;
      if (this.heap instanceof DataFileHeap) {
         fileInUse = ((DataFileHeap) heap).isMapped();
         if (!fileInUse)
            ((DataFileHeap) this.heap).open();
      }

      List<Object[]> blocks = allocator.listBlocks();

      if (this.heap instanceof DataFileHeap) {
         if (!fileInUse)
            ((DataFileHeap) this.heap).close();
      }

      int[] starts = new int[blocks.size()];
      for (int i = 0; i < blocks.size(); i++)
         starts[i] = (Integer) blocks.get(i)[0];

      Arrays.sort(starts);

      sorted = new Vector<Object[]>(blocks.size());
      for (int i = 0; i < starts.length; i++) {
         for (int j = 0; j < blocks.size(); j++) {
            if ((Integer) blocks.get(i)[0] == starts[i]) {
               sorted.addElement(blocks.get(i));
               break;
            }
         }
      }
   }

   /**
    * Moves to a next block.
    */
   void next() {
      index++;
   }

   /**
    * Moves to a previous block.
    */
   void previous() {
      index--;
   }

   /**
    * Returns the current block start.
    * 
    * @return block start
    */
   int getStart() {
      return (Integer) sorted.get(index)[0];
   }

   /**
    * Returns the current block end.
    * 
    * @return block end
    */
   int getEnd() {
      return (Integer) sorted.get(index)[0] + (Integer) sorted.get(index)[1] + (Integer) sorted.get(index)[2];
   }

   /**
    * Returns if the current block is free.
    * 
    * @return free?
    */
   boolean isFree() {
      return (Boolean) sorted.get(index)[3];
   }

   /**
    * Setups the xorter index for the byte index.
    * 
    * @param byteIndex
    *           current byte index
    */
   public void setup(int byteIndex) {
      if (isInCurrent(byteIndex))
         return;
      else if (byteIndex > getEnd()) {
         do {
            next();
         } while (!isInCurrent(byteIndex));
      } else if (byteIndex < getStart()) {
         do {
            previous();
         } while (!isInCurrent(byteIndex));
      }
   }

   /**
    * Returns if a byte at the index is in the current block.
    * 
    * @param byteIndex
    *           byte index
    * @return in current block?
    */
   boolean isInCurrent(int byteIndex) {
      return byteIndex >= getStart() && byteIndex <= getEnd();
   }

   /**
    * Return is a byte at the index is a header byte.
    * 
    * @param byteIndex
    *           current byte index
    * @return is header?
    */
   boolean isHeader(int byteIndex) {
      return byteIndex - (Integer) sorted.get(index)[0] <= (Integer) sorted.get(index)[2];
   }

   /**
    * Returns a block at the index specified. <br />
    * The method does not influence the current internal block index.
    * 
    * @param index
    *           block index
    * @return block
    */
   Object[] getBlockAt(int index) {
      return sorted.get(index);
   }

   /**
    * Returns a byte at the specified index. <br />
    * The method is independent of the current block index, and
    * <code>byteIndex</code> is regarded absolute.
    * 
    * @param byteIndex
    *           byte index
    * @return byte
    */
   byte getByteAt(int byteIndex) {
      try {
         return this.heap.readByte(byteIndex);
      } catch (DatabaseException ex) {
         throw new DatabaseRuntimeException("getByteAt: " + byteIndex, ex);
      }
   }

   /**
    * Returns the total byte count.
    * 
    * @return byte count
    */
   public int getByteCount() {
      return byteCount;
   }

   /**
    * Returns the start offset.
    * 
    * @return start offset
    */
   int getStartOffset() {
      return startOffset;
   }

   /**
    * Returns a fragment of the specified length of heap starting at the index
    * given.
    * 
    * @param startByteIndex
    *           start byte index
    * @param length
    *           length
    * @return
    */
   public byte[] getFragment(int startByteIndex, int length) {
      byte[] fragment = new byte[length];
      for (int i = 0; i < length; i++)
         fragment[i] = getByteAt(startByteIndex + i);

      return fragment;
   }

   /**
    * Returns types for a fragment of the specified length of heap starting at
    * the index given.
    * 
    * @param startByteIndex
    *           start byte index
    * @param length
    *           length
    * @return
    */
   public byte[] getFragmentTypes(int startByteIndex, int length) {
      byte[] fragment = new byte[length];
      for (int i = startByteIndex; i < startByteIndex + length; i++) {
         byte type;

         if (i < getStartOffset())
            type = FILE_HEADER;
         else if (i <= (Integer) getBlockAt(0)[0])
            type = DB_ROOT;
         else {
            if (!isInCurrent(i))
               setup(i);

            if (isFree()) {
               if (isHeader(i))
                  type = FREE_HEADER;
               else
                  type = FREE_DATA;
            } else {
               if (isHeader(i))
                  type = OCCUPIED_HEADER;
               else
                  type = OCCUPIED_DATA;
            }
         }

         fragment[i - startByteIndex] = type;
      }

      return fragment;
   }
}
