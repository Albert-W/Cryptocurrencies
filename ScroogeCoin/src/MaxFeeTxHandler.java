

public class MaxFeeTxHandler {

    private  UTXOPool utxoPool_handler;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        utxoPool_handler = new UTXOPool(utxoPool);
    }

    /**
     *
     * @param tx it's a transaction.
     * @return the fee of the transaction, ie, the total input- the total output.
     */
    public double TransactionFee (Transaction tx){
        double insum=0, outsum=0,transactionfee=0;

        TxHandler txHandler = new TxHandler(utxoPool_handler);
        if(txHandler.isValidTx(tx)){
            for(int i=0;i<tx.numInputs();i++){
                Transaction.Input input= tx.getInput(i);

                UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
                Transaction.Output prevOutput = utxoPool_handler.getTxOutput(utxo);
                insum+=prevOutput.value;
            }
            for(int j=0;j<tx.numOutputs();j++){
                outsum+=tx.getOutput(j).value;
            }
            transactionfee = insum-outsum;
            return transactionfee;

        }
        return -1;
    }

    /**
     *
     * @param possibleTxs it's a set of transaction.
     * @return the transaction who has maximum total transaction fees.
     */

    public Transaction handleTxs(Transaction[] possibleTxs){
        double maxfee=0;
        Transaction transaction = null;
        for(int i=0;i<possibleTxs.length;i++){
            if(TransactionFee(possibleTxs[i])>maxfee){
                maxfee=TransactionFee(possibleTxs[i]);
                transaction=possibleTxs[i];
            }
        }
        return transaction;

    }

}
