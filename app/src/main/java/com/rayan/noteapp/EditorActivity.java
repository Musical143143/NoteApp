package com.rayan.noteapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class EditorActivity extends Activity {

    private NotesDB db;

    private FrameLayout screen;
    private LinearLayout main;

    private EditText titleEdit;
    private EditText bodyEdit;
    private EditText folderEdit;
    private EditText tagsEdit;

    private TextView countText;
    private TextView attachmentText;

    private long noteId = -1;
    private String attachmentUri = "";

    private static final int PICK_FILE = 501;

    // =========================================================
    // LIQUID GLASS COLORS
    // =========================================================

    private static final int BG =
            Color.rgb(1, 5, 14);

    private static final int GLASS =
            Color.rgb(8, 24, 48);

    private static final int GLASS_DARK =
            Color.rgb(4, 16, 34);

    private static final int GLASS_BLUE =
            Color.rgb(9, 31, 61);

    private static final int BLUE =
            Color.rgb(50, 130, 255);

    private static final int BLUE_LIGHT =
            Color.rgb(83, 166, 255);

    private static final int PURPLE =
            Color.rgb(124, 54, 255);

    private static final int PURPLE_LIGHT =
            Color.rgb(174, 85, 255);

    private static final int WHITE =
            Color.rgb(247, 250, 255);

    private static final int TEXT =
            Color.rgb(210, 221, 247);

    private static final int MUTED =
            Color.rgb(137, 158, 202);

    private static final int BORDER =
            Color.rgb(45, 104, 215);

    // =========================================================
    // DP
    // =========================================================

    private int dp(float v) {
        return (int) (
                v *
                getResources()
                        .getDisplayMetrics()
                        .density
                + .5f
        );
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        db = new NotesDB(this);

        if (getIntent().hasExtra("note_id")) {
            noteId = getIntent()
                    .getLongExtra(
                            "note_id",
                            -1
                    );
        }

        buildUI();

        if (noteId != -1) {
            loadNote();
        }

        updateCount();
    }

    // =========================================================
    // DRAWABLES
    // =========================================================

    private GradientDrawable glass(
            int color,
            int radius,
            int stroke
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);

        g.setCornerRadius(
                dp(radius)
        );

        g.setStroke(
                dp(1),
                stroke
        );

        return g;
    }

    private GradientDrawable liquidGlass(
            int radius
    ) {

        GradientDrawable g =
                new GradientDrawable(
                        GradientDrawable.Orientation
                                .LEFT_RIGHT,
                        new int[]{
                                Color.rgb(7, 30, 62),
                                Color.rgb(10, 19, 44),
                                Color.rgb(24, 13, 54)
                        }
                );

        g.setCornerRadius(
                dp(radius)
        );

        g.setStroke(
                dp(1),
                BLUE
        );

        return g;
    }

    private GradientDrawable saveGradient() {

        GradientDrawable g =
                new GradientDrawable(
                        GradientDrawable.Orientation
                                .LEFT_RIGHT,
                        new int[]{
                                BLUE,
                                PURPLE
                        }
                );

        g.setCornerRadius(
                dp(32)
        );

        return g;
    }

    // =========================================================
    // TEXT
    // =========================================================

    private TextView txt(
            String value,
            float size,
            int color
    ) {

        TextView t =
                new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        return t;
    }

    // =========================================================
    // BOUNCE
    // =========================================================

    private void bounce(View v) {

        v.setOnTouchListener(
                (view, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        view.animate()
                                .scaleX(.95f)
                                .scaleY(.95f)
                                .setDuration(70)
                                .start();

                    } else if (
                            event.getAction() ==
                                    MotionEvent.ACTION_UP ||
                            event.getAction() ==
                                    MotionEvent.ACTION_CANCEL) {

                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(130)
                                .start();
                    }

                    return false;
                }
        );
    }

    // =========================================================
    // MAIN LAYOUT
    // =========================================================

    private void buildUI() {

        /*
         * IMPORTANT:
         *
         * NO outer ScrollView.
         *
         * The screen itself stays fixed.
         *
         * ONLY bodyScroll scrolls.
         */

        screen =
                new FrameLayout(this);

        screen.setBackgroundColor(BG);

        setContentView(screen);

        main =
                new LinearLayout(this);

        main.setOrientation(
                LinearLayout.VERTICAL
        );

        main.setPadding(
                dp(28),
                dp(5),
                dp(28),
                dp(8)
        );

        main.setBackgroundColor(BG);

        screen.addView(
                main,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        screen.setOnApplyWindowInsetsListener(
                (v, insets) -> {

                    android.graphics.Insets top =
                            insets.getInsets(
                                    WindowInsets.Type.statusBars()
                            );

                    android.graphics.Insets bottom =
                            insets.getInsets(
                                    WindowInsets.Type.navigationBars()
                            );

                    main.setPadding(
                            dp(28),
                            top.top + dp(5),
                            dp(28),
                            bottom.bottom + dp(8)
                    );

                    return insets;
                }
        );

        buildHeader();
        buildTitle();
        buildActionBar();
        buildFormatBar();
        buildWritingArea();
        buildFolderTags();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void buildHeader() {

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams hp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(68)
                );

        hp.bottomMargin =
                dp(12);

        main.addView(
                header,
                hp
        );

        // BACK
        TextView back =
                txt(
                        "‹",
                        40,
                        WHITE
                );

        back.setGravity(
                Gravity.CENTER
        );

        back.setBackground(
                liquidGlass(28)
        );

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        dp(86),
                        dp(64)
                );

        header.addView(
                back,
                bp
        );

        bounce(back);

        back.setOnClickListener(
                v -> {

                    if (hasContent()) {
                        save();
                    } else {
                        finish();
                    }
                }
        );

        // CENTER
        LinearLayout heading =
                new LinearLayout(this);

        heading.setOrientation(
                LinearLayout.VERTICAL
        );

        heading.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView icon =
                txt(
                        "▤",
                        24,
                        BLUE_LIGHT
                );

        icon.setVisibility(
                View.GONE
        );

        TextView title =
                txt(
                        noteId == -1
                                ? "New note"
                                : "Edit note",
                        21,
                        TEXT
                );

        TextView subtitle =
                txt(
                        "Write your thoughts",
                        14,
                        MUTED
                );

        heading.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(29)
                )
        );

        heading.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(24)
                )
        );

        LinearLayout.LayoutParams hp2 =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        hp2.leftMargin =
                dp(12);

        header.addView(
                heading,
                hp2
        );

        // SAVE
        TextView save =
                txt(
                        "Save",
                        20,
                        WHITE
                );

        save.setGravity(
                Gravity.CENTER
        );

        save.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        save.setBackground(
                saveGradient()
        );

        LinearLayout.LayoutParams sp =
                new LinearLayout.LayoutParams(
                        dp(132),
                        dp(64)
                );

        header.addView(
                save,
                sp
        );

        bounce(save);

        save.setOnClickListener(
                v -> save()
        );
    }

    // =========================================================
    // TITLE CARD
    // =========================================================

    private void buildTitle() {

        FrameLayout card =
                new FrameLayout(this);

        card.setBackground(
                liquidGlass(34)
        );

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(126)
                );

        cp.bottomMargin =
                dp(16);

        main.addView(
                card,
                cp
        );

        // ICON CIRCLE
        TextView pencil =
                txt(
                        "✎",
                        30,
                        BLUE_LIGHT
                );

        pencil.setGravity(
                Gravity.CENTER
        );

        pencil.setBackground(
                glass(
                        Color.rgb(8, 40, 78),
                        40,
                        BORDER
                )
        );

        FrameLayout.LayoutParams pp =
                new FrameLayout.LayoutParams(
                        dp(64),
                        dp(64)
                );

        pp.gravity =
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL;

        pp.leftMargin =
                dp(16);

        card.addView(
                pencil,
                pp
        );

        // TITLE EDIT
        titleEdit =
                new EditText(this);

        titleEdit.setHint(
                "Title"
        );

        titleEdit.setTextSize(
                27
        );

        titleEdit.setTextColor(
                WHITE
        );

        titleEdit.setHintTextColor(
                MUTED
        );

        titleEdit.setSingleLine(
                true
        );

        titleEdit.setGravity(
                Gravity.CENTER_VERTICAL
        );

        titleEdit.setPadding(
                dp(94),
                0,
                dp(20),
                0
        );

        titleEdit.setBackgroundColor(
                Color.TRANSPARENT
        );

        card.addView(
                titleEdit,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );
    }

    // =========================================================
    // ACTION BAR
    // =========================================================

    private void buildActionBar() {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bar.setGravity(
                Gravity.CENTER
        );

        bar.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        bar.setBackground(
                liquidGlass(30)
        );

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(76)
                );

        bp.bottomMargin =
                dp(12);

        main.addView(
                bar,
                bp
        );

        actionButton(
                bar,
                "•",
                23,
                v -> insertAtCursor("• ")
        );

        actionButton(
                bar,
                "1.",
                18,
                v -> insertAtCursor("1. ")
        );

        actionButton(
                bar,
                "□",
                21,
                v -> insertAtCursor("☐ ")
        );

        actionButton(
                bar,
                "↶",
                22,
                v -> undo()
        );

        actionButton(
                bar,
                "↷",
                22,
                v -> redo()
        );

        actionButton(
                bar,
                "⌕",
                23,
                v -> chooseAttachment()
        );

        actionButton(
                bar,
                "↗",
                25,
                v -> shareNote()
        );
    }

    private void actionButton(
            LinearLayout parent,
            String value,
            float size,
            View.OnClickListener listener
    ) {

        TextView button =
                txt(
                        value,
                        size,
                        WHITE
                );

        button.setGravity(
                Gravity.CENTER
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setBackground(
                glass(
                        GLASS_DARK,
                        23,
                        BORDER
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(64),
                        1
                );

        p.leftMargin =
                dp(2);

        p.rightMargin =
                dp(2);

        parent.addView(
                button,
                p
        );

        bounce(button);

        button.setOnClickListener(
                listener
        );
    }

    // =========================================================
    // FORMAT BAR
    // =========================================================

    private void buildFormatBar() {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bar.setGravity(
                Gravity.CENTER
        );

        bar.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        bar.setBackground(
                liquidGlass(30)
        );

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(76)
                );

        bp.bottomMargin =
                dp(14);

        main.addView(
                bar,
                bp
        );

        formatButton(
                bar,
                "B",
                v -> applyStyle(
                        Typeface.BOLD
                )
        );

        formatButton(
                bar,
                "I",
                v -> applyStyle(
                        Typeface.ITALIC
                )
        );

        formatButton(
                bar,
                "U",
                v -> applyUnderline()
        );

        formatButton(
                bar,
                "S",
                v -> applyStrike()
        );

        formatButton(
                bar,
                "H1",
                v -> insertAtCursor("# ")
        );
    }

    private void formatButton(
            LinearLayout parent,
            String value,
            View.OnClickListener listener
    ) {

        TextView button =
                txt(
                        value,
                        value.equals("H1")
                                ? 18
                                : 22,
                        WHITE
                );

        button.setGravity(
                Gravity.CENTER
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setBackground(
                glass(
                        GLASS_DARK,
                        23,
                        BORDER
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(64),
                        1
                );

        p.leftMargin =
                dp(3);

        p.rightMargin =
                dp(3);

        parent.addView(
                button,
                p
        );

        bounce(button);

        button.setOnClickListener(
                listener
        );
    }

    // =========================================================
    // WRITING AREA
    // =========================================================

    private void buildWritingArea() {

        FrameLayout writingCard =
                new FrameLayout(this);

        writingCard.setBackground(
                liquidGlass(40)
        );

        LinearLayout.LayoutParams wp =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        main.addView(
                writingCard,
                wp
        );

        /*
         * TOP AREA
         */

        LinearLayout writingHeader =
                new LinearLayout(this);

        writingHeader.setOrientation(
                LinearLayout.HORIZONTAL
        );

        writingHeader.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // DOCUMENT ICON
        TextView document =
                txt(
                        "▤",
                        28,
                        BLUE_LIGHT
                );

        document.setGravity(
                Gravity.CENTER
        );

        document.setBackground(
                glass(
                        Color.rgb(8, 40, 78),
                        35,
                        BORDER
                )
        );

        LinearLayout.LayoutParams dp1 =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                );

        writingHeader.addView(
                document,
                dp1
        );

        LinearLayout heading =
                new LinearLayout(this);

        heading.setOrientation(
                LinearLayout.VERTICAL
        );

        heading.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView hint =
                txt(
                        "Start writing...",
                        20,
                        TEXT
                );

        TextView sub =
                txt(
                        "Capture your ideas...",
                        14,
                        MUTED
                );

        heading.addView(
                hint,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(29)
                )
        );

        heading.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(23)
                )
        );

        LinearLayout.LayoutParams hp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        hp.leftMargin =
                dp(12);

        writingHeader.addView(
                heading,
                hp
        );

        // EXPAND
        TextView expand =
                txt(
                        "⛶",
                        26,
                        WHITE
                );

        expand.setGravity(
                Gravity.CENTER
        );

        expand.setBackground(
                glass(
                        Color.rgb(8, 38, 74),
                        35,
                        BORDER
                )
        );

        LinearLayout.LayoutParams ep =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                );

        writingHeader.addView(
                expand,
                ep
        );

        FrameLayout.LayoutParams whp =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(76)
                );

        whp.gravity =
                Gravity.TOP;

        whp.leftMargin =
                dp(16);

        whp.rightMargin =
                dp(16);

        whp.topMargin =
                dp(16);

        writingCard.addView(
                writingHeader,
                whp
        );

        bounce(expand);

        expand.setOnClickListener(
                v -> {

                    bodyEdit.requestFocus();

                    InputMethodManager imm =
                            (InputMethodManager)
                                    getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

                    if (imm != null) {
                        imm.showSoftInput(
                                bodyEdit,
                                InputMethodManager
                                        .SHOW_IMPLICIT
                        );
                    }
                }
        );

        /*
         * ONLY THIS AREA SCROLLS
         */

        ScrollView bodyScroll =
                new ScrollView(this);

        bodyScroll.setFillViewport(true);

        bodyScroll.setVerticalScrollBarEnabled(
                true
        );

        bodyScroll.setScrollbarFadingEnabled(
                false
        );

        bodyScroll.setClipToPadding(
                false
        );

        bodyScroll.setOverScrollMode(
                View.OVER_SCROLL_IF_CONTENT_SCROLLS
        );

        bodyEdit =
                new EditText(this);

        bodyEdit.setTextSize(
                18
        );

        bodyEdit.setTextColor(
                WHITE
        );

        bodyEdit.setHintTextColor(
                MUTED
        );

        bodyEdit.setGravity(
                Gravity.TOP |
                Gravity.LEFT
        );

        bodyEdit.setHint(
                ""
        );

        bodyEdit.setPadding(
                dp(20),
                dp(8),
                dp(22),
                dp(40)
        );

        bodyEdit.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        bodyEdit.setBackgroundColor(
                Color.TRANSPARENT
        );

        bodyScroll.addView(
                bodyEdit,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
        );

        FrameLayout.LayoutParams bsp =
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                );

        bsp.leftMargin =
                dp(20);

        bsp.rightMargin =
                dp(12);

        bsp.topMargin =
                dp(88);

        bsp.bottomMargin =
                dp(70);

        writingCard.addView(
                bodyScroll,
                bsp
        );

        /*
         * BOTTOM BAR
         */

        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bottom.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView markdown =
                txt(
                        "▣  Markdown supported",
                        14,
                        MUTED
                );

        markdown.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams mp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        bottom.addView(
                markdown,
                mp
        );

        countText =
                txt(
                        "0 words  •  0 characters",
                        14,
                        MUTED
                );

        countText.setGravity(
                Gravity.CENTER
        );

        bottom.addView(
                countText,
                new LinearLayout.LayoutParams(
                        dp(180),
                        -1
                )
        );

        FrameLayout.LayoutParams bp =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        bp.gravity =
                Gravity.BOTTOM;

        bp.leftMargin =
                dp(18);

        bp.rightMargin =
                dp(12);

        bp.bottomMargin =
                dp(8);

        writingCard.addView(
                bottom,
                bp
        );

        bodyEdit.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        updateCount();

                        if (noteId != -1) {

                            bodyEdit.postDelayed(
                                    () -> autosave(),
                                    500
                            );
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable e
                    ) {
                    }
                }
        );
    }

    // =========================================================
    // FOLDER + TAGS
    // =========================================================

    private void buildFolderTags() {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(94)
                );

        rp.topMargin =
                dp(14);

        main.addView(
                row,
                rp
        );

        LinearLayout folder =
                metadata(
                        "📁",
                        "Folder",
                        false
                );

        LinearLayout tags =
                metadata(
                        "◇",
                        "Tags",
                        true
                );

        LinearLayout.LayoutParams fp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        fp.rightMargin =
                dp(6);

        row.addView(
                folder,
                fp
        );

        LinearLayout.LayoutParams tp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        tp.leftMargin =
                dp(6);

        row.addView(
                tags,
                tp
        );
    }

    private LinearLayout metadata(
            String icon,
            String hint,
            boolean purple
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(12),
                0,
                dp(7),
                0
        );

        card.setBackground(
                liquidGlass(30)
        );

        TextView iconView =
                txt(
                        icon,
                        21,
                        purple
                                ? PURPLE_LIGHT
                                : WHITE
                );

        iconView.setGravity(
                Gravity.CENTER
        );

        card.addView(
                iconView,
                new LinearLayout.LayoutParams(
                        dp(34),
                        -1
                )
        );

        EditText edit =
                new EditText(this);

        edit.setHint(
                hint
        );

        edit.setTextSize(
                14
        );

        edit.setTextColor(
                WHITE
        );

        edit.setHintTextColor(
                TEXT
        );

        edit.setSingleLine(
                true
        );

        edit.setGravity(
                Gravity.CENTER_VERTICAL
        );

        edit.setPadding(
                dp(3),
                0,
                0,
                0
        );

        edit.setBackgroundColor(
                Color.TRANSPARENT
        );

        if (hint.equals("Folder")) {
            folderEdit = edit;
        } else {
            tagsEdit = edit;
        }

        card.addView(
                edit,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView arrow =
                txt(
                        "›",
                        28,
                        WHITE
                );

        arrow.setGravity(
                Gravity.CENTER
        );

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(25),
                        -1
                )
        );

        bounce(card);

        card.setOnClickListener(
                v -> {

                    edit.requestFocus();

                    InputMethodManager imm =
                            (InputMethodManager)
                                    getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

                    if (imm != null) {

                        imm.showSoftInput(
                                edit,
                                InputMethodManager
                                        .SHOW_IMPLICIT
                        );
                    }
                }
        );

        return card;
    }

    // =========================================================
    // LOAD
    // =========================================================

    private void loadNote() {

        Note n =
                db.get(noteId);

        if (n == null) {
            return;
        }

        titleEdit.setText(
                n.title
        );

        if (n.body != null &&
                !n.body.isEmpty()) {

            bodyEdit.setText(
                    Html.fromHtml(
                            n.body,
                            Html.FROM_HTML_MODE_LEGACY
                    )
            );
        }

        folderEdit.setText(
                n.folder == null
                        ? ""
                        : n.folder
        );

        tagsEdit.setText(
                n.tags == null
                        ? ""
                        : n.tags
        );

        attachmentUri =
                n.attachment == null
                        ? ""
                        : n.attachment;

        updateAttachment();
        updateCount();
    }

    // =========================================================
    // CONTENT
    // =========================================================

    private boolean hasContent() {

        return
                !titleEdit
                        .getText()
                        .toString()
                        .trim()
                        .isEmpty()
                ||
                !bodyEdit
                        .getText()
                        .toString()
                        .trim()
                        .isEmpty();
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void save() {

        if (!hasContent()) {

            Toast.makeText(
                    this,
                    "Write something first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String title =
                titleEdit
                        .getText()
                        .toString()
                        .trim();

        String body =
                Html.toHtml(
                        bodyEdit.getText(),
                        Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                );

        String folder =
                folderEdit
                        .getText()
                        .toString()
                        .trim();

        String tags =
                tagsEdit
                        .getText()
                        .toString()
                        .trim();

        if (folder.isEmpty()) {
            folder = "General";
        }

        long id;

        if (noteId == -1) {

            id =
                    db.insert(
                            title,
                            body,
                            folder,
                            tags
                    );

            noteId = id;

        } else {

            db.update(
                    noteId,
                    title,
                    body,
                    folder,
                    tags
            );

            id = noteId;
        }

        if (id != -1) {

            db.attachment(
                    id,
                    attachmentUri
            );
        }

        setResult(
                RESULT_OK,
                new Intent()
                        .putExtra(
                                "note_saved",
                                true
                        )
                        .putExtra(
                                "note_id",
                                id
                        )
        );

        Toast.makeText(
                this,
                "Saved",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    // =========================================================
    // AUTOSAVE
    // =========================================================

    private void autosave() {

        if (noteId == -1 ||
                !hasContent()) {
            return;
        }

        String title =
                titleEdit
                        .getText()
                        .toString()
                        .trim();

        String body =
                Html.toHtml(
                        bodyEdit.getText(),
                        Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                );

        String folder =
                folderEdit
                        .getText()
                        .toString()
                        .trim();

        String tags =
                tagsEdit
                        .getText()
                        .toString()
                        .trim();

        if (folder.isEmpty()) {
            folder = "General";
        }

        db.update(
                noteId,
                title,
                body,
                folder,
                tags
        );
    }

    // =========================================================
    // COUNT
    // =========================================================

    private void updateCount() {

        if (countText == null ||
                bodyEdit == null) {
            return;
        }

        String value =
                bodyEdit
                        .getText()
                        .toString()
                        .trim();

        int chars =
                value.length();

        int words =
                value.isEmpty()
                        ? 0
                        : value.split(
                                "\\s+"
                        ).length;

        countText.setText(
                words +
                " words  •  " +
                chars +
                " characters"
        );
    }

    // =========================================================
    // FORMATTING
    // =========================================================

    private void applyStyle(
            int style
    ) {

        int start =
                bodyEdit.getSelectionStart();

        int end =
                bodyEdit.getSelectionEnd();

        if (start < 0 ||
                end <= start) {

            Toast.makeText(
                    this,
                    "Select text first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        bodyEdit
                .getText()
                .setSpan(
                        new StyleSpan(style),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
    }

    private void applyUnderline() {

        int start =
                bodyEdit.getSelectionStart();

        int end =
                bodyEdit.getSelectionEnd();

        if (start < 0 ||
                end <= start) {
            return;
        }

        bodyEdit
                .getText()
                .setSpan(
                        new UnderlineSpan(),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
    }

    private void applyStrike() {

        int start =
                bodyEdit.getSelectionStart();

        int end =
                bodyEdit.getSelectionEnd();

        if (start < 0 ||
                end <= start) {
            return;
        }

        bodyEdit
                .getText()
                .setSpan(
                        new StrikethroughSpan(),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
    }

    // =========================================================
    // INSERT
    // =========================================================

    private void insertAtCursor(
            String value
    ) {

        int position =
                bodyEdit.getSelectionStart();

        if (position < 0) {
            position =
                    bodyEdit.length();
        }

        bodyEdit
                .getText()
                .insert(
                        position,
                        value
                );
    }

    // =========================================================
    // UNDO / REDO
    // =========================================================

    private void undo() {

        Toast.makeText(
                this,
                "Undo",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void redo() {

        Toast.makeText(
                this,
                "Redo",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // ATTACHMENT
    // =========================================================

    private void chooseAttachment() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType("*/*");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(
                intent,
                PICK_FILE
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_FILE &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            Uri uri =
                    data.getData();

            attachmentUri =
                    uri.toString();

            try {

                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {
            }

            updateAttachment();
        }
    }

    private void updateAttachment() {

        if (attachmentText == null) {
            return;
        }

        if (attachmentUri == null ||
                attachmentUri.isEmpty()) {

            return;
        }
    }

    // =========================================================
    // SHARE
    // =========================================================

    private void shareNote() {

        String title =
                titleEdit
                        .getText()
                        .toString();

        String body =
                bodyEdit
                        .getText()
                        .toString();

        Intent intent =
                new Intent(
                        Intent.ACTION_SEND
                );

        intent.setType(
                "text/plain"
        );

        intent.putExtra(
                Intent.EXTRA_TEXT,
                title +
                "\n\n" +
                body
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "Share note"
                )
        );
    }
}
