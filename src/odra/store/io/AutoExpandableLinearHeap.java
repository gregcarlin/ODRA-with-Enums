package odra.store.io;

public class AutoExpandableLinearHeap extends AutoExpandableHeap {

	private AutoExpandableLinearHeap(int size) {
		super(size);
	}
	
	public static AutoExpandableLinearHeap initializeTransientHeap(int size) {
		return new AutoExpandableLinearHeap(size);
	}
	
	private AutoExpandableLinearHeap(String path_prefix) {
		super(path_prefix);
	}

	public static AutoExpandableLinearHeap startPersistantHeap(String path_prefix) {
		return new AutoExpandableLinearHeap(path_prefix);
	}
	
	protected final int calculateStartOffset(int heap_num) {
		return heap_num * init_size;
	}

	public final int findHeapNum(int offset) {
		return offset / init_size;
	}

	protected final int getHeapSize(int heap_num) {
		return init_size;
	}

}
