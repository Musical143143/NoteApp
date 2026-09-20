package com.rayan.noteapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class EditorActivity extends Activity {

    private NotesDB db;

    private FrameLayout frame;
    private LinearLayout root;

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
    // COLORS
    // =========================================================

    private static final int BG =
            Color.rgb(3, 7, 16);

    private static final int GLASS =
            Color.rgb(15, 24, 43);

    private static final int GLASS_2 =
            Color.rgb(18, 29, 52);

    private static final int GLASS_3 =
            Color.rgb(20, 34, 61);

    private static final int BORDER =
            Color.rgb(61, 86, 145);

    private static final int BORDER_BRIGHT =
            Color.rgb(91, 137, 225);

    private static final int WHITE =
            Color.rgb(248, 250, 255);

    private static final int MUTED =
            Color.rgb(153, 166, 200);

    private static final int LIGHT =
            Color.rgb(184, 198, 239);

    private static final int BLUE =
            Color.rgb(66, 133, 255);

    private static final int PURPLE =
            Color.rgb(117, 67, 255);

    private static final int CYAN =
            Color.rgb(83, 215, 255);

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
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        WindowInsetsFix();

        db = new NotesDB(this);

        if (
                getIntent().hasExtra("note_id")
        ) {

            noteId =
                    getIntent()
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
    // WINDOW
    // =========================================================

    private void WindowInsetsFix() {

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        if (Build.VERSION.SDK_INT >= 30) {

            getWindow()
                    .setDecorFitsSystemWindows(false);
        }
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private void buildUI() {

        frame =
                new FrameLayout(this);

        frame.setBackgroundColor(BG);

        setContentView(frame);

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        scroll.setClipToPadding(false);

        scroll.setVerticalScrollBarEnabled(false);

        frame.addView(
                scroll,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(18)
        );

        scroll.addView(
                root,
                new ScrollView.LayoutParams(
                        -1,
                        -2
                )
        );

        root.setOnApplyWindowInsetsListener(
                (v, insets) -> {

                    int top = 0;
                    int bottom = 0;

                    if (Build.VERSION.SDK_INT >= 30) {

                        android.graphics.Insets bars =
                                insets.getInsets(
                                        WindowInsets.Type.statusBars()
                                                | WindowInsets.Type.displayCutout()
                                                | WindowInsets.Type.navigationBars()
                                );

                        top = bars.top;
                        bottom = bars.bottom;
                    }

                    root.setPadding(
                            dp(16),
                            top + dp(8),
                            dp(16),
                            bottom + dp(18)
                    );

                    return insets;
                }
        );

        buildTopBar();

        space(8);

        buildTitleCard();

        space(12);

        buildActionToolbar();

        space(10);

        buildFormatToolbar();

        space(12);

        buildBodyCard();

        space(12);

        buildMetadata();

        space(8);

        buildCounter();

        buildAttachment();
    }

    // =========================================================
    // TOP BAR
    // =========================================================

    private void buildTopBar() {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                bar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(66)
                )
        );

        TextView back =
                glassButton(
                        "‹",
                        36
                );

        LinearLayout.LayoutParams backLp =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                );

        bar.addView(
                back,
                backLp
        );

        back.setOnClickListener(
                v -> {

                    bounce(v);

                    if (hasContent()) {
                        save();
                    } else {
                        finish();
                    }
                }
        );

        TextView title =
                label(
                        noteId == -1
                                ? "New note"
                                : "Edit note",
                        19,
                        MUTED
                );

        title.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams titleLp =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        bar.addView(
                title,
                titleLp
        );

        TextView save =
                gradientButton(
                        "Save",
                        17
                );

        LinearLayout.LayoutParams saveLp =
                new LinearLayout.LayoutParams(
                        dp(108),
                        dp(58)
                );

        bar.addView(
                save,
                saveLp
        );

        save.setOnClickListener(
                v -> {

                    bounce(v);

                    save();
                }
        );
    }

    // =========================================================
    // TITLE GLASS CARD
    // =========================================================

    private void buildTitleCard() {

        LinearLayout card =
                glassCard(
                        GLASS_2,
                        27
                );

        titleEdit =
                new EditText(this);

        titleEdit.setHint(
                "Title"
        );

        titleEdit.setTextSize(31);

        titleEdit.setTextColor(
                WHITE
        );

        titleEdit.setHintTextColor(
                Color.rgb(119, 132, 170)
        );

        titleEdit.setSingleLine(true);

        titleEdit.setGravity(
                Gravity.CENTER_VERTICAL
        );

        titleEdit.setPadding(
                dp(18),
                0,
                dp(18),
                0
        );

        titleEdit.setBackgroundColor(
                Color.TRANSPARENT
        );

        card.addView(
                titleEdit,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(86)
                )
        );

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(92)
                )
        );
    }

    // =========================================================
    // FIRST TOOLBAR
    // =========================================================

    private void buildActionToolbar() {

        LinearLayout card =
                glassCard(
                        GLASS,
                        28
                );

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        scroll.setHorizontalScrollBarEnabled(
                false
        );

        scroll.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        LinearLayout tools =
                new LinearLayout(this);

        tools.setOrientation(
                LinearLayout.HORIZONTAL
        );

        tools.setGravity(
                Gravity.CENTER_VERTICAL
        );

        tools.setPadding(
                dp(9),
                dp(8),
                dp(9),
                dp(8)
        );

        scroll.addView(
                tools,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        -1
                )
        );

        addTool(
                tools,
                "•",
                19,
                v -> insertAtCursor("• ")
        );

        addTool(
                tools,
                "1.",
                18,
                v -> insertAtCursor("1. ")
        );

        addTool(
                tools,
                "□",
                20,
                v -> insertAtCursor("☐ ")
        );

        addTool(
                tools,
                "↶",
                22,
                v -> undo()
        );

        addTool(
                tools,
                "↷",
                22,
                v -> redo()
        );

        addTool(
                tools,
                "📎",
                21,
                v -> chooseAttachment()
        );

        addTool(
                tools,
                "↗",
                22,
                v -> shareNote()
        );

        card.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );
    }

    // =========================================================
    // SECOND TOOLBAR
    // =========================================================

    private void buildFormatToolbar() {

        LinearLayout card =
                glassCard(
                        GLASS,
                        28
                );

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        scroll.setHorizontalScrollBarEnabled(
                false
        );

        scroll.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        LinearLayout tools =
                new LinearLayout(this);

        tools.setOrientation(
                LinearLayout.HORIZONTAL
        );

        tools.setGravity(
                Gravity.CENTER
        );

        tools.setPadding(
                dp(9),
                dp(8),
                dp(9),
                dp(8)
        );

        scroll.addView(
                tools,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        -1
                )
        );

        addTool(
                tools,
                "B",
                19,
                v -> applyStyle(
                        Typeface.BOLD
                )
        );

        addTool(
                tools,
                "I",
                19,
                v -> applyStyle(
                        Typeface.ITALIC
                )
        );

        addTool(
                tools,
                "U",
                19,
                v -> applyUnderline()
        );

        addTool(
                tools,
                "S",
                19,
                v -> applyStrike()
        );

        addTool(
                tools,
                "H1",
                17,
                v -> insertAtCursor("# ")
        );

        card.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );
    }

    // =========================================================
    // BODY GLASS CARD
    // =========================================================

    private void buildBodyCard() {

        LinearLayout card =
                glassCard(
                        Color.rgb(8, 15, 29),
                        30
                );

        bodyEdit =
                new EditText(this);

        bodyEdit.setHint(
                "Start writing..."
        );

        bodyEdit.setTextSize(19);

        bodyEdit.setTextColor(
                WHITE
        );

        bodyEdit.setHintTextColor(
                Color.rgb(119, 132, 168)
        );

        bodyEdit.setGravity(
                Gravity.TOP | Gravity.START
        );

        bodyEdit.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(18)
        );

        bodyEdit.setBackgroundColor(
                Color.TRANSPARENT
        );

        bodyEdit.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                );

        card.addView(
                bodyEdit,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(590)
                )
        );

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(598)
                )
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
                            Editable s
                    ) {
                    }
                }
        );
    }

    // =========================================================
    // FOLDER + TAGS
    // =========================================================

    private void buildMetadata() {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER
        );

        root.addView(
                row,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(82)
                )
        );

        LinearLayout folderCard =
                metadataCard(
                        "📁",
                        "Folder"
                );

        folderEdit =
                (EditText)
                        folderCard.getTag();

        LinearLayout.LayoutParams folderLp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                );

        row.addView(
                folderCard,
                folderLp
        );

        LinearLayout tagsCard =
                metadataCard(
                        "◇",
                        "Tags"
                );

        tagsEdit =
                (EditText)
                        tagsCard.getTag();

        LinearLayout.LayoutParams tagsLp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(78),
                        1
                );

        tagsLp.leftMargin =
                dp(10);

        row.addView(
                tagsCard,
                tagsLp
        );
    }

    private LinearLayout metadataCard(
            String icon,
            String hint
    ) {

        LinearLayout card =
                glassCard(
                        GLASS_2,
                        27
                );

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView iconText =
                label(
                        icon,
                        23,
                        hint.equals("Folder")
                                ? CYAN
                                : Color.rgb(
                                        167,
                                        112,
                                        255
                                )
                );

        iconText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                iconText,
                new LinearLayout.LayoutParams(
                        dp(42),
                        -1
                )
        );

        EditText edit =
                new EditText(this);

        edit.setHint(
                hint
        );

        edit.setTextSize(17);

        edit.setTextColor(
                WHITE
        );

        edit.setHintTextColor(
                MUTED
        );

        edit.setSingleLine(true);

        edit.setGravity(
                Gravity.CENTER_VERTICAL
        );

        edit.setPadding(
                0,
                0,
                0,
                0
        );

        edit.setBackgroundColor(
                Color.TRANSPARENT
        );

        card.addView(
                edit,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView arrow =
                label(
                        "›",
                        31,
                        LIGHT
                );

        arrow.setGravity(
                Gravity.CENTER
        );

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(38),
                        -1
                )
        );

        card.setTag(edit);

        return card;
    }

    // =========================================================
    // ATTACHMENT
    // =========================================================

    private void buildAttachment() {

        attachmentText =
                label(
                        "",
                        14,
                        LIGHT
                );

        attachmentText.setGravity(
                Gravity.CENTER_VERTICAL
        );

        attachmentText.setPadding(
                dp(18),
                0,
                dp(18),
                0
        );

        attachmentText.setBackground(
                glass(
                        Color.rgb(
                                17,
                                31,
                                55
                        ),
                        22,
                        BORDER
                )
        );

        attachmentText.setVisibility(
                View.GONE
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        lp.topMargin = dp(10);

        root.addView(
                attachmentText,
                lp
        );
    }

    // =========================================================
    // COUNTER
    // =========================================================

    private void buildCounter() {

        countText =
                label(
                        "0 words  •  0 characters",
                        14,
                        MUTED
                );

        countText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                countText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );
    }

    // =========================================================
    // TOOL BUTTON
    // =========================================================

    private void addTool(
            LinearLayout parent,
            String value,
            float size,
            View.OnClickListener listener
    ) {

        TextView tool =
                glassButton(
                        value,
                        size
                );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        dp(70),
                        dp(60)
                );

        lp.leftMargin = dp(3);
        lp.rightMargin = dp(3);

        parent.addView(
                tool,
                lp
        );

        tool.setOnClickListener(
                v -> {

                    bounce(v);

                    listener.onClick(v);
                }
        );
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

        if (
                n.body != null
                        && !n.body.isEmpty()
        ) {

            bodyEdit.setText(
                    Html.fromHtml(
                            n.body,
                            Html.FROM_HTML_MODE_LEGACY
                    )
            );
        }

        folderEdit.setText(
                n.folder
        );

        tagsEdit.setText(
                n.tags
        );

        attachmentUri =
                n.attachment == null
                        ? ""
                        : n.attachment;

        updateAttachment();
    }

    // =========================================================
    // SAVE
    // =========================================================

    private boolean hasContent() {

        return !titleEdit
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

        String body =
                Html.toHtml(
                        bodyEdit.getText(),
                        Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                );

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

        if (
                noteId == -1
                        || !hasContent()
        ) {
            return;
        }

        String title =
                titleEdit
                        .getText()
                        .toString()
                        .trim();

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

        String body =
                Html.toHtml(
                        bodyEdit.getText(),
                        Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                );

        db.update(
                noteId,
                title,
                body,
                folder,
                tags
        );
    }

    // =========================================================
    // COUNTER
    // =========================================================

    private void updateCount() {

        if (
                countText == null
                        || bodyEdit == null
        ) {
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
                        : value.split("\\s+").length;

        countText.setText(
                words
                        + " words  •  "
                        + chars
                        + " characters"
        );
    }

    // =========================================================
    // TEXT FORMATTING
    // =========================================================

    private void applyStyle(
            int style
    ) {

        int start =
                bodyEdit.getSelectionStart();

        int end =
                bodyEdit.getSelectionEnd();

        if (
                start < 0
                        || end <= start
        ) {

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

        if (
                start < 0
                        || end <= start
        ) {
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

        if (
                start < 0
                        || end <= start
        ) {
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

    private void insertAtCursor(
            String value
    ) {

        int position =
                bodyEdit.getSelectionStart();

        if (position < 0) {
            position = bodyEdit.length();
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
                "Undo is handled by text editing",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void redo() {

        Toast.makeText(
                this,
                "Redo is handled by text editing",
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
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
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

        if (
                requestCode == PICK_FILE
                        && resultCode == RESULT_OK
                        && data != null
                        && data.getData() != null
        ) {

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

        if (
                attachmentUri == null
                        || attachmentUri.isEmpty()
        ) {

            attachmentText.setVisibility(
                    View.GONE
            );

            return;
        }

        attachmentText.setVisibility(
                View.VISIBLE
        );

        attachmentText.setText(
                "📎  Attachment added                         ›"
        );

        attachmentText.setOnClickListener(
                v -> {

                    try {

                        Intent intent =
                                new Intent(
                                        Intent.ACTION_VIEW
                                );

                        intent.setData(
                                Uri.parse(
                                        attachmentUri
                                )
                        );

                        intent.addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        startActivity(intent);

                    } catch (Exception e) {

                        Toast.makeText(
                                this,
                                "Cannot open attachment",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
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
                title
                        + "\n\n"
                        + body
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "Share note"
                )
        );
    }

    // =========================================================
    // GLASS HELPERS
    // =========================================================

    private LinearLayout glassCard(
            int color,
            int radius
    ) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setBackground(
                glass(
                        color,
                        radius,
                        BORDER
                )
        );

        layout.setElevation(
                dp(3)
        );

        return layout;
    }

    private GradientDrawable glass(
            int color,
            int radius,
            int stroke
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);

        drawable.setCornerRadius(
                dp(radius)
        );

        drawable.setStroke(
                dp(1),
                stroke
        );

        return drawable;
    }

    private TextView label(
            String value,
            float size,
            int color
    ) {

        TextView view =
                new TextView(this);

        view.setText(value);

        view.setTextSize(size);

        view.setTextColor(color);

        view.setIncludeFontPadding(false);

        return view;
    }

    private TextView glassButton(
            String value,
            float size
    ) {

        TextView view =
                label(
                        value,
                        size,
                        WHITE
                );

        view.setGravity(
                Gravity.CENTER
        );

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        view.setBackground(
                glass(
                        GLASS_3,
                        26,
                        BORDER
                )
        );

        view.setElevation(
                dp(4)
        );

        return view;
    }

    private TextView gradientButton(
            String value,
            float size
    ) {

        TextView view =
                label(
                        value,
                        size,
                        WHITE
                );

        view.setGravity(
                Gravity.CENTER
        );

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable gradient =
                new GradientDrawable(
                        GradientDrawable
                                .Orientation
                                .LEFT_RIGHT,
                        new int[]{
                                BLUE,
                                PURPLE
                        }
                );

        gradient.setCornerRadius(
                dp(29)
        );

        gradient.setStroke(
                dp(1),
                Color.rgb(
                        106,
                        167,
                        255
                )
        );

        view.setBackground(
                gradient
        );

        view.setElevation(
                dp(5)
        );

        return view;
    }

    // =========================================================
    // SPACING
    // =========================================================

    private void space(
            int value
    ) {

        View spacer =
                new View(this);

        root.addView(
                spacer,
                new LinearLayout.LayoutParams(
                        1,
                        dp(value)
                )
        );
    }

    // =========================================================
    // BOUNCE
    // =========================================================

    private void bounce(
            View view
    ) {

        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(70)
                .withEndAction(
                        () ->
                                view.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start()
                )
                .start();
    }
}
