package com.example.music.Fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.example.music.Activities.MainActivity;
import com.example.music.R;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SignInFragment extends Fragment {

    private TextView dontHaveAnAccount;
    private TextView resetPassword;
    private FrameLayout frameLayout;
    private EditText email;
    private EditText password;
    private ProgressBar signInBar;
    private Button signInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleLauncher;
    private MaterialCardView googleSignInButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        dontHaveAnAccount = view.findViewById(R.id.don_t_have_an_account);
        resetPassword = view.findViewById(R.id.reset_password);
        frameLayout = getActivity().findViewById(R.id.register_frame_layout);

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        signInBar = view.findViewById(R.id.signInBar);
        signInButton = view.findViewById(R.id.signinButton);
        googleSignInButton = view.findViewById(R.id.googleSignInButton);
        mAuth = FirebaseAuth.getInstance();

        // Ẩn ProgressBar lúc khởi tạo
        signInBar.setVisibility(View.INVISIBLE);

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dontHaveAnAccount.setOnClickListener(v -> setFragment(new SignUpFragment()));
        resetPassword.setOnClickListener(v -> setFragment(new ResetPasswordFragment()));

        signInButton.setOnClickListener(v -> signInWithFirebase());
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

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });
    }
    private void handleGoogleResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Google Sign In Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user.getUid();

                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (!snapshot.exists()) {

                                // 🔥 USER MỚI → TẠO DỮ LIỆU
                                DatabaseReference newUserRef =
                                        FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(uid);

                                newUserRef.child("email")
                                        .setValue(user.getEmail());

                                newUserRef.child("name")
                                        .setValue(user.getDisplayName());

                                newUserRef.child("role")
                                        .setValue("user");

                                goToMain();
                                return;
                            }

                            String role = snapshot.child("role").getValue(String.class);

                            if ("admin".equals(role)) {

                                Intent intent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://muzic-sigma.vercel.app")
                                );
                                startActivity(intent);

                                FirebaseAuth.getInstance().signOut();
                                requireActivity().finish();

                            } else {
                                goToMain();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(),
                                    error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }

    private void goToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.form_right, R.anim.out_form_left);
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }

    private void signInWithFirebase() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty() || !userEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            email.setError("Invalid Email");
            return;
        }
        if (userPassword.isEmpty() || userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        signInBar.setVisibility(View.VISIBLE);
        signInButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        signInBar.setVisibility(View.INVISIBLE);
                        signInButton.setEnabled(true);
                        Toast.makeText(
                                getContext(),
                                task.getException() != null ? task.getException().getMessage() : "Sign-in failed",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    // ✅ ĐĂNG NHẬP OK → ĐỌC ROLE TỪ REALTIME DB
                    String uid = mAuth.getCurrentUser().getUid();

                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("role");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            signInBar.setVisibility(View.INVISIBLE);
                            signInButton.setEnabled(true);

                            if (!snapshot.exists()) {
                                Toast.makeText(getContext(), "Role not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String role = snapshot.getValue(String.class);

                            if ("admin".equals(role)) {

                                // 👉 MỞ WEB ADMIN
                                Intent intent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://muzic-sigma.vercel.app")
                                );
                                startActivity(intent);

                                // 🔥 LOGOUT ADMIN KHỎI APP
                                FirebaseAuth.getInstance().signOut();

                                getActivity().finish();
                            } else {
                                // 👉 USER → APP
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            signInBar.setVisibility(View.INVISIBLE);
                            signInButton.setEnabled(true);
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
    }

}
