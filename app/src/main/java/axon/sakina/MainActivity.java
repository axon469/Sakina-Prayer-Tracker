package org.axon.sakina;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.webkit.WebViewAssetLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private AlertDialog updateDialog;

    private GeolocationPermissions.Callback locationCallback;
    private String locationOrigin;

    private ValueCallback<Uri[]> fileCallback;

    private String pendingBackupContent;
    private String pendingBackupFilename;

    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private static final int FILE_PICKER_REQUEST = 102;
    private static final int SAVE_BACKUP_REQUEST = 103;

    private static final String LOCAL_URL =
            "https://appassets.androidplatform.net/assets/index.html";

    private static final String UPDATE_JSON_URL =
            "https://raw.githubusercontent.com/axon469/Sakina-Prayer-Tracker/main/update.json";

    private static final int BG = Color.rgb(10, 10, 10);
    private static final int CARD = Color.rgb(27, 27, 29);
    private static final int CARD_DARK = Color.rgb(38, 38, 41);
    private static final int ACCENT = Color.rgb(207, 159, 134);
    private static final int WHITE = Color.WHITE;
    private static final int SOFT = Color.rgb(201, 201, 206);
    private static final int MUTED = Color.rgb(143, 143, 150);

    private WebViewAssetLoader assetLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler(
                        "/assets/",
                        new WebViewAssetLoader.AssetsPathHandler(this)
                )
                .build();

        buildWebView();
        checkForUpdates();
    }

    private void buildWebView() {

        FrameLayout root = new FrameLayout(this);

        webView = new WebView(this);

        progressBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        root.addView(webView, webParams);

        FrameLayout.LayoutParams progressParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(3),
                        Gravity.TOP
                );

        root.addView(progressBar, progressParams);

        setContentView(root);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);

        // Native backup-saving bridge for the bundled Sakina HTML.
        webView.addJavascriptInterface(
                new AndroidFileBridge(),
                "AndroidFileBridge"
        );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    android.webkit.WebResourceRequest request
            ) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    android.webkit.WebResourceRequest request
            ) {
                String url = request.getUrl().toString();

                if (url.startsWith(LOCAL_URL)
                        || url.startsWith("https://appassets.androidplatform.net/")) {
                    return false;
                }

                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );
                    startActivity(intent);
                } catch (Exception ignored) {
                }

                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(
                        newProgress >= 100 ? View.GONE : View.VISIBLE
                );
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin,
                    GeolocationPermissions.Callback callback
            ) {
                requestLocationPermission(origin, callback);
            }

            // Handles both JSON backup imports and background image selection.
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params
            ) {
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }

                fileCallback = callback;

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");

                String[] acceptedTypes = params.getAcceptTypes();

