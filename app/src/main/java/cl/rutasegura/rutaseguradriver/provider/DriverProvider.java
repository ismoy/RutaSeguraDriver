package cl.rutasegura.rutaseguradriver.provider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import cl.rutasegura.rutaseguradriver.Model.Driver;

public class DriverProvider {

    DatabaseReference mDatabase;

    public DriverProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
    }

    public Task<Void> create(Driver driver) {
        return mDatabase.child(driver.getIdDriver()).setValue(driver);
    }

    public DatabaseReference getDriver(String idDriver) {
        return mDatabase.child(idDriver);
    }

  /*  public Task<Void> update(Driver driver) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", driver.getName());
        map.put("image", driver.getImage());
        map.put("vehicleBrand", driver.getVehicleBrand());
        map.put("vehiclePlate", driver.getVehiclePlate());
        map.put("password",driver.getPassword());
        map.put("password2",driver.getPassword2());
        return mDatabase.child(driver.getId()).updateChildren(map);
    }
*/
}
