package com.rayan.noteapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LockActivity extends Activity {

    private static final int BG =
            Color.rgb(2, 5, 12);

    private static final int WHITE =
            Color.rgb(248, 249, 255);

    private static final int MUTED =
            Color.rgb(157, 170, 205);

    private static final int BLUE =
            Color.rgb(82, 112, 255);

    private static final int PURPLE =
            Color.rgb(120, 62, 246);

    private static final int BORDER =
            Color.rgb(70, 95, 170);

    private EditText pinEdit;

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Window window =
                getWindow();

        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);

        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);
        }

        buildUI();
    }

    private GradientDrawable background(
            int start,
            int end,
            int stroke,
            int radius) {

        GradientDrawable drawable =
                new GradientDrawable(
                        GradientDrawable
                                .Orientation
                                .TL_BR,
                        new int[]{
                                start,
                                end
                        }
                );

        drawable.setCornerRadius(
                dp(radius)
        );

        drawable.setStroke(
                dp(1),
                stroke
        );

        return drawable;
    }

    private void buildUI() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(28),
                dp(24),
                dp(28),
                dp(24)
        );

        root.setBackgroundColor(BG);

        setContentView(root);

        TextView icon =
                new TextView(this);

        icon.setText("🔐");
        icon.setTextSize(48);
        icon.setGravity(Gravity.CENTER);

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        TextView title =
                new TextView(this);

        title.setText(
                "Notes Locked"
        );

        title.setTextColor(WHITE);
        title.setTextSize(28);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                )
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Enter your PIN to continue"
        );

        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(14);

        subtitle.setGravity(
                Gravity.CENTER
        );

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        pinEdit =
                new EditText(this);

        pinEdit.setHint("••••");

        pinEdit.setTextColor(WHITE);
        pinEdit.setHintTextColor(MUTED);
        pinEdit.setTextSize(24);

        pinEdit.setGravity(
                Gravity.CENTER
        );

        pinEdit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        pinEdit.setSingleLine(true);

        pinEdit.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );

        pinEdit.setBackground(
                background(
                        Color.rgb(17, 25, 46),
                        Color.rgb(29, 20, 55),
                        BORDER,
                        24
                )
        );

        LinearLayout.LayoutParams pinLp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                );

        pinLp.topMargin = dp(18);
        pinLp.bottomMargin = dp(14);

        root.addView(
                pinEdit,
                pinLp
        );

        TextView unlock =
                new TextView(this);

        unlock.setText("UNLOCK");

        unlock.setTextColor(WHITE);
        unlock.setTextSize(15);

        unlock.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        unlock.setGravity(
                Gravity.CENTER
        );

        unlock.setBackground(
                background(
                        BLUE,
                        PURPLE,
                        BORDER,
                        25
                )
        );

        root.addView(
                unlock,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        unlock.setOnClickListener(
                v -> unlock()
        );

        pinEdit.setOnEditorActionListener(
                (v, actionId, event) -> {

                    unlock();

                    return true;
                }
        );
    }

    private void unlock() {

        String entered =
                pinEdit.getText()
                        .toString()
                        .trim();

        if (entered.length() < 4) {

            Toast.makeText(
                    this,
                    "Enter your PIN",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                AppLock.verify(
                        this,
                        entered
                )
        ) {

            setResult(
                    RESULT_OK
            );

            finish();

        } else {

            pinEdit.setText("");

            Toast.makeText(
                    this,
                    "Wrong PIN",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onBackPressed() {

        finishAffinity();
    }
}
