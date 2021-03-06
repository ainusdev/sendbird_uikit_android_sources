package com.sendbird.uikit.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class SoftInputUtils {
    public static void hideSoftKeyboard(View view) {
        if (view == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager) view.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    view.getWindowToken(), 0);
        }
    }

    public static void showSoftKeyboard(EditText editText) {
        if (editText == null) {
            return;
        }
        
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        editText.postDelayed(() -> {
            editText.requestFocus();
            if (imm != null) {
                imm.showSoftInput(editText, 0);
            }
        }, 100);
    }

    public static void setSoftInputMode(@NonNull Context context, int mode) {
        try {
            ((Activity) context).getWindow().setSoftInputMode(mode);
        } catch (Throwable ignore) {}
    }

    public static int getSoftInputMode(@NonNull Context context) {
        try {
            return ((Activity) context).getWindow().getAttributes().softInputMode;
        } catch (Throwable ignore) {}
        return WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED;
    }
}
