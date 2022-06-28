package cl.rutasegura.rutaseguradriver.activities.register;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import cl.rutasegura.rutaseguradriver.Model.Driver;
import cl.rutasegura.rutaseguradriver.R;
import cl.rutasegura.rutaseguradriver.activities.login.LoginActivity;
import cl.rutasegura.rutaseguradriver.databinding.ActivityRegisterBinding;
import cl.rutasegura.rutaseguradriver.provider.AuthProvider;
import cl.rutasegura.rutaseguradriver.provider.DriverProvider;
import cl.rutasegura.rutaseguradriver.utils.ValidateGeneral;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private ProgressDialog mProgressDialog;
    private AuthProvider mAuthProvider;
    private ValidateGeneral mValidateGeneral;
    private  FirebaseAuth fAuth;
    private DriverProvider mDriverProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mProgressDialog = new ProgressDialog(this);
        mAuthProvider = new AuthProvider();
        mValidateGeneral = new ValidateGeneral();
        mDriverProvider = new DriverProvider();
        binding.btnRegister.setOnClickListener(view -> {
            validateOnclickButton();
        });
        validateRealTime();
        fAuth = FirebaseAuth.getInstance();
    }

    private void validateOnclickButton() {
        if (binding.firstname.getText().toString().isEmpty()) {
            binding.layoutfirstname.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        }else if (!mValidateGeneral.validarletras(binding.firstname.getText().toString())){
            binding.layoutfirstname.setHelperText(getString(R.string.only_letter));
        }else  if (binding.lastname.getText().toString().isEmpty()) {
            binding.layoutlastname.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        } else if (!mValidateGeneral.validarletras(binding.lastname.getText().toString())){
            binding.layoutlastname.setHelperText(getString(R.string.only_letter));
        }else if (binding.emaillogin.getText().toString().isEmpty()){
            binding.layoutemail.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        }else if (!mValidateGeneral.validaremail(binding.emaillogin.getText().toString())){
            binding.layoutemail.setHelperText(getString(R.string.invalid_email));
            binding.btnRegister.setEnabled(false);
        }else if (binding.password.getText().toString().isEmpty()){
            binding.layoutpassword.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        }else if (binding.password.getText().toString().length()<6){
            binding.layoutpassword.setHelperText(getString(R.string.short_passord));
            binding.btnRegister.setEnabled(false);
        }else  if (binding.confirmpassword.getText().toString().isEmpty()){
            binding.layoutconfirmpassword.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        }else if (binding.confirmpassword.getText().toString().length()<6){
            binding.layoutconfirmpassword.setHelperText(getString(R.string.short_passord));
        }else if (!binding.confirmpassword.getText().toString().equals(binding.password.getText().toString())){
            binding.layoutconfirmpassword.setHelperText(getString(R.string.no_match_password));
        }else if (binding.brand.getText().toString().isEmpty()){
            binding.layoutbrand.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        }else if (!mValidateGeneral.validarletras(binding.brand.getText().toString())){
            binding.layoutbrand.setHelperText(getString(R.string.only_letter));
            binding.btnRegister.setEnabled(false);
        }else if (binding.licenceplate.getText().toString().isEmpty()){
            binding.layoutlicense.setHelperText(getString(R.string.error_empty));
            binding.btnRegister.setEnabled(false);
        }else {
            binding.btnRegister.setEnabled(true);
            binding.layoutfirstname.setHelperText("");
            binding.layoutlastname.setHelperText("");
            binding.layoutemail.setHelperText("");
            binding.layoutpassword.setHelperText("");
            binding.layoutconfirmpassword.setHelperText("");
            binding.layoutbrand.setHelperText("");
            binding.layoutlicense.setHelperText("");
            clickRegister(binding.firstname.getText().toString(),binding.lastname.getText().toString(),
                    binding.emaillogin.getText().toString(),binding.password.getText().toString(),
                    binding.confirmpassword.getText().toString(),binding.brand.getText().toString(),
                    binding.licenceplate.getText().toString());
        }
    }

    private void validateRealTime() {
        binding.firstname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.firstname.getText().toString().isEmpty()) {
                    binding.layoutfirstname.setHelperText(getString(R.string.error_empty));
                    binding.btnRegister.setEnabled(false);
                }else if (!mValidateGeneral.validarletras(binding.firstname.getText().toString())){
                    binding.layoutfirstname.setHelperText(getString(R.string.only_letter));
                }
                else {
                    binding.btnRegister.setEnabled(true);
                    binding.layoutfirstname.setHelperText("");

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.lastname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.lastname.getText().toString().isEmpty()) {
                    binding.layoutlastname.setHelperText(getString(R.string.error_empty));
                    binding.btnRegister.setEnabled(false);
                } else if (!mValidateGeneral.validarletras(binding.lastname.getText().toString())){
                    binding.layoutlastname.setHelperText(getString(R.string.only_letter));
                }
                else {
                    binding.btnRegister.setEnabled(true);
                    binding.layoutlastname.setHelperText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.emaillogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
              if (binding.emaillogin.getText().toString().isEmpty()){
                  binding.layoutemail.setHelperText(getString(R.string.error_empty));
                  binding.btnRegister.setEnabled(false);
              }else if (!mValidateGeneral.validaremail(binding.emaillogin.getText().toString())){
                  binding.layoutemail.setHelperText(getString(R.string.invalid_email));
                  binding.btnRegister.setEnabled(false);
              }
              else {
                  binding.layoutemail.setHelperText("");
                  binding.btnRegister.setEnabled(true);
              }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.password.getText().toString().isEmpty()){
                    binding.layoutpassword.setHelperText(getString(R.string.error_empty));
                    binding.btnRegister.setEnabled(false);
                }else if (binding.password.getText().toString().length()<6){
                    binding.layoutpassword.setHelperText(getString(R.string.short_passord));
                    binding.btnRegister.setEnabled(false);
                }
                else {
                    binding.layoutpassword.setHelperText("");
                    binding.btnRegister.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.confirmpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.confirmpassword.getText().toString().isEmpty()){
                    binding.layoutconfirmpassword.setHelperText(getString(R.string.error_empty));
                    binding.btnRegister.setEnabled(false);
                }else if (binding.confirmpassword.getText().toString().length()<6){
                    binding.layoutconfirmpassword.setHelperText(getString(R.string.short_passord));
                }else if (!binding.confirmpassword.getText().toString().equals(binding.password.getText().toString())){
                    binding.layoutconfirmpassword.setHelperText(getString(R.string.no_match_password));
                }else {
                    binding.layoutconfirmpassword.setHelperText("");
                    binding.btnRegister.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.brand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
              if (binding.brand.getText().toString().isEmpty()){
                  binding.layoutbrand.setHelperText(getString(R.string.error_empty));
                  binding.btnRegister.setEnabled(false);
              }else if (!mValidateGeneral.validarletras(binding.brand.getText().toString())){
                  binding.layoutbrand.setHelperText(getString(R.string.only_letter));
                  binding.btnRegister.setEnabled(false);
              }else {
                  binding.layoutbrand.setHelperText("");
                  binding.btnRegister.setEnabled(true);
              }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.licenceplate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             if (binding.licenceplate.getText().toString().isEmpty()){
                 binding.layoutlicense.setHelperText(getString(R.string.error_empty));
                 binding.btnRegister.setEnabled(false);
             }else {
                 binding.layoutlicense.setHelperText("");
                 binding.btnRegister.setEnabled(true);
             }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //method para registrar
    private void clickRegister(String firstname, String lastname, String email, String password, String confirmpassword, String vehicleBrand, String licenseplate) {
        mProgressDialog.setMessage(getString(R.string.Loading___));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        registers(firstname,lastname,email,password,confirmpassword,vehicleBrand,licenseplate);
     }

    private void registers(String firstname, String lastname, String email, String password, String confirmpassword, String vehicleBrand, String licenseplate) {
    mAuthProvider.register(email, password).addOnCompleteListener(task -> {
        mProgressDialog.dismiss();
        if (task.isSuccessful()) {
            String id =(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())).getUid();
            Driver driver = new Driver(id,firstname,lastname,email,password,confirmpassword,vehicleBrand,licenseplate,"",2);
            RegisterDriver(driver);
            FirebaseUser user = fAuth.getCurrentUser();
            fAuth.setLanguageCode("es");
            assert user!=null;
            user.sendEmailVerification().addOnCompleteListener(task1 -> Toast.makeText(this, getString(R.string.Youraccounthasbeencreatedsuccessfully), Toast.LENGTH_LONG).show()).addOnFailureListener(e -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }else {
            Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
    }

    private void RegisterDriver(Driver driver) {
        mDriverProvider.create(driver).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                mAuthProvider.logout();
            }else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}