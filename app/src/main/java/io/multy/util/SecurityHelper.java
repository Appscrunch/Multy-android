package io.multy.util;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.scottyab.rootbeer.RootBeer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import io.multy.BuildConfig;
import io.multy.Multy;
import timber.log.Timber;

public class SecurityHelper {

    private static Random random = new SecureRandom();
    private static String result;
    private static boolean pass;

    private static OnSuccessListener<SafetyNetApi.AttestationResponse> successListener =
            new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
                @Override
                public void onSuccess(SafetyNetApi.AttestationResponse attestationResponse) {
                    result = attestationResponse.getJwsResult();
                    final String[] jwt = result.split("\\.");
                    String decodedPayload = new String(Base64.decode(jwt[1], Base64.DEFAULT));
                    if (decodedPayload != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(decodedPayload);
                            pass = jsonObj.getString("basicIntegrity").equals("true");
                        } catch (final JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

    private static OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            result = null;
            if (e instanceof ApiException) {
                ApiException apiException = (ApiException) e;
                Timber.d("Error: " + CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " + apiException);
            } else {
                Timber.d("Error: " + e.getMessage());
            }
        }
    };

    private static byte[] getNonce(String data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);

        try {
            byteStream.write(bytes);
            byteStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return byteStream.toByteArray();
    }

    public static void sendSafetyNetRequest(AppCompatActivity activity) {
        String nonceData = System.currentTimeMillis() + System.nanoTime() + "";
        byte[] nonce = getNonce(nonceData);

        SafetyNetClient client = SafetyNet.getClient(activity);
        Task<SafetyNetApi.AttestationResponse> task = client.attest(nonce, BuildConfig.GCC_API_KEY);
        task.addOnSuccessListener(activity, successListener).addOnFailureListener(activity, failureListener);
    }


    public static boolean checkForBinary(String filename) {
        for (String path : Constants.rootPaths) {
            final String completePath = path + filename;
            final File file = new File(completePath);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkForBinaries(){
        for (String filename : Constants.rootFiles){
            if (checkForBinary(filename)){
                return true;
            }
        }
        return false;
    }

    public static boolean isConnected() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Multy.getContext()) == ConnectionResult.SUCCESS;
    }

    public static boolean isSecured(AppCompatActivity activity){
        RootBeer rootBeer = new RootBeer(activity);
        pass = true;
        if (isConnected()) {
            sendSafetyNetRequest(activity);
        }
        return !(rootBeer.detectRootManagementApps(Constants.rootApplications) || rootBeer.isRootedWithoutBusyBoxCheck() || checkForBinaries() || !pass);
    }
}
