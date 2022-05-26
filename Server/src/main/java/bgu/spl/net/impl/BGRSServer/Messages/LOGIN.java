package bgu.spl.net.impl.BGRSServer.Messages;

public class LOGIN extends MessageIn {

    private String Username;
    private String Password;
    private int numOfZeros;

    public LOGIN() {
        numOfZeros=2;
        Username=null;
        Password=null;
    }
    public int getNumOfZeros() {
        return numOfZeros;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
    @Override
    public short getOpcode() {
        return 3;
    }
}
