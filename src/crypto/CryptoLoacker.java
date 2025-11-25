package crypto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CryptoLoacker extends JFrame {
    private JTextField cartellaField, ivField;
    private JComboBox<String> algoritmoCombo;
    private JPasswordField chiaveField;
    private JButton selezioneBtn, cifraBtn, decifraBtn;
    
    public CryptoLoacker() {
        super("Applicazione");
        creaInterfaccia();
    }
    
    private void creaInterfaccia() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        
       
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        //cartella
        panel.add(new JLabel("Cartella:"));
        cartellaField = new JTextField();
        cartellaField.setEditable(false);
        panel.add(cartellaField);
        
        //pulsante selezione
        panel.add(new JLabel(""));
        selezioneBtn = new JButton("Scegli Cartella");
        selezioneBtn.addActionListener(e -> scegliCartella());
        panel.add(selezioneBtn);
        
        //algoritmo
        panel.add(new JLabel("Algoritmo:"));
        String[] algoritmi = {"AES-256-ECB", "AES-256-CBC", "DES-ECB", "DES-CBC"};
        algoritmoCombo = new JComboBox<>(algoritmi);
        algoritmoCombo.addActionListener(e -> controllaIV());
        panel.add(algoritmoCombo);
        
        //chiave
        panel.add(new JLabel("Chiave:"));
        chiaveField = new JPasswordField();
        panel.add(chiaveField);
        
        // IV
        panel.add(new JLabel("IV (solo CBC):"));
        ivField = new JTextField();
        panel.add(ivField);
        
        //Pulsanti 
        JPanel bottoniPanel = new JPanel(new FlowLayout());
        cifraBtn = new JButton("CIFRA");
        cifraBtn.addActionListener(e -> eseguiCifratura(true));
        
        decifraBtn = new JButton("DECIFRA");
        decifraBtn.addActionListener(e -> eseguiCifratura(false));
        
        bottoniPanel.add(cifraBtn);
        bottoniPanel.add(decifraBtn);
        
              setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(bottoniPanel, BorderLayout.SOUTH);
        
        controllaIV();
    }
    
    private void scegliCartella() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            cartellaField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void controllaIV() {
        String algo = (String)algoritmoCombo.getSelectedItem();
        boolean mostraIV = algo != null && algo.contains("CBC");
        ivField.setEnabled(mostraIV);
        if (!mostraIV) {
            ivField.setText("");
        }
    }
    
    private void eseguiCifratura(boolean cifra) {
        String cartella = cartellaField.getText();
        String algoritmo = (String)algoritmoCombo.getSelectedItem();
        String chiave = new String(chiaveField.getPassword());
        String iv = ivField.getText();
        
        if (cartella.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Scegli una cartella");
            return;
        }
        if (chiave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci una chiave");
            return;
        }
        if (algoritmo.contains("CBC") && iv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci l'IV per la modalità CBC");
            return;
        }
        
        try {
            FileCipher cipher = new FileCipher();
            int fileProcessati;
            
            if (cifra) {
                fileProcessati = cipher.cifraCartella(cartella, algoritmo, chiave, iv);
                JOptionPane.showMessageDialog(this, 
                    "Cifratura completata\nFile processati: " + fileProcessati +
                    "\nRecovery salvato in: recovery.txt");
            } else {
                fileProcessati = cipher.decifraCartella(cartella, algoritmo, chiave, iv);
                JOptionPane.showMessageDialog(this, 
                    "Decifratura completata\nFile processati: " + fileProcessati);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new CryptoLoacker().setVisible(true);
    });
}
}