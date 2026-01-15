package com.example.music.Fragments;

import android.content.Intent;
import android.media.MediaPlayer;
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

import com.example.music.Activities.MainActivity;
import com.example.music.R;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class SignInFragment extends Fragment {

    private TextView dontHaveAnAccount;
    private TextView resetPassword;
    private FrameLayout frameLayout;
    private EditText email;
    private EditText password;
    private ProgressBar signInBar;
    private Button signInButton;
    private FirebaseAuth mAuth;

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

        // Validate input
        if (userEmail.isEmpty() || !userEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            email.setError("Invalid Email");
            return;
        }
        if (userPassword.isEmpty() || userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        // Show loading và disable button
        signInBar.setVisibility(View.VISIBLE);
        signInButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    // Ẩn loading và enable button dù thành công hay thất bại
                    signInBar.setVisibility(View.INVISIBLE);
                    signInButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        Toast.makeText(getContext(),
                                task.getException() != null ? task.getException().getMessage() : "Sign-in failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
