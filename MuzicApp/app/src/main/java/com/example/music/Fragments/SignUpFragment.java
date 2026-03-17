package com.example.music.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.music.Activities.MainActivity;
import com.example.music.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import android.app.Activity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.card.MaterialCardView;
import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {

    private EditText userName, email, password, confirmPassword;
    private Button signUpBtn;
    private ProgressBar signUpBar;
    private TextView alreadyHaveAccount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;
    private MaterialCardView googleSignUpButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        userName = view.findViewById(R.id.userName); // ✅ lấy từ EditText layout
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        confirmPassword = view.findViewById(R.id.confirmPassword);
        signUpBtn = view.findViewById(R.id.signUpButton);
        signUpBar = view.findViewById(R.id.signUpBar);
        alreadyHaveAccount = view.findViewById(R.id.already_have_an_account);
        googleSignUpButton = view.findViewById(R.id.googleSignUpButton);
        // khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        alreadyHaveAccount.setOnClickListener(v -> setFragment(new SignInFragment()));

        // check realtime input
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        userName.addTextChangedListener(inputWatcher);
        email.addTextChangedListener(inputWatcher);
        password.addTextChangedListener(inputWatcher);
        confirmPassword.addTextChangedListener(inputWatcher);

        signUpBtn.setOnClickListener(v -> checkEmailAndPassword());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleResult(task);
                    }
                });

        googleSignUpButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });
    }
    private void handleGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Google Sign Up Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(getActivity(),
                                "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    String userId = user.getUid();

                    boolean isNewUser =
                            task.getResult().getAdditionalUserInfo().isNewUser();

                    if (isNewUser) {

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", userId);
                        userMap.put("email", user.getEmail());
                        userMap.put("name", user.getDisplayName());
                        userMap.put("role", "user");   // 🔥 THÊM ROLE

                        // Firestore
                        firebaseFirestore.collection("Users")
                                .document(userId)
                                .set(userMap);

                        // Realtime DB
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .setValue(userMap);
                    }

                    Toast.makeText(getActivity(),
                            "Welcome " + user.getDisplayName(),
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(getActivity(), MainActivity.class));
                    requireActivity().finish();
                });
    }
    private void checkInputs() {
        if (!TextUtils.isEmpty(userName.getText())) {
            if (!TextUtils.isEmpty(email.getText())) {
                if (!TextUtils.isEmpty(password.getText()) && password.length() >= 6) {
                    if (!TextUtils.isEmpty(confirmPassword.getText())) {
                        signUpBtn.setEnabled(true);
                    } else {
                        signUpBtn.setEnabled(false);
                    }
                } else {
                    signUpBtn.setEnabled(false);
                }
            } else {
                signUpBtn.setEnabled(false);
            }
        } else {
            signUpBtn.setEnabled(false);
        }
    }

    private void checkEmailAndPassword() {
        String nameStr = userName.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String passStr = password.getText().toString().trim();
        String confirmStr = confirmPassword.getText().toString().trim();

        if (nameStr.isEmpty()) {
            userName.setError("Vui lòng nhập tên");
            return;
        }

        if (!passStr.equals(confirmStr)) {
            confirmPassword.setError("Mật khẩu nhập lại không khớp");
            return;
        }

        if(emailStr.isEmpty() || passStr.isEmpty() || passStr.length() < 6){
            Toast.makeText(getActivity(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpBar.setVisibility(View.VISIBLE);
        signUpBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String userId = mAuth.getCurrentUser().getUid();

                        // 1️⃣ Lưu vào Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", userId);
                        userMap.put("email", emailStr);
                        userMap.put("password", passStr);
                        userMap.put("name", nameStr); // ✅ dùng tên từ EditText

                        firebaseFirestore.collection("Users").document(userId)
                                .set(userMap)
                                .addOnCompleteListener(userTask -> {
                                    if(userTask.isSuccessful()){

                                        // 2️⃣ Lưu song song vào Realtime Database
                                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(userId);

                                        userRef.setValue(userMap).addOnCompleteListener(rTask -> {
                                            if(rTask.isSuccessful()){
                                                // 3️⃣ Tạo collection "start" Firestore
                                                Map<String, Object> startMap = new HashMap<>();
                                                startMap.put("welcome", "Chào mừng " + nameStr + "!");
                                                firebaseFirestore.collection("start").document(userId)
                                                        .set(startMap)
                                                        .addOnCompleteListener(startTask -> {
                                                            signUpBar.setVisibility(View.INVISIBLE);
                                                            signUpBtn.setEnabled(true);

                                                            if(startTask.isSuccessful()){
                                                                Toast.makeText(getActivity(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                startActivity(intent);
                                                                getActivity().finish();
                                                            } else {
                                                                Toast.makeText(getActivity(), "Lỗi tạo start: " + startTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(getActivity(), "Lỗi lưu Realtime: " + rTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } else {
                                        signUpBar.setVisibility(View.INVISIBLE);
                                        signUpBtn.setEnabled(true);
                                        Toast.makeText(getActivity(), "Lỗi lưu Users: " + userTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        signUpBar.setVisibility(View.INVISIBLE);
                        signUpBtn.setEnabled(true);
                        Toast.makeText(getActivity(), "Lỗi đăng ký: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.from_left, R.anim.out_from_right);
        fragmentTransaction.replace(R.id.register_frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
