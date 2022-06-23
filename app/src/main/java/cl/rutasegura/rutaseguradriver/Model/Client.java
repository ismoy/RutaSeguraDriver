package cl.rutasegura.rutaseguradriver.Model;

public class Client {

    String idClient;
    String firstname;
    String lastname;
    String email;
    public Client() {
    }

    public Client(String idClient, String firstname, String lastname, String email) {
        this.idClient = idClient;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
