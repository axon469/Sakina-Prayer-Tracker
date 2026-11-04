package org.axon.sakina;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private AlertDialog updateDialog;

    private static final String WEBSITE_URL =
            "https://axon469.github.io/Sakina-Prayer-Tracker/";

    private static final String UPDATE_JSON_URL =
            "https://raw.githubusercontent.com/axon469/Sakina-Prayer-Tracker/main/update.json";

    private static final int BG = Color.rgb(10, 10, 10);
    private static final int CARD = Color.rgb(27, 27, 29);
    private static final int CARD_DARK = Color.rgb(38, 38, 41);
    private static final int ACCENT = Color.rgb(207, 159, 134);
    private static final int WHITE = Color.WHITE;
    private static final int SOFT = Color.rgb(201, 201, 206);
    private static final int MUTED = Color.rgb(143, 143, 150);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildWebView(savedInstanceState);
        checkForUpdates();
    }

    private void buildWebView(Bundle savedInstanceState) {
        FrameLayout root = new FrameLayout(this);

        webView = new WebView(this);
        progressBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        root.addView(webView, webParams);

        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
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

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(
                        newProgress >= 100 ? View.GONE : View.VISIBLE
                );
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(WEBSITE_URL);
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
                String latestVersionName =
                        data.optString("versionName", "");
                String title =
                        data.optString("title", "A little update is ready.");
                String message =
                        data.optString("message", "A newer version of Sakina is available.");
                String downloadUrl =
                        data.optString("downloadUrl", "");

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
                // If the internet or update file is unavailable,
                // the prayer tracker continues working normally.
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
        panel.setBackground(roundRect(CARD, dp(28), Color.rgb(48, 48, 52), dp(1)));

        TextView brand = text("☾  SAKINA", 13, ACCENT, true);
        brand.setLetterSpacing(0.12f);
        panel.addView(brand, matchWrap());

        if (versionName != null && !versionName.trim().isEmpty()) {
            TextView version = text("VERSION " + versionName, 11, MUTED, true);
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
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
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
                    TextView bullet = text("•  " + item, 14, SOFT, false);
                    bullet.setLineSpacing(dp(3), 1.0f);
                    LinearLayout.LayoutParams bulletParams = matchWrap();
                    bulletParams.topMargin = dp(9);
                    panel.addView(bullet, bulletParams);
                }
            }
        }

        TextView updateButton = buttonText("Update now", BG);
        updateButton.setBackground(roundRect(ACCENT, dp(40), ACCENT, 0));
        LinearLayout.LayoutParams updateParams = matchWrap();
        updateParams.topMargin = dp(23);
        panel.addView(updateButton, updateParams);

        TextView laterButton = buttonText("Maybe later", WHITE);
        laterButton.setBackground(roundRect(CARD_DARK, dp(40), CARD_DARK, 0));
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
                // No browser available.
            }
            dialog.dismiss();
        });

        laterButton.setOnClickListener(v -> dialog.dismiss());

        updateDialog = dialog;
        dialog.show();
    }

    private TextView text(String value, int size, int color, boolean bold) {
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
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
