import algorithm.DES;
import algorithm.RSA;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Scanner;

public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);
    JLabel label = new JLabel("enter message:");
    JButton button = new JButton("Exchange DES secret");
    public int clientIndex = 0;
    public String publicKey;
    public String privateKey;
    public String peerPublicKey;
    public String DESKey;
    public String peerDESKey;

    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
        Box inputArea=Box.createVerticalBox();
        inputArea.add(label);
        inputArea.add(textField);
        inputArea.add(button);

        frame.getContentPane().add(inputArea, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);

        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(e -> {
            String cipherText = null;
            try {
                cipherText = DES.encrypt(textField.getText(), DESKey);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            out.println(cipherText);
            textField.setText("");
        });
        button.addActionListener(e ->{
            byte[] RSAEncrypt = RSA.encrypt(DESKey.getBytes(),peerPublicKey);
            out.println("EXCHANGE "+ Base64.encodeBase64String(RSAEncrypt));
        });
    }

    private String getName() {
        return "client-"+ clientIndex;
    }

    public void run() throws Exception {
        try {
            Socket socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                }
                else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                }
                else if (line.startsWith("ENCRYPTMESSAGE")) {
                    line = line.substring("ENCRYPTMESSAGE".length()+1);
                    System.out.println(getName()+",接收到数据："+line);
                    String[] receivedDatum = line.split(": ");
                    System.out.println(getName()+",接收到加密数据："+receivedDatum[1]);

                    String clientName = receivedDatum[0];
                    String msg;
                    if(getName().equals(clientName)){
                        msg = DES.decrypt(receivedDatum[1], DESKey);
                    }else{
                        msg = peerDESKey!=null?
                         DES.decrypt(receivedDatum[1], peerDESKey):" please exchange secret first!";
                    }
                    messageArea.append(line +"        |   Plaint Text text: "+msg + "\n");
                }
                else if (line.startsWith("EXCHANGE")){
                    String key = line.substring("EXCHANGE".length()+1);
                    messageArea.append(key + "\n");
                    byte[] RSADecrypt = RSA.decrypt(Base64.decodeBase64(key),privateKey);
                    assert RSADecrypt != null;
                    peerDESKey = new String(RSADecrypt, StandardCharsets.UTF_8);
                    System.out.println(getName()+",AESKey:"+ DESKey);

                    byte[] RSAEncrypt = RSA.encrypt(DESKey.getBytes(),peerPublicKey);
                    out.println("ENDEXCHANGE "+ Base64.encodeBase64String(RSAEncrypt));
                    System.out.println(getName()+",peerAESKey:"+ peerDESKey);
                }
                else if (line.startsWith("ENDEXCHANGE")){
                    String key = line.substring("ENDEXCHANGE".length()+1);
                    messageArea.append(key + "\n");
                    byte[] RSADecrypt = RSA.decrypt(Base64.decodeBase64(key),privateKey);
                    assert RSADecrypt != null;
                    peerDESKey = new String(RSADecrypt, StandardCharsets.UTF_8);
                    System.out.println(getName()+",peerAESKey:"+ peerDESKey);
                    System.out.println(getName()+",AESKey:"+ DESKey);
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }




    public static void main(String[] args) throws Exception {
        KeyPair keyPairA = RSA.pairGenerator();
        KeyPair keyPairB = RSA.pairGenerator();

        SecretKey keyA = DES.getSecretEncryptionKey();
        SecretKey keyB = DES.getSecretEncryptionKey();
        Runnable startClientA = () -> {
            ChatClient client = new ChatClient("localhost");
            client.clientIndex = 0;
            client.publicKey = Base64.encodeBase64String(keyPairA.getPublic().getEncoded());
            client.privateKey= Base64.encodeBase64String(keyPairA.getPrivate().getEncoded());
            client.peerPublicKey=Base64.encodeBase64String(keyPairB.getPublic().getEncoded());
            client.DESKey = Base64.encodeBase64String(keyA.getEncoded());

            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setVisible(true);

            try {
                client.run();
            } catch (Exception e) {
                System.out.println("The chat client throw exception");
                e.printStackTrace();
            }
        };
        Runnable startClientB = () -> {
            ChatClient clientB = new ChatClient("localhost");
            clientB.clientIndex = 1;
            clientB.publicKey = Base64.encodeBase64String(keyPairB.getPublic().getEncoded());
            clientB.privateKey= Base64.encodeBase64String(keyPairB.getPrivate().getEncoded());
            clientB.peerPublicKey=Base64.encodeBase64String(keyPairA.getPublic().getEncoded());
            clientB.DESKey = Base64.encodeBase64String(keyB.getEncoded());
            clientB.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            clientB.frame.setVisible(true);
            try {
                clientB.run();
            } catch (Exception e) {
                System.out.println("The chat client throw exception");
                e.printStackTrace();
            }
        };

        new Thread(startClientA).start();
        new Thread(startClientB).start();
    }
}