package com.example.music.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment {

    private FrameLayout frameLayout;
    private TextView back;
    private EditText email;
    private ProgressBar resetBar;
    private Button resetButton;
    private TextView responseMessage;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        back = view.findViewById(R.id.back);
        frameLayout = getActivity().findViewById(R.id.register_frame_layout);
        email = view.findViewById(R.id.email);
        resetBar = view.findViewById(R.id.ResetBar);
        responseMessage = view.findViewById(R.id.responseMessage);
        resetButton = view.findViewById(R.id.resetbutton);

        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        back.setOnClickListener(v -> setFragment(new SignInFragment()));

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        resetButton.setOnClickListener(v -> resetPassword());
    }

    private void checkInputs() {
        String emailInput = email.getText().toString().trim();
        resetButton.setEnabled(!TextUtils.isEmpty(emailInput));
    }

    private void resetPassword() {
        String emailInput = email.getText().toString().trim();

        if (TextUtils.isEmpty(emailInput)) {
            email.setError("Email không được để trống");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Email không hợp lệ");
            return;
        }

        resetBar.setVisibility(View.VISIBLE);
        resetButton.setEnabled(false);

        mAuth.sendPasswordResetEmail(emailInput)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        resetBar.setVisibility(View.INVISIBLE);
                        resetButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            responseMessage.setText("Liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
                            responseMessage.setVisibility(View.VISIBLE);
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                            responseMessage.setText(error);
                            responseMessage.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.from_left, R.anim.out_from_right);
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}
