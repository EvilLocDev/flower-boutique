package com.example.flowerboutique.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityModifyProfileBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ModifyProfileActivity extends AppCompatActivity {

    private ActivityModifyProfileBinding binding;
    private final AppFirebase appFirebase = new AppFirebase();
    private final FirebaseUser user = appFirebase.getFirebaseAuth().getCurrentUser();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "vn"));
    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()).setTitleText("Chọn ngày sinh").build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModifyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (user == null) finish();

        initProfile();

        datePicker.addOnPositiveButtonClickListener(v -> {
            binding.birthday.setText(dateFormat.format(new Date(v)));
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.birthdaySection.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), null);
        });

        binding.birthday.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), null);
        });

        binding.btnCompleteProfile.setOnClickListener(v -> {
            if (user == null) return;
            String name = binding.edtName.getText() != null ? binding.edtName.getText().toString().trim() : null;
            String phoneNumber = binding.edtPhoneNumber.getText() != null ? binding.edtPhoneNumber.getText().toString().trim() : null;
            String gender = binding.gender.getText() != null ? binding.gender.getText().toString().trim() : null;
            String birthday = binding.birthday.getText() != null ? binding.birthday.getText().toString().trim() : null;
            if (name == null) {
                Toast.makeText(this, "Không để tên trống", Toast.LENGTH_LONG).show();
                return;
            }
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", name);
            updatedData.put("phoneNumber", phoneNumber);
            updatedData.put("gender", gender);
            try {
                updatedData.put("birthday", dateFormat.parse(birthday));
            } catch (ParseException ignored) {

            }
            appFirebase.getUsersCollection().document(user.getUid()).update(updatedData).addOnCompleteListener(task -> {
                Toast.makeText(this, task.isSuccessful() ? "Lưu thành công" : "Lỗi", Toast.LENGTH_LONG).show();
                if (task.isSuccessful())
                    setResult(ModifyProfileActivity.RESULT_OK);
            });
        });
    }

    private void initProfile() {
        appFirebase.getUsersCollection().document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot result = task.getResult();
                Picasso.get().load(result.getString("avatar")).into(binding.ivAvatar);
                binding.edtName.setText(result.getString("name"));
                binding.edtPhoneNumber.setText(result.getString("phoneNumber"));
                binding.gender.setText(result.getString("gender"), false);
                binding.birthday.setText(result.getDate("birthday") == null ? null : dateFormat.format(result.getDate("birthday")));
            }
        });
    }
}