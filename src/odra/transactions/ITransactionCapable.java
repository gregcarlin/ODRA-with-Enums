package odra.transactions;

public interface ITransactionCapable {

	boolean isTransactionCapable();

	ITransactionCapabilities getTransactionCapabilities();
}