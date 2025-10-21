package com.example.habitforge.presentation.activity.ui.QR;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.databinding.FragmentQrScanBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QrScanFragment extends Fragment {

    private FragmentQrScanBinding binding;
    private UserRepository userRepository;
    private DecoratedBarcodeView barcodeView;

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, android.view.ViewGroup container,
                                          Bundle savedInstanceState) {
        binding = FragmentQrScanBinding.inflate(inflater, container, false);
        userRepository = new UserRepository(requireContext());

        barcodeView = binding.barcodeScanner;

        binding.btnScanQr.setOnClickListener(v -> startQrScan());

        return binding.getRoot();
    }

    private void startQrScan() {
        // Pokreće kontinuirano skeniranje
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) return;

                // Zaustavi odmah skeniranje kad je prvi rezultat
                barcodeView.pause();

                String scannedUserId = result.getText();
                sendFriendRequest(scannedUserId);
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) { }
        });

        barcodeView.resume(); // Start skeniranja
    }

    private void sendFriendRequest(String toUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Morate biti prijavljeni!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fromUserId = currentUser.getUid();

        userRepository.sendFriendRequest(fromUserId, toUserId, new UserRepository.FriendRequestCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.habitforge.application.model.FriendRequest> requests) {
                Toast.makeText(getContext(), "Friend request poslat!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace(); // Ovo će pokazati zašto ne radi
                Toast.makeText(getContext(), "Greška pri slanju zahteva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



//package com.example.habitforge.presentation.activity.ui.QR;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.habitforge.data.repository.UserRepository;
//import com.example.habitforge.databinding.FragmentQrScanBinding;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;
//
//public class QrScanFragment extends Fragment {
//
//    private FragmentQrScanBinding binding;
//    private UserRepository userRepository;
//
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        binding = FragmentQrScanBinding.inflate(inflater, container, false);
//        userRepository = new UserRepository(requireContext());
//
//
//
//        binding.btnScanQr.setOnClickListener(v -> startQrScan());
//
//        return binding.getRoot();
//    }
//
//    private void startQrScan() {
//        new IntentIntegrator(requireActivity())
//                .setPrompt("Skeniraj QR kod prijatelja")
//                .setOrientationLocked(false) // ! promenjeno sa true na false
//                .setBeepEnabled(true)         // zvuk kad se skenira
//                .setCameraId(0)               // zadnja kamera
//                .initiateScan();
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null && result.getContents() != null) {
//            String scannedUserId = result.getContents();
//            sendFriendRequest(scannedUserId);
//        }
//    }
//
//    private void sendFriendRequest(String toUserId) {
//
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(getContext(), "Morate biti prijavljeni!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String fromUserId = currentUser.getUid();
//
//        userRepository.sendFriendRequest(fromUserId, toUserId, new UserRepository.FriendRequestCallback() {
//            @Override
//            public void onSuccess(java.util.List<com.example.habitforge.application.model.FriendRequest> requests) {
//                Toast.makeText(getContext(), "Friend request poslat!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(getContext(), "Greška pri slanju zahteva", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}
