package cl.rutasegura.rutaseguradriver.Model;

/**
 * Created by ISMOY BELIZAIRE on 08/06/2022.
 */
public class Driver {
    String idDriver;
    String firstname;
    String lastName;
    String email;
    String password;
    String password2;
    String vehicleBrand;
    String vehiclePlate;
    String image;

    public Driver() {
    }

    public Driver(String idDriver, String firstname, String lastName, String email, String password, String password2, String vehicleBrand, String vehiclePlate, String image) {
        this.idDriver = idDriver;
        this.firstname = firstname;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.password2 = password2;
        this.vehicleBrand = vehicleBrand;
        this.vehiclePlate = vehiclePlate;
        this.image = image;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(String idDriver) {
        this.idDriver = idDriver;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
