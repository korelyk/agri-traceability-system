package com.agritrace.blockchain;

import com.agritrace.crypto.DigitalSignature;
import com.agritrace.entity.Block;
import com.agritrace.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockchainPersistenceTest {

    @Test
    void shouldReloadPersistedChainState() throws Exception {
        Path stateFile = Files.createTempFile("agritrace-chain", ".json");
        Files.deleteIfExists(stateFile);

        Blockchain blockchain = new Blockchain(stateFile.toString());
        blockchain.initialize();
        blockchain.setDifficulty(1);

        Transaction transaction = createSignedTransaction();
        blockchain.addTransaction(transaction);
        Block minedBlock = blockchain.minePendingTransactions("miner-1");

        assertNotNull(minedBlock);
        assertTrue(Files.exists(stateFile));

        Blockchain reloaded = new Blockchain(stateFile.toString());
        reloaded.initialize();

        assertEquals(blockchain.getChain().size(), reloaded.getChain().size());
        assertNotNull(reloaded.findTransaction(transaction.getTransactionId()));
        assertTrue(reloaded.isChainValid());
    }

    private Transaction createSignedTransaction() {
        DigitalSignature digitalSignature = new DigitalSignature();
        Map<String, String> keys = DigitalSignature.KeyPairGenerator.generateKeyPair();

        Transaction transaction = Transaction.createTraceTransaction(
                "P-1001",
                "Organic Tomato",
                "PROCESS",
                "U-1001",
                "Processor Demo",
                "Shandong",
                "Washed and packed");
        transaction.setFromAddress("AT1234567890");
        transaction.setFromPublicKey(keys.get("publicKey"));
        transaction.setDigitalSignature(digitalSignature.signTransaction(transaction, keys.get("privateKey")));
        transaction.setTransactionHash(transaction.calculateHash());
        return transaction;
    }
}
