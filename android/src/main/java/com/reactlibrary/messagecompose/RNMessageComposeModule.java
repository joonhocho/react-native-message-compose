package com.reactlibrary.messagecompose;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.util.Base64;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class RNMessageComposeModule extends ReactContextBaseJavaModule {
    private static final int ACTIVITY_SEND = 129382;

    private Promise mPromise;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == ACTIVITY_SEND) {
                if (mPromise != null) {
                    if (resultCode == Activity.RESULT_CANCELED) {
                        mPromise.reject("cancelled", "Operation has been cancelled");
                    } else {
                        mPromise.resolve("sent");
                    }
                    mPromise = null;
                }
            }
        }
    };

    public RNMessageComposeModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNMessageCompose";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("name", getName());
        return constants;
    }

    private void putExtra(Intent intent, String key, String value) {
        if (value != null && !value.isEmpty()) {
            intent.putExtra(key, value);
        }
    }

    private String getString(ReadableMap map, String key) {
        if (map.hasKey(key) && map.getType(key) == ReadableType.String) {
            return map.getString(key);
        }
        return null;
    }

    private ReadableMap getMap(ReadableMap map, String key) {
        if (map.hasKey(key) && map.getType(key) == ReadableType.Map) {
            return map.getMap(key);
        }
        return null;
    }

    private byte[] getBlob(ReadableMap map, String key) {
        if (map.hasKey(key) && map.getType(key) == ReadableType.String) {
            String base64 = map.getString(key);
            if (base64 != null && !base64.isEmpty()) {
                return Base64.decode(base64, 0);
            }
        }
        return null;
    }

    public static byte[] byteArrayFromUrl(String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;

        try {
            is = url.openStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.

            int n;
            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }

        return baos.toByteArray();
    }

    private byte[] getBlobFromUri(ReadableMap map, String key) {
        if (map.hasKey(key) && map.getType(key) == ReadableType.String) {
            String uri = map.getString(key);
            if (uri != null && !uri.isEmpty()) {
                return byteArrayFromUrl(uri);
            }
        }
        return null;
    }

    @ReactMethod
    public void send(ReadableMap data, Promise promise) {
        if (mPromise != null) {
            mPromise.reject("timeout", "Operation has timed out");
            mPromise = null;
        }

        String address = getString(data, "address");
        if (address != null && Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
            // http://stackoverflow.com/a/18975676/692528
            address = address.replace(';', ',');
        }
        if (address == null) {
            address = "";
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + address));
        // intent.setPackage("com.android.mms");
        // intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getCurrentActivity());
            if (defaultSmsPackageName != null) {
                intent.setPackage(defaultSmsPackageName);
            }
        }

        // intent.setType("vnd.android-dir/mms-sms");
        // intent.setData(Uri.parse("mms:" + address));

        putExtra(intent, "address", address);
        putExtra(intent, "subject", getString(data, "subject"));
        putExtra(intent, "sms_body", getString(data, "body"));
        // http://stackoverflow.com/a/21388864/692528
        putExtra(intent, intent.EXTRA_TEXT, getString(data, "body"));
        intent.putExtra("exit_on_sent", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        /*
        ReadableMap attachment = getMap(data, "attachment");
        if (attachment != null) {
            byte[] blob = getBlob(attachment, "data");
            String text = getString(attachment, "text");
            String type = getString(attachment, "type");
            String filename = getString(attachment, "filename");
            if (filename == null) {
                filename = UUID.randomUUID().toString();
            }
            String ext = getString(attachment, "ext");

            try {
                File tempFile = File.createTempFile(filename, ext, getCurrentActivity().getBaseContext().getCacheDir());

                if (text != null) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.getAbsoluteFile()));
                    try {
                        bw.write(text);
                        bw.flush();
                    } finally {
                        bw.close();
                    }
                } else if (blob != null) {
                    FileOutputStream fo = new FileOutputStream(tempFile);
                    try {
                        fo.write(blob);
                        fo.flush();
                    } finally {
                        fo.close();
                    }
                }

                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile));
                if (type != null) {
                    intent.setType(type);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        */


        try {
            getCurrentActivity().startActivityForResult(intent, ACTIVITY_SEND);
            mPromise = promise;
        } catch (ActivityNotFoundException e) {
            promise.reject("failed", "Activity not found");
        } catch (Exception e) {
            promise.reject("failed", "Unknown Error");
        }
    }
}