if (acceptedTypes != null && acceptedTypes.length > 0) {
    java.util.ArrayList<String> mimeTypes = new java.util.ArrayList<>();

    for (String type : acceptedTypes) {
        if (type == null || type.trim().isEmpty()) {
            continue;
        }

        type = type.trim().toLowerCase(java.util.Locale.ROOT);

        if (type.equals(".json")) {
            mimeTypes.add("application/json");
        } else if (type.contains("/")) {
            mimeTypes.add(type);
        }
    }

    if (mimeTypes.size() == 1) {
        intent.setType(mimeTypes.get(0));
    } else if (!mimeTypes.isEmpty()) {
        intent.setType("*/*");
        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                mimeTypes.toArray(new String[0])
        );
    }
}

                try {
                    startActivityForResult(intent, FILE_PICKER_REQUEST);
                    return true;
                } catch (Exception e) {
                    fileCallback.onReceiveValue(null);
                    fileCallback = null;
                    return false;
                }
            }
        });

        // Always open the bundled HTML.
        webView.loadUrl(LOCAL_URL);
    }

    // JavaScript calls this when the user exports a backup.
    private class AndroidFileBridge {

        @JavascriptInterface
        public void saveBackup(String filename, String content) {

            if (filename == null || content == null) {
                return;
            }

            runOnUiThread(() -> {

                pendingBackupFilename = filename;
                pendingBackupContent = content;

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(
                        Intent.EXTRA_TITLE,
                        pendingBackupFilename
                );

                try {
                    startActivityForResult(intent, SAVE_BACKUP_REQUEST);
                } catch (Exception e) {
                    pendingBackupContent = null;
                    pendingBackupFilename = null;
                }
            });
        }
    }

    private void requestLocationPermission(
            String origin,
            GeolocationPermissions.Callback callback
    ) {

        locationOrigin = origin;
        locationCallback = callback;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            callback.invoke(origin, true, false);
            locationCallback = null;
            return;
        }

        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST
                && locationCallback != null) {

            boolean granted =
                    checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            locationCallback.invoke(
                    locationOrigin,
                    granted,
                    false
            );

            locationCallback = null;
            locationOrigin = null;
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST) {

            if (fileCallback != null) {

                Uri[] results = null;

                if (resultCode == RESULT_OK && data != null) {

                    if (data.getData() != null) {
                        results = new Uri[]{data.getData()};
                    } else if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        results = new Uri[count];

                        for (int i = 0; i < count; i++) {
                            results[i] = data.getClipData()
                                    .getItemAt(i)
                                    .getUri();
                        }
                    }
                }

                fileCallback.onReceiveValue(results);
                fileCallback = null;
            }
        }

        if (requestCode == SAVE_BACKUP_REQUEST) {

            if (resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null
                    && pendingBackupContent != null) {

                Uri destination = data.getData();

                try (OutputStream output =
                             getContentResolver().openOutputStream(destination)) {

                    if (output != null) {
                        output.write(
                                pendingBackupContent.getBytes(
                                        java.nio.charset.StandardCharsets.UTF_8
                                )
                        );
                        output.flush();
                    }

                } catch (Exception ignored) {
                    // The save operation may fail if the selected provider is unavailable.
                }
            }

            pendingBackupContent = null;
            pendingBackupFilename = null;
        }
    }

    private void checkForUpdates() {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(UPDATE_JSON_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setUseCaches(false);
                connection.setRequestProperty("Cache-Control", "no-cache");

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return;
                }

                InputStream input = connection.getInputStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input, "UTF-8")
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                input.close();

                JSONObject data = new JSONObject(result.toString());

                int latestVersionCode = data.optInt("versionCode", 0);
                String latestVersionName = data.optString("versionName", "");
                String title = data.optString(
                        "title",
                        "A little update is ready."
                );
                String message = data.optString(
                        "message",
                        "A newer version of Sakina is available."
                );
                String downloadUrl = data.optString("downloadUrl", "");
                JSONArray whatsNew = data.optJSONArray("whatsNew");

                int installedVersionCode = getInstalledVersionCode();

                if (latestVersionCode > installedVersionCode
                        && !downloadUrl.trim().isEmpty()) {

                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            showUpdateDialog(
                                    latestVersionName,
                                    title,
                                    message,
                                    whatsNew,
                                    downloadUrl
                            );
                        }
                    });
                }

            } catch (Exception ignored) {
                // The app continues working if the internet is unavailable.
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    @SuppressWarnings("deprecation")
    private int getInstalledVersionCode() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionCode;
        } catch (Exception e) {
            return 1;
        }
    }

    private void showUpdateDialog(
            String versionName,
            String title,
            String message,
            JSONArray whatsNew,
            String downloadUrl
    ) {

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(22), dp(24), dp(22));
        panel.setBackground(
                roundRect(CARD, dp(28), Color.rgb(48, 48, 52), dp(1))
        );

        TextView brand = text("☾  SAKINA", 13, ACCENT, true);
        brand.setLetterSpacing(0.12f);
        panel.addView(brand, matchWrap());

        if (versionName != null && !versionName.trim().isEmpty()) {
            TextView version = text(
                    "VERSION " + versionName,
                    11,
                    MUTED,
                    true
            );

            version.setLetterSpacing(0.10f);

            LinearLayout.LayoutParams versionParams = matchWrap();
            versionParams.topMargin = dp(8);

            panel.addView(version, versionParams);
        }

        TextView heading = text(title, 25, WHITE, true);
        heading.setLineSpacing(dp(1), 1.0f);

        LinearLayout.LayoutParams headingParams = matchWrap();
        headingParams.topMargin = dp(17);

        panel.addView(heading, headingParams);

        TextView description = text(message, 14, SOFT, false);
        description.setLineSpacing(dp(4), 1.0f);

        LinearLayout.LayoutParams descriptionParams = matchWrap();
        descriptionParams.topMargin = dp(10);

        panel.addView(description, descriptionParams);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(55, 55, 59));

        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        dividerParams.topMargin = dp(19);
        dividerParams.bottomMargin = dp(17);

        panel.addView(divider, dividerParams);

        TextView whatsNewTitle = text("WHAT'S NEW", 11, MUTED, true);
        whatsNewTitle.setLetterSpacing(0.12f);
        panel.addView(whatsNewTitle, matchWrap());

        if (whatsNew != null) {
            for (int i = 0; i < whatsNew.length(); i++) {

                String item = whatsNew.optString(i, "");

                if (!item.trim().isEmpty()) {

                    TextView bullet = text(
                            "•  " + item,
                            14,
                            SOFT,
                            false
                    );

                    bullet.setLineSpacing(dp(3), 1.0f);

                    LinearLayout.LayoutParams bulletParams = matchWrap();
                    bulletParams.topMargin = dp(9);

                    panel.addView(bullet, bulletParams);
                }
            }
        }

        TextView updateButton = buttonText("Update now", BG);
        updateButton.setBackground(
                roundRect(ACCENT, dp(40), ACCENT, 0)
        );

        LinearLayout.LayoutParams updateParams = matchWrap();
        updateParams.topMargin = dp(23);

        panel.addView(updateButton, updateParams);

        TextView laterButton = buttonText("Maybe later", WHITE);
        laterButton.setBackground(
                roundRect(CARD_DARK, dp(40), CARD_DARK, 0)
        );

        LinearLayout.LayoutParams laterParams = matchWrap();
        laterParams.topMargin = dp(10);

        panel.addView(laterButton, laterParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(panel)
                .create();

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                dialog.getWindow().setDimAmount(0.72f);
            }
        });

        updateButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(downloadUrl)
                );
                startActivity(intent);
            } catch (Exception ignored) {
            }

            dialog.dismiss();
        });

        laterButton.setOnClickListener(v -> dialog.dismiss());

        updateDialog = dialog;
        dialog.show();
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {
        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START);
        view.setIncludeFontPadding(false);

        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return view;
    }

    private TextView buttonText(String value, int color) {
        TextView view = text(value, 15, color, true);

        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(52));
        view.setPadding(dp(16), dp(13), dp(16), dp(13));

        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private GradientDrawable roundRect(
            int color,
            int radius,
            int strokeColor,
            int strokeWidth
    ) {
        GradientDrawable drawable = new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(radius);

        if (strokeWidth > 0) {
            drawable.setStroke(strokeWidth, strokeColor);
        }

        return drawable;
    }

    private int dp(float value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (locationCallback != null) {
            locationCallback.invoke(
                    locationOrigin,
                    false,
                    false
            );
            locationCallback = null;
        }

        if (fileCallback != null) {
            fileCallback.onReceiveValue(null);
            fileCallback = null;
        }

        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }

        if (webView != null) {
            webView.removeJavascriptInterface("AndroidFileBridge");
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
