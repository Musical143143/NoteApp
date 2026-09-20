package com.rayan.noteapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditorActivity extends Activity {

    private NotesDB db;

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

    private final int BG = 0xFF070A11;
    private final int GLASS = 0xCC151A2A;
    private final int BORDER = 0x554A5578;
    private final int TEXT = 0xFFF4F6FC;
    private final int MUTED = 0xFF969DB3;
    private final int ACCENT = 0xFF6278D8;

    private int dp(float value) {
        return (int)(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(
                BG
        );

        getWindow().setNavigationBarColor(
                BG
        );

        db = new NotesDB(this);

        if (getIntent().hasExtra("note_id")) {

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

    private GradientDrawable glass(
            int color,
            int radius
    ) {

        GradientDrawable d =
                new GradientDrawable();

        d.setColor(color);

        d.setCornerRadius(
                dp(radius)
        );

        d.setStroke(
                dp(1),
                BORDER
        );

        return d;
    }

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

    private void buildUI() {

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(16),
                dp(20),
                dp(16),
                dp(10)
        );

        root.setBackgroundColor(
                BG
        );

        setContentView(root);

        root.setOnApplyWindowInsetsListener(
                (v, insets) -> {

                    int top =
                            insets.getInsets(
                                    WindowInsets.Type.statusBars()
                            ).top;

                    int bottom =
                            insets.getInsets(
                                    WindowInsets.Type.navigationBars()
                            ).bottom;

                    root.setPadding(
                            dp(16),
                            top + dp(8),
                            dp(16),
                            bottom + dp(8)
                    );

                    return insets;
                }
        );

        buildTopBar();

        buildTitle();

        buildToolbar();

        buildBody();

        buildAttachment();

        buildMetadata();

        buildCounter();
    }

    private void buildTopBar() {

        LinearLayout bar =
                new LinearLayout(this);

        bar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        root.addView(
                bar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(64)
                )
        );

        TextView back =
                button(
                        "‹",
                        30
                );

        bar.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(54)
                )
        );

        back.setOnClickListener(
                v -> {

                    if (hasContent()) {
                        save();
                    } else {
                        finish();
                    }
                }
        );

        TextView label =
                text(
                        noteId == -1
                                ? "New note"
                                : "Edit note",
                        17,
                        MUTED
                );

        label.setGravity(
                Gravity.CENTER
        );

        bar.addView(
                label,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView save =
                button(
                        "Save",
                        16
                );

        bar.addView(
                save,
                new LinearLayout.LayoutParams(
                        dp(105),
                        dp(54)
                )
        );

        save.setOnClickListener(
                v -> save()
        );
    }

    private TextView button(
            String value,
            float size
    ) {

        TextView t =
                text(
                        value,
                        size,
                        TEXT
                );

        t.setGravity(
                Gravity.CENTER
        );

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        t.setBackground(
                glass(
                        0xCC181E31,
                        22
                )
        );

        t.setElevation(
                dp(4)
        );

        return t;
    }

    private void buildTitle() {

        titleEdit =
                new EditText(this);

        titleEdit.setHint(
                "Title"
        );

        titleEdit.setTextSize(
                31
        );

        titleEdit.setTextColor(
                TEXT
        );

        titleEdit.setHintTextColor(
                0xFF737A92
        );

        titleEdit.setSingleLine(false);

        titleEdit.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );

        titleEdit.setBackgroundColor(
                0x00000000
        );

        root.addView(
                titleEdit,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );
    }

    private void buildToolbar() {

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        scroll.setHorizontalScrollBarEnabled(
                false
        );

        LinearLayout toolbar =
                new LinearLayout(this);

        toolbar.setPadding(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        scroll.addView(toolbar);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        addTool(
                toolbar,
                "B",
                v -> applyStyle(Typeface.BOLD)
        );

        addTool(
                toolbar,
                "I",
                v -> applyStyle(Typeface.ITALIC)
        );

        addTool(
                toolbar,
                "U",
                v -> applyUnderline()
        );

        addTool(
                toolbar,
                "S",
                v -> applyStrike()
        );

        addTool(
                toolbar,
                "H1",
                v -> insertAtCursor("# ")
        );

        addTool(
                toolbar,
                "•",
                v -> insertAtCursor("• ")
        );

        addTool(
                toolbar,
                "1.",
                v -> insertAtCursor("1. ")
        );

        addTool(
                toolbar,
                "☐",
                v -> insertAtCursor("☐ ")
        );

        addTool(
                toolbar,
                "↶",
                v -> undo()
        );

        addTool(
                toolbar,
                "↷",
                v -> redo()
        );

        addTool(
                toolbar,
                "📎",
                v -> chooseAttachment()
        );

        addTool(
                toolbar,
                "↗",
                v -> shareNote()
        );
    }

    private void addTool(
            LinearLayout parent,
            String value,
            View.OnClickListener listener
    ) {

        TextView t =
                button(
                        value,
                        16
                );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        dp(54),
                        dp(50)
                );

        p.rightMargin =
                dp(6);

        parent.addView(
                t,
                p
        );

        t.setOnClickListener(
                listener
        );
    }

    private void buildBody() {

        bodyEdit =
                new EditText(this);

        bodyEdit.setHint(
                "Start writing..."
        );

        bodyEdit.setTextSize(
                19
        );

        bodyEdit.setTextColor(
                TEXT
        );

        bodyEdit.setHintTextColor(
                0xFF747B91
        );

        bodyEdit.setGravity(
                Gravity.TOP | Gravity.START
        );

        bodyEdit.setPadding(
                dp(10),
                dp(12),
                dp(10),
                dp(12)
        );

        bodyEdit.setBackground(
                glass(
                        0x44151A2A,
                        24
                )
        );

        root.addView(
                bodyEdit,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        bodyEdit.addTextChangedListener(
                new android.text.TextWatcher() {

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

    private void buildAttachment() {

        attachmentText =
                text(
                        "",
                        14,
                        0xFFBBD1FF
                );

        attachmentText.setGravity(
                Gravity.CENTER_VERTICAL
        );

        attachmentText.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );

        attachmentText.setBackground(
                glass(
                        0xAA182039,
                        18
                )
        );

        attachmentText.setVisibility(
                View.GONE
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                );

        p.topMargin = dp(8);

        root.addView(
                attachmentText,
                p
        );
    }

    private void buildMetadata() {

        LinearLayout row =
                new LinearLayout(this);

        row.setPadding(
                0,
                dp(8),
                0,
                0
        );

        folderEdit =
                metadataEdit(
                        "Folder"
                );

        tagsEdit =
                metadataEdit(
                        "Tags"
                );

        row.addView(
                folderEdit,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        p.leftMargin =
                dp(8);

        row.addView(
                tagsEdit,
                p
        );

        root.addView(row);
    }

    private EditText metadataEdit(
            String hint
    ) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(14);

        e.setSingleLine(true);

        e.setTextColor(TEXT);

        e.setHintTextColor(
                MUTED
        );

        e.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        e.setBackground(
                glass(
                        0xAA151A2A,
                        20
                )
        );

        return e;
    }

    private void buildCounter() {

        countText =
                text(
                        "",
                        12,
                        MUTED
                );

        countText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                countText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(34)
                )
        );
    }

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

    private boolean hasContent() {

        return !titleEdit.getText()
                .toString()
                .trim()
                .isEmpty()
                ||
                !bodyEdit.getText()
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
                titleEdit.getText()
                        .toString()
                        .trim();

        String folder =
                folderEdit.getText()
                        .toString()
                        .trim();

        String tags =
                tagsEdit.getText()
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

    private void autosave() {

        if (noteId == -1 ||
                !hasContent()) {
            return;
        }

        String title =
                titleEdit.getText()
                        .toString()
                        .trim();

        String folder =
                folderEdit.getText()
                        .toString()
                        .trim();

        String tags =
                tagsEdit.getText()
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

    private void updateCount() {

        if (countText == null ||
                bodyEdit == null) {
            return;
        }

        String s =
                bodyEdit.getText()
                        .toString()
                        .trim();

        int chars =
                s.length();

        int words =
                s.isEmpty()
                        ? 0
                        : s.split("\\s+").length;

        countText.setText(
                words +
                        " words  •  " +
                        chars +
                        " characters"
        );
    }

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

        bodyEdit.getText().setSpan(
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

        bodyEdit.getText().setSpan(
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

        bodyEdit.getText().setSpan(
                new StrikethroughSpan(),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }

    private void insertAtCursor(
            String value
    ) {

        int pos =
                bodyEdit.getSelectionStart();

        if (pos < 0) {
            pos = bodyEdit.length();
        }

        bodyEdit.getText().insert(
                pos,
                value
        );
    }

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

            attachmentText.setVisibility(
                    View.GONE
            );

            return;
        }

        attachmentText.setVisibility(
                View.VISIBLE
        );

        attachmentText.setText(
                "📎  Attachment added     ›"
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

    private void shareNote() {

        String title =
                titleEdit.getText()
                        .toString();

        String body =
                bodyEdit.getText()
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
