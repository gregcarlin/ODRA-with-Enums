package odra.store.io;

public class AutoExpandablePowerHeap extends AutoExpandableHeap {

	private AutoExpandablePowerHeap(int size) {
		super(size);
	}

	public static AutoExpandablePowerHeap initializeTransientHeap(int size) {
		return new AutoExpandablePowerHeap(size);
	}
	
	private AutoExpandablePowerHeap(String path_prefix) {
		super(path_prefix);
	}

	public static AutoExpandablePowerHeap startPersistantHeap(String path_prefix) {
		return new AutoExpandablePowerHeap(path_prefix);
	}
	
	protected final int calculateStartOffset(int heap_num) {
		return heap_num == 0 ? 0 : getStartOffset(heap_num - 1) + getHeapSize(heap_num - 1);
	}

	public final int findHeapNum(int offset) {
		return (int) (Math.log(((offset / init_size) + 1)) / Math.log(2));
	}

	protected final int getHeapSize(int heap_num) {
		return init_size << heap_num;
	}

}
