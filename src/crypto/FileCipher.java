package crypto;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;

public class FileCipher {
    
    public int cifraCartella(String percorso, String algoritmo, String chiave, String iv) throws Exception {
        File cartella = new File(percorso);
        File[] files = cartella.listFiles();
        int count = 0;
        
        if (files == null) return 0;
        
        for (File file : files) {
            if (file.isFile() && !file.getName().equals("recovery.txt")) {
                cifraFile(file, algoritmo, chiave, iv);
                count++;
            }
        }
        
        salvaRecovery(cartella, algoritmo, chiave, iv);
        return count;
    }
    
    public int decifraCartella(String percorso, String algoritmo, String chiave, String iv) throws Exception {
        File cartella = new File(percorso);
        File[] files = cartella.listFiles();
        int count = 0;
        
        if (files == null) return 0;
        
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".loacker")) {
                decifraFile(file, algoritmo, chiave, iv);
                count++;
            }
        }
        return count;
    }
    
    private void cifraFile(File file, String algoritmo, String chiave, String iv) throws Exception {
        Cipher cipher = preparaCipher(Cipher.ENCRYPT_MODE, algoritmo, chiave, iv);
        
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        byte[] fileCifrato = cipher.doFinal(fileBytes);
        
        //salva
        File fileOutput = new File(file.getAbsolutePath() + ".loacker");
        Files.write(fileOutput.toPath(), fileCifrato);
        
        //elimina originale
        Files.delete(file.toPath());
    }
    
    private void decifraFile(File file, String algoritmo, String chiave, String iv) throws Exception {
        Cipher cipher = preparaCipher(Cipher.DECRYPT_MODE, algoritmo, chiave, iv);
        
        //leggi
        byte[] fileCifrato = Files.readAllBytes(file.toPath());
        
        //decifra
        byte[] fileDecifrato = cipher.doFinal(fileCifrato);
        
        //salva
        String nomeOriginale = file.getAbsolutePath().replace(".loacker", "");
        Files.write(Paths.get(nomeOriginale), fileDecifrato);
        
        Files.delete(file.toPath());
    }
    
    private Cipher preparaCipher(int mode, String algoritmo, String chiave, String iv) throws Exception {
        
        String[] parti = algoritmo.split("-");
        String nomeAlgo = parti[0];  // AES  DES
        String modalita = parti[2];  // ECB  CBC

        
        byte[] chiaveBytes = chiave.getBytes("UTF-8");
        int dimChiave = nomeAlgo.equals("AES") ? 32 : 8; 
        
       
        byte[] chiaveAdattata = new byte[dimChiave];
        System.arraycopy(chiaveBytes, 0, chiaveAdattata, 0, Math.min(chiaveBytes.length, dimChiave));
        
        SecretKeySpec keySpec = new SecretKeySpec(chiaveAdattata, nomeAlgo);
        Cipher cipher;
        
        if (modalita.equals("CBC")) {
            
            byte[] ivBytes = iv.getBytes("UTF-8");
            int dimIV = nomeAlgo.equals("AES") ? 16 : 8; 
            byte[] ivAdattato = new byte[dimIV];
            System.arraycopy(ivBytes, 0, ivAdattato, 0, Math.min(ivBytes.length, dimIV));
            
            IvParameterSpec ivSpec = new IvParameterSpec(ivAdattato);
            cipher = Cipher.getInstance(nomeAlgo + "/CBC/PKCS5Padding");
            cipher.init(mode, keySpec, ivSpec);
        } else {
            
            cipher = Cipher.getInstance(nomeAlgo + "/ECB/PKCS5Padding");
            cipher.init(mode, keySpec);
        }
        
        return cipher;
    }
    
    private void salvaRecovery(File cartella, String algoritmo, String chiave, String iv) {
        File recovery = new File(cartella, "recovery.txt");
        try (PrintWriter writer = new PrintWriter(recovery)) {
            writer.println("=== RECUPERO CRYPTO LOACKER ===");
            writer.println("Algoritmo: " + algoritmo);
            writer.println("Chiave: " + chiave);
            if (iv != null && !iv.isEmpty()) {
                writer.println("IV: " + iv);
            }
            writer.println("Salvataggio: " + new java.util.Date());
            writer.println("===============================");
        } catch (IOException e) {
            System.out.println("Attenzione: non ho potuto salvare il file di recovery");
        }
    }
}