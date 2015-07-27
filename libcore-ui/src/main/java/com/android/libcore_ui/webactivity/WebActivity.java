package com.android.libcore_ui.webactivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import com.android.libcore.Toast.T;
import com.android.libcore.dialog.BaseDialog;
import com.android.libcore_ui.R;
import com.android.libcore_ui.activity.BaseActivity;
import com.android.libcore_ui.dialog.DialogFactory;
import com.android.libcore_ui.dialog.LoadingDialog;

/**
 * Description: 应用基础的网页浏览activity
 *
 * @author zzp(zhao_zepeng@hotmail.com)
 * @since 2015-07-27
 */
public class WebActivity extends BaseActivity{

    public static final String EXTRA_URL = "extra_url";

    private WebView webView;
    private FrameworkWebViewClient webViewClient = new FrameworkWebViewClient();
    private FrameworkChromeClient chromeClient = new FrameworkChromeClient();

    private LoadingDialog dialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView() {
        setContentViewSrc(R.layout.activity_web_layout);
        webView = (WebView) findViewById(R.id.wb_content);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(chromeClient);

        WebSettings settings = webView.getSettings();
        //设置网页大小自适应
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        //支持缩放
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        //支持js
        settings.setJavaScriptEnabled(true);

        dialog = new LoadingDialog(this);
    }

    @Override
    protected void initData() {
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url != null)
            webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }

    private class FrameworkWebViewClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //使用应用浏览器加载
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            dialog.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            dialog.dismiss();
        }
    }

    private class FrameworkChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            BitmapDrawable drawable = new BitmapDrawable(getResources(), icon);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            tv_title.setCompoundDrawables(drawable, null, null, null);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            tv_title.setText(title);
        }

        //js警告框
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            T.getInstance().showShort(message);
            result.confirm();
            return true;
        }

        //确认框
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            DialogFactory.createDialog(null, message, getString(R.string.confirm), getString(R.string.cancel))
                    .setOnButtonClickListener(new BaseDialog.ButtonClickListener() {
                        @Override
                        public void onButtonClick(int button_id) {
                            if (button_id == 0){
                                result.confirm();
                            }else{
                                result.cancel();
                            }
                        }
                    })
                    .show();
            return true;
        }

        //提示框
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            final View v = View.inflate(WebActivity.this, R.layout.dialog_js_prompt_message_layout, null);
            ((TextView)(v.findViewById(R.id.tv_message))).setText(message);
            ((EditText)(v.findViewById(R.id.et_content))).setText(defaultValue);
            Dialog dialog = DialogFactory.createDialog(null, v, getString(R.string.confirm), getString(R.string.cancel))
                    .setOnButtonClickListener(new BaseDialog.ButtonClickListener() {
                        @Override
                        public void onButtonClick(int button_id) {
                            if (button_id == 0) {
                                result.confirm(((EditText) (v.findViewById(R.id.et_content))).getText().toString());
                            } else {
                                result.cancel();
                            }
                        }
                    });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
             return true;
        }

    }
}
