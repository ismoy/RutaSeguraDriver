package cl.rutasegura.rutaseguradriver.Model;

/**
 * Created by ISMOY BELIZAIRE on 22/06/2022.
 */
public class CreatePromoteQualification {
    String idDriver;
    float calificationClient;

    public CreatePromoteQualification() {
    }

    public CreatePromoteQualification(String idDriver, float calificationClient) {
        this.idDriver = idDriver;
        this.calificationClient = calificationClient;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public void setIdDriver(String idDriver) {
        this.idDriver = idDriver;
    }

    public float getCalificationClient() {
        return calificationClient;
    }

    public void setCalificationClient(float calificationClient) {
        this.calificationClient = calificationClient;
    }
}
