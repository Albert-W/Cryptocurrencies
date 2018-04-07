// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    //private UTXOPool utxoPool;
    //UTXOPool mainUTXOPool;
    private static TransactionPool transactionPool;
    private BlockNode MaxHeightNode;


    class BlockNode {
        Block current;
        BlockNode parent;
        int height;
        //every node has his own pool;
        private UTXOPool ownPool;

        //cannot add the first coin to the UTXOpool, so discard it.
//        BlockNode(Block g){
//            current = g;
//            parent = null;
//            height = 1;
//            ownPool = new UTXOPool(); //for getOwnPool() function, it has to be initialized.
//        }

        BlockNode(Block c,BlockNode p, UTXOPool utxoPool){
            current = c;
            parent = p;
            if(p!=null)
                height=p.height+1;
            else height=1;
            //every node store a snapshot of utxopool;
            ownPool = utxoPool;
        }

        public UTXOPool getOwnPool() {
            return new UTXOPool(ownPool);
        }
    }

    //ArrayList<BlockNode> blockChain;

    //**it's important to get the parent node through the preBlockHash;
    private HashMap<ByteArrayWrapper, BlockNode> hashblockChain;
    //ByteArrayWrapper wrapper = new ByteArrayWrapper();

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        UTXOPool utxoPool = new UTXOPool();
        addCoinbaseToUTXOPool(genesisBlock,utxoPool);
        BlockNode c = new BlockNode(genesisBlock,null,utxoPool);
        //blockChain = new ArrayList<>();
        //blockChain.add(c);
        //utxoPool = new UTXOPool();
        transactionPool = new TransactionPool();

        MaxHeightNode = c;
        //mainUTXOPool = utxoPool;
        //2nd version
        hashblockChain = new HashMap<>();
        hashblockChain.put(new ByteArrayWrapper(genesisBlock.getHash()),c);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return MaxHeightNode.current;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return MaxHeightNode.getOwnPool();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        //to get the parent node;
        byte[] parentblockhash = block.getPrevBlockHash();
        if(parentblockhash == null)
            return false;
        BlockNode parentBlockNode = hashblockChain.get(new ByteArrayWrapper(parentblockhash));
        if(parentBlockNode==null)
            return false;
        //to get the parent node;
        if(MaxHeightNode.height - parentBlockNode.height > CUT_OFF_AGE)
            return false;


        TxHandler txHandler = new TxHandler(parentBlockNode.getOwnPool());
        //a different way to get transactions;
//        Transaction[] transactions = block.getTransactions().toArray(new Transaction[0]);
//        Transaction[] validTxs = txHandler.handleTxs(transactions);
//        if(validTxs.length != transactions.length)
//            return false;

        //Process a block with many valid transactions ==> FAILED
        Transaction[] transactions =new Transaction[block.getTransactions().size()];
        for(int i=0;i<block.getTransactions().size();i++){
            //if(!txHandler.isValidTx(block.getTransaction(i)))
            //    return false;
            transactions[i]=(Transaction)block.getTransaction(i);
        }
        Transaction[] validTxs = txHandler.handleTxs(transactions);
        if(validTxs.length != transactions.length)
            return false;

        //first add the coin, the create the BlockNode
        //the pool of different node is different, because they accepted different transactions.
        //what UTXOPoll should be here?
        //you want to change the pool so new need to create one. or you will not make the add affect.
        UTXOPool utxoPool = txHandler.getUTXOPool();
        addCoinbaseToUTXOPool(block,utxoPool);
        BlockNode blockNode = new BlockNode(block,parentBlockNode,utxoPool);
        //blockChain.add(blockNode);
        hashblockChain.put(new ByteArrayWrapper(block.getHash()),blockNode);
        if(parentBlockNode.height >= MaxHeightNode.height){
            MaxHeightNode = blockNode;
        }
        return true;

    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
    }

    private void addCoinbaseToUTXOPool(Block block, UTXOPool utxoPool){
        Transaction transaction = block.getCoinbase();
        for(int i=0;i<transaction.numOutputs();i++){
            Transaction.Output out = transaction.getOutput(i);
            UTXO utxo = new UTXO(transaction.getHash(),i);
            utxoPool.addUTXO(utxo,out);
        }

    }
}