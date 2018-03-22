
import java.util.*;

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
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS

        double outsum =0,insum = 0;

        Set<UTXO> set= new HashSet<UTXO>();

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input= tx.getInput(i);

            UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);

            //(1) all outputs claimed by {@code tx} are in the current UTXO pool,
            if(!utxoPool_handler.contains(utxo)){
                return false;
            } else {
                set.add(utxo);
            }
            //(2) the signatures on each input of {@code tx} are valid,
            Transaction.Output prevOutput = utxoPool_handler.getTxOutput(utxo);
            //if(!Crypto.verifySignature(prevOutput.address,tx.getHash(),input.signature))
            //have to use getRawDataToSign();
            if(!Crypto.verifySignature(prevOutput.address,tx.getRawDataToSign(i),input.signature))
            {
                return false;
            };
            //the sum of {@code tx}s input values
            insum+=prevOutput.value;

        }
        //(3) no UTXO is claimed multiple times by {@code tx},
        if(set.size()<tx.numInputs()) {
            return false;
        }
        //all of output values
        for(int i=0;i<tx.numOutputs();i++) {
            if(tx.getOutput(i).value<0){
                return false;
            } else {
                outsum+=tx.getOutput(i).value;
            }
        }
        //(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        if(insum<outsum){
            return false;
        }
        return true;

    }



    /**
     *
     * @param tx it's a transaction.
     * @return the fee of the transaction, ie, the total input- the total output.
     */
    public double TransactionFeeRate (Transaction tx){
        double insum=0, outsum=0, transactionfee=-1;

        if(isValidTx(tx)){
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
            //return transactionfee/insum;
            return transactionfee/outsum;

        }
        return 0;
    }

    /**
     *
     * @param possibleTxs it's a set of transaction.
     * @return the transaction who has maximum total transaction fees.
     */

    public Transaction[] handleTxs(Transaction[] possibleTxs){
        Set<Transaction> txsSortedByFeesRate = new TreeSet<>((tx1, tx2) -> {
            double tx1Fees = TransactionFeeRate(tx1);
            double tx2Fees = TransactionFeeRate(tx2);
            return Double.valueOf(tx2Fees).compareTo(tx1Fees);
        });

        Collections.addAll(txsSortedByFeesRate, possibleTxs);
        //Transaction []transaction = new Transaction[1];
        Set<Transaction> acceptedTxs = new HashSet<>();
        for (Transaction tx : txsSortedByFeesRate) {
            if (isValidTx(tx)) {
                acceptedTxs.add(tx);
                for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    utxoPool_handler.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    utxoPool_handler.addUTXO(utxo, out);
                }
            }
        }
        Transaction[] validTxArray = new Transaction[acceptedTxs.size()];
        return acceptedTxs.toArray(validTxArray);

    }

}
