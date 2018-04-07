import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    private UTXOPool utxoPool_handler;

    public UTXOPool getUTXOPool(){
        return utxoPool_handler;
    }
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
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
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Transaction[] transactions =new Transaction[possibleTxs.length];
        //l the index of new set of transaction.
        int l=0;
        for(int i=0;i<possibleTxs.length;i++){
            if(isValidTx(possibleTxs[i])){
                for(int j=0;j<possibleTxs[i].numInputs();j++){
                    UTXO utxo = new UTXO(possibleTxs[i].getInput(j).prevTxHash,possibleTxs[i].getInput(j).outputIndex);
                    utxoPool_handler.removeUTXO(utxo);
                }
                for(int k=0;k<possibleTxs[i].numOutputs();k++){
                    UTXO newUtxo = new UTXO(possibleTxs[i].getHash(),k);
                    utxoPool_handler.addUTXO(newUtxo,possibleTxs[i].getOutput(k));
                }
                transactions[l++]=possibleTxs[i];
            }
        }
        return transactions;

    }

}
