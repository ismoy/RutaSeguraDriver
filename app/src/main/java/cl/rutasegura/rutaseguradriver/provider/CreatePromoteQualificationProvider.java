package cl.rutasegura.rutaseguradriver.provider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cl.rutasegura.rutaseguradriver.Model.CreatePromoteQualification;

/**
 * Created by ISMOY BELIZAIRE on 22/06/2022.
 */
public class CreatePromoteQualificationProvider {
    DatabaseReference mDatabase;

    public CreatePromoteQualificationProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Calification");
    }


    public Task<Void> create(CreatePromoteQualification resumeTrip) {
        return mDatabase.child(resumeTrip.getIdDriver()).setValue(resumeTrip);
    }

}
