package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;

import android.os.Build;
import android.util.Log;

import com.android.apksig.ApkSigner;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.utils.apksigner.JarMap;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompilerUtil {

    private static final String TAG = "CompilerUtil";
    private static final String aapt = References.TOOLS_DIR + "/libaapt.so";
    private static final String zipalign = References.TOOLS_DIR + "/libzipalign.so";

    public static boolean buildOverlays() throws IOException {
        preExecute();

        // Create AndroidManifest.xml and build APK using AAPT
        File dir = new File(References.DATA_DIR + "/Overlays");
        for (File pkg : Objects.requireNonNull(dir.listFiles())) {
            if (pkg.isDirectory()) {
                for (File overlay : Objects.requireNonNull(pkg.listFiles())) {
                    if (overlay.isDirectory()) {
                        String overlay_name = overlay.toString().replace(pkg.toString() + '/', "");
                        if (createManifest(overlay_name, pkg.toString().replace(References.DATA_DIR + "/Overlays/", ""), overlay.getAbsolutePath())) {
                            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
                            postExecute(true);
                            return true;
                        }
                        if (runAapt(overlay.getAbsolutePath(), overlay_name)) {
                            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
                            postExecute(true);
                            return true;
                        }
                    }
                }
            }
        }

        // ZipAlign the APK
        dir = new File(References.UNSIGNED_UNALIGNED_DIR);
        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                if (zipAlign(overlay.getAbsolutePath(), overlay.toString().replace(References.UNSIGNED_UNALIGNED_DIR + '/', "").replace("-unaligned", ""))) {
                    Log.e(TAG, "Failed to align " + overlay + "! Exiting...");
                    postExecute(true);
                    return true;
                }
            }
        }

        // Sign the APK
        dir = new File(References.UNSIGNED_DIR);
        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                if (apkSigner(overlay.getAbsolutePath(), overlay.toString().replace(References.UNSIGNED_DIR + '/', "").replace("-unsigned", ""))) {
                    Log.e(TAG, "Failed to sign " + overlay + "! Exiting...");
                    postExecute(true);
                    return true;
                }
            }
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Clean data directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();

        // Extract keystore and overlays from assets
        FileUtil.copyAssets("Keystore");
        FileUtil.copyAssets("Overlays");

        // Create temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR + "; mkdir -p " + References.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + References.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.SIGNED_DIR).exec();
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -a " + References.SIGNED_DIR + "/. " + References.OVERLAY_DIR).exec();
            RootUtil.setPermissionsRecursively(644, References.OVERLAY_DIR + '/');
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();
    }

    private static boolean createManifest(String pkgName, String target, String source) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String source, String name) {
        return !Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + References.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec().isSuccess();
    }

    private static boolean zipAlign(String source, String name) {
        return !Shell.cmd(zipalign + " 4 " + source + ' ' + References.UNSIGNED_DIR + '/' + name).exec().isSuccess();
    }

    private static boolean apkSigner(String source, String name) {
        try {
            File key = new File(References.DATA_DIR + "/Keystore/key");
            char[] keyPass = "overlay".toCharArray();

            if (!key.exists()) {
                Log.d("KeyStore", "Loading keystore from assets...");
                FileUtil.copyAssets("Keystore");
            }

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(key), keyPass);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("key", keyPass);
            List<X509Certificate> certs = new ArrayList<>();
            certs.add((X509Certificate) keyStore.getCertificateChain("key")[0]);

            ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder("overlay", privateKey, certs).build();
            List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<>();
            signerConfigs.add(signerConfig);
            new ApkSigner.Builder(signerConfigs)
                    .setV1SigningEnabled(true)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(true)
                    .setInputApk(new File(source))
                    .setOutputApk(new File(References.SIGNED_DIR + "/IconifyComponent" + name))
                    .setMinSdkVersion(Build.VERSION.SDK_INT)
                    .build()
                    .sign();
        } catch (Exception e) {
            Log.e(TAG, "Signature scheme v1 + v2 + v3 signing method failed! Using only v1 instead...\n" + e);

            try {
                File testKey = new File(References.DATA_DIR + "/Keystore/testkey.pk8");
                File certificate = new File(References.DATA_DIR + "/Keystore/testkey.x509.pem");

                if (!testKey.exists() || !certificate.exists()) {
                    Log.d("KeyStore", "Loading keystore from assets...");
                    FileUtil.copyAssets("Keystore");
                }

                InputStream keyFile = new FileInputStream(testKey);
                PrivateKey key = readPrivateKey(keyFile);

                InputStream certFile = new FileInputStream(certificate);
                X509Certificate cert = readCertificate(certFile);

                JarMap jar = JarMap.open(new FileInputStream(source), true);
                FileOutputStream out = new FileOutputStream(References.SIGNED_DIR + "/IconifyComponent" + name);

                SignAPK.sign(cert, key, jar, out);
            } catch (Exception exception) {
                Log.e(TAG, exception.toString());
                postExecute(true);
                return true;
            }
        }
        return false;
    }
}
