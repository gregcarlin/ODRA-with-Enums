package odra.transactions.store;

import odra.store.io.IHeap;

public interface ITransactionCapableHeap extends IHeap, IHeapTransactionExtension {
}