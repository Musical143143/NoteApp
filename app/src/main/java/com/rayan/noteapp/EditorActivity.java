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

    private long noteId = -1;
    private String attachmentUri = "";

    private static final int PICK_FILE = 501;

    // =========================================================
    // COLORS
    // =========================================================

    private static final int BG =
            Color.rgb(1, 5, 14);

    private static final int GLASS_1 =
            Color.rgb(7, 28, 58);

    private static final int GLASS_2 =
            Color.rgb(7, 18, 39);

    private static final int GLASS_3 =
            Color.rgb(22, 11, 50);

    private static final int INNER =
            Color.rgb(5, 17, 36);

    private static final int BLUE =
            Color.rgb(48, 128, 255);

    private static final int BLUE_LIGHT =
            Color.rgb(92, 170, 255);

    private static final int PURPLE =
            Color.rgb(119, 48, 255);

    private static final int PURPLE_LIGHT =
            Color.rgb(174, 91, 255);

    private static final int WHITE =
            Color.rgb(248, 250, 255);

    private static final int TEXT =
            Color.rgb(211, 222, 247);

    private static final int MUTED =
            Color.rgb(137, 157, 202);

    // =========================================================
    // DP
    // =========================================================

    private int dp(float value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
                + 0.5f
        );
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    // GLASS
    // =========================================================

    private GradientDrawable liquidGlass(
            int radius
    ) {

        GradientDrawable g =
                new GradientDrawable(
                        GradientDrawable.Orientation
                                .LEFT_RIGHT,
                        new int[]{
                                GLASS_1,
                                GLASS_2,
                                GLASS_3
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

    private GradientDrawable innerGlass(
            int radius
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(INNER);

        g.setCornerRadius(
                dp(radius)
        );

        g.setStroke(
                dp(1),
                Color.rgb(38, 94, 194)
        );

        return g;
    }

    private GradientDrawable saveBackground() {

        GradientDrawable g =
                new GradientDrawable(
                        GradientDrawable.Orientation
                                .LEFT_RIGHT,
                        new int[]{
                                Color.rgb(63, 132, 255),
                                Color.rgb(119, 45, 255)
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

    private TextView text(
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

    private void bounce(View view) {

        view.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        v.animate()
                                .scaleX(.95f)
                                .scaleY(.95f)
                                .setDuration(60)
                                .start();

                    } else if (
                            event.getAction() ==
                                    MotionEvent.ACTION_UP ||
                            event.getAction() ==
                                    MotionEvent.ACTION_CANCEL) {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start();
                    }

                    return false;
                }
        );
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private void buildUI() {

        /*
         * IMPORTANT:
         *
         * There is NO outer ScrollView.
         *
         * The whole editor stays fixed.
         *
         * ONLY bodyScroll inside the writing
         * card is scrollable.
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
                dp(4),
                dp(28),
                dp(6)
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

                    android.graphics.Insets status =
                            insets.getInsets(
                                    WindowInsets.Type.statusBars()
                            );

                    android.graphics.Insets nav =
                            insets.getInsets(
                                    WindowInsets.Type.navigationBars()
                            );

                    main.setPadding(
                            dp(28),
                            status.top + dp(4),
                            dp(28),
                            nav.bottom + dp(6)
                    );

                    return insets;
                }
        );

        buildHeader();
        buildTitle();
        buildActionBar();
        buildFormatBar();
        buildWritingCard();
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
                dp(10);

        main.addView(
                header,
                hp
        );

        // BACK
        TextView back =
                text(
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

        LinearLayout.LayoutParams backLp =
                new LinearLayout.LayoutParams(
                        dp(88),
                        dp(62)
                );

        header.addView(
                back,
                backLp
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

        // CENTER TITLE
        TextView headerTitle =
                text(
                        noteId == -1
                                ? "New note"
                                : "Edit note",
                        21,
                        TEXT
                );

        headerTitle.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams titleLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        header.addView(
                headerTitle,
                titleLp
        );

        // SAVE
        TextView save =
                text(
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
                saveBackground()
        );

        LinearLayout.LayoutParams saveLp =
                new LinearLayout.LayoutParams(
                        dp(130),
                        dp(62)
                );

        header.addView(
                save,
                saveLp
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
                        dp(122)
                );

        cp.bottomMargin =
                dp(14);

        main.addView(
                card,
                cp
        );

        // PENCIL
        TextView pencil =
                text(
                        "✎",
                        30,
                        BLUE_LIGHT
                );

        pencil.setGravity(
                Gravity.CENTER
        );

        pencil.setBackground(
                innerGlass(40)
        );

        FrameLayout.LayoutParams pencilLp =
                new FrameLayout.LayoutParams(
                        dp(62),
                        dp(62)
                );

        pencilLp.gravity =
                Gravity.LEFT |
                Gravity.CENTER_VERTICAL;

        pencilLp.leftMargin =
                dp(16);

        card.addView(
                pencil,
                pencilLp
        );

        // TITLE
        titleEdit =
                new EditText(this);

        titleEdit.setHint(
                "Title"
        );

        titleEdit.setTextSize(
                28
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

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(74)
                );

        lp.bottomMargin =
                dp(10);

        main.addView(
                bar,
                lp
        );

        addAction(
                bar,
                "•",
                22,
                v -> insertAtCursor("• ")
        );

        addAction(
                bar,
                "1.",
                18,
                v -> insertAtCursor("1. ")
        );

        addAction(
                bar,
                "□",
                21,
                v -> insertAtCursor("☐ ")
        );

        addAction(
                bar,
                "↶",
                22,
                v -> undo()
        );

        addAction(
                bar,
                "↷",
                22,
                v -> redo()
        );

        addAction(
                bar,
                "⌕",
                23,
                v -> chooseAttachment()
        );

        addAction(
                bar,
                "↗",
                25,
                v -> shareNote()
        );
    }

    private void addAction(
            LinearLayout parent,
            String value,
            float size,
            View.OnClickListener listener
    ) {

        TextView button =
                text(
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
                innerGlass(23)
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(62),
                        1
                );

        lp.leftMargin =
                dp(2);

        lp.rightMargin =
                dp(2);

        parent.addView(
                button,
                lp
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

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(74)
                );

        lp.bottomMargin =
                dp(12);

        main.addView(
                bar,
                lp
        );

        addFormat(
                bar,
                "B",
                22,
                v -> applyStyle(
                        Typeface.BOLD
                )
        );

        addFormat(
                bar,
                "I",
                22,
                v -> applyStyle(
                        Typeface.ITALIC
                )
        );

        addFormat(
                bar,
                "U",
                22,
                v -> applyUnderline()
        );

        addFormat(
                bar,
                "S",
                22,
                v -> applyStrike()
        );

        addFormat(
                bar,
                "H1",
                18,
                v -> insertAtCursor("# ")
        );
    }

    private void addFormat(
            LinearLayout parent,
            String value,
            float size,
            View.OnClickListener listener
    ) {

        TextView button =
                text(
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
                innerGlass(23)
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(62),
                        1
                );

        lp.leftMargin =
                dp(3);

        lp.rightMargin =
                dp(3);

        parent.addView(
                button,
                lp
        );

        bounce(button);

        button.setOnClickListener(
                listener
        );
    }

    // =========================================================
    // WRITING CARD
    // =========================================================

    private void buildWritingCard() {

        FrameLayout card =
                new FrameLayout(this);

        card.setBackground(
                liquidGlass(40)
        );

        /*
         * The writing card takes the remaining
         * available screen height.
         *
         * Folder and Tags stay visible.
         */

        LinearLayout.LayoutParams cardLp =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        main.addView(
                card,
                cardLp
        );

        // -----------------------------------------------------
        // WRITING HEADER
        // -----------------------------------------------------

        LinearLayout top =
                new LinearLayout(this);

        top.setOrientation(
                LinearLayout.HORIZONTAL
        );

        top.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // DOCUMENT ICON
        TextView document =
                text(
                        "▤",
                        28,
                        BLUE_LIGHT
                );

        document.setGravity(
                Gravity.CENTER
        );

        document.setBackground(
                innerGlass(38)
        );

        top.addView(
                document,
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                )
        );

        // HEADER TEXT
        LinearLayout labels =
                new LinearLayout(this);

        labels.setOrientation(
                LinearLayout.VERTICAL
        );

        labels.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView start =
                text(
                        "Start writing...",
                        20,
                        WHITE
                );

        TextView capture =
                text(
                        "Capture your ideas...",
                        14,
                        MUTED
                );

        labels.addView(
                start,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(29)
                )
        );

        labels.addView(
                capture,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(23)
                )
        );

        LinearLayout.LayoutParams labelsLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        labelsLp.leftMargin =
                dp(12);

        top.addView(
                labels,
                labelsLp
        );

        // EXPAND
        TextView expand =
                text(
                        "⛶",
                        27,
                        WHITE
                );

        expand.setGravity(
                Gravity.CENTER
        );

        expand.setBackground(
                innerGlass(38)
        );

        top.addView(
                expand,
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                )
        );

        FrameLayout.LayoutParams topLp =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(72)
                );

        topLp.gravity =
                Gravity.TOP;

        topLp.leftMargin =
                dp(16);

        topLp.rightMargin =
                dp(16);

        topLp.topMargin =
                dp(15);

        card.addView(
                top,
                topLp
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

        // -----------------------------------------------------
        // BODY SCROLL
        // -----------------------------------------------------

        ScrollView bodyScroll =
                new ScrollView(this);

        bodyScroll.setFillViewport(
                false
        );

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

        bodyEdit.setPadding(
                dp(18),
                dp(8),
                dp(20),
                dp(30)
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

        FrameLayout.LayoutParams bodyLp =
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                );

        bodyLp.gravity =
                Gravity.TOP;

        bodyLp.leftMargin =
                dp(20);

        bodyLp.rightMargin =
                dp(10);

        bodyLp.topMargin =
                dp(86);

        bodyLp.bottomMargin =
                dp(62);

        card.addView(
                bodyScroll,
                bodyLp
        );

        // -----------------------------------------------------
        // BOTTOM GLASS FOOTER
        // -----------------------------------------------------

        LinearLayout footer =
                new LinearLayout(this);

        footer.setOrientation(
                LinearLayout.HORIZONTAL
        );

        footer.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // MARKDOWN
        LinearLayout markdown =
                new LinearLayout(this);

        markdown.setOrientation(
                LinearLayout.HORIZONTAL
        );

        markdown.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView mdIcon =
                text(
                        "▣",
                        19,
                        BLUE_LIGHT
                );

        mdIcon.setGravity(
                Gravity.CENTER
        );

        markdown.addView(
                mdIcon,
                new LinearLayout.LayoutParams(
                        dp(32),
                        -1
                )
        );

        TextView mdText =
                text(
                        "Markdown\nsupported",
                        14,
                        MUTED
                );

        mdText.setGravity(
                Gravity.CENTER_VERTICAL
        );

        markdown.addView(
                mdText,
                new LinearLayout.LayoutParams(
                        dp(116),
                        -1
                )
        );

        footer.addView(
                markdown,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        // DIVIDER
        View divider =
                new View(this);

        divider.setBackgroundColor(
                Color.rgb(
                        38,
                        91,
                        178
                )
        );

        footer.addView(
                divider,
                new LinearLayout.LayoutParams(
                        dp(1),
                        dp(35)
                )
        );

        // COUNT
        countText =
                text(
                        "0 words  •  0 characters",
                        14,
                        MUTED
                );

        countText.setGravity(
                Gravity.CENTER
        );

        footer.addView(
                countText,
                new LinearLayout.LayoutParams(
                        dp(180),
                        -1
                )
        );

        FrameLayout.LayoutParams footerLp =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(56)
                );

        footerLp.gravity =
                Gravity.BOTTOM;

        footerLp.leftMargin =
                dp(18);

        footerLp.rightMargin =
                dp(12);

        footerLp.bottomMargin =
                dp(8);

        card.addView(
                footer,
                footerLp
        );

        // -----------------------------------------------------
        // COUNTER WATCHER
        // -----------------------------------------------------

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

        LinearLayout.LayoutParams rowLp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(94)
                );

        rowLp.topMargin =
                dp(12);

        main.addView(
                row,
                rowLp
        );

        LinearLayout folder =
                metadataCard(
                        "📁",
                        "Folder",
                        false
                );

        LinearLayout tags =
                metadataCard(
                        "◇",
                        "Tags",
                        true
                );

        LinearLayout.LayoutParams folderLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        folderLp.rightMargin =
                dp(6);

        row.addView(
                folder,
                folderLp
        );

        LinearLayout.LayoutParams tagsLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        tagsLp.leftMargin =
                dp(6);

        row.addView(
                tags,
                tagsLp
        );
    }

    private LinearLayout metadataCard(
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
                dp(10),
                0,
                dp(7),
                0
        );

        card.setBackground(
                liquidGlass(30)
        );

        TextView iconView =
                text(
                        icon,
                        22,
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
                        dp(38),
                        -1
                )
        );

        EditText edit =
                new EditText(this);

        edit.setHint(
                hint
        );

        edit.setTextSize(
                15
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
                dp(2),
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
                text(
                        "›",
                        29,
                        WHITE
                );

        arrow.setGravity(
                Gravity.CENTER
        );

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(27),
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
    // LOAD NOTE
    // =========================================================

    private void loadNote() {

        Note note =
                db.get(noteId);

        if (note == null) {
            return;
        }

        titleEdit.setText(
                note.title
        );

        if (note.body != null &&
                !note.body.isEmpty()) {

            bodyEdit.setText(
                    Html.fromHtml(
                            note.body,
                            Html.FROM_HTML_MODE_LEGACY
                    )
            );
        }

        folderEdit.setText(
                note.folder == null
                        ? ""
                        : note.folder
        );

        tagsEdit.setText(
                note.tags == null
                        ? ""
                        : note.tags
        );

        attachmentUri =
                note.attachment == null
                        ? ""
                        : note.attachment;

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

        int characters =
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
                characters +
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

            Toast.makeText(
                    this,
                    "Attachment added",
                    Toast.LENGTH_SHORT
            ).show();
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

    // =========================================================
    // BACK
    // =========================================================

    @Override
    public void onBackPressed() {

        if (hasContent()) {
            save();
        } else {
            super.onBackPressed();
        }
    }
}
