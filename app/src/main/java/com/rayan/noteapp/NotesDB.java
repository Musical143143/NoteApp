package com.rayan.noteapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class NotesDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "notes_v2.db";
    private static final int DB_VERSION = 2;

    private final Context context;

    public NotesDB(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE notes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT NOT NULL DEFAULT ''," +
                        "body TEXT NOT NULL DEFAULT ''," +
                        "folder TEXT NOT NULL DEFAULT 'General'," +
                        "tags TEXT NOT NULL DEFAULT ''," +
                        "created INTEGER NOT NULL," +
                        "updated INTEGER NOT NULL," +
                        "pinned INTEGER NOT NULL DEFAULT 0," +
                        "favorite INTEGER NOT NULL DEFAULT 0," +
                        "archived INTEGER NOT NULL DEFAULT 0," +
                        "trash INTEGER NOT NULL DEFAULT 0," +
                        "color INTEGER NOT NULL DEFAULT 0," +
                        "attachment TEXT NOT NULL DEFAULT ''," +
                        "reminder INTEGER NOT NULL DEFAULT 0" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addColumnIfMissing(db, "folder",
                    "TEXT NOT NULL DEFAULT 'General'");
            addColumnIfMissing(db, "tags",
                    "TEXT NOT NULL DEFAULT ''");
            addColumnIfMissing(db, "pinned",
                    "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(db, "favorite",
                    "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(db, "archived",
                    "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(db, "trash",
                    "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(db, "color",
                    "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(db, "attachment",
                    "TEXT NOT NULL DEFAULT ''");
            addColumnIfMissing(db, "reminder",
                    "INTEGER NOT NULL DEFAULT 0");
        }
    }

    private void addColumnIfMissing(
            SQLiteDatabase db,
            String column,
            String definition
    ) {
        try {
            db.execSQL(
                    "ALTER TABLE notes ADD COLUMN "
                            + column + " " + definition
            );
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    // ------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------

    public long insert(
            String title,
            String body,
            String folder,
            String tags
    ) {
        SQLiteDatabase db = getWritableDatabase();

        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put("title", title == null ? "" : title);
        values.put("body", body == null ? "" : body);
        values.put(
                "folder",
                folder == null || folder.trim().isEmpty()
                        ? "General"
                        : folder
        );
        values.put("tags", tags == null ? "" : tags);
        values.put("created", now);
        values.put("updated", now);
        values.put("pinned", 0);
        values.put("favorite", 0);
        values.put("archived", 0);
        values.put("trash", 0);
        values.put("color", 0);
        values.put("attachment", "");
        values.put("reminder", 0);

        return db.insert("notes", null, values);
    }

    // ------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------

    public boolean update(
            long id,
            String title,
            String body,
            String folder,
            String tags
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title == null ? "" : title);
        values.put("body", body == null ? "" : body);
        values.put(
                "folder",
                folder == null || folder.trim().isEmpty()
                        ? "General"
                        : folder
        );
        values.put("tags", tags == null ? "" : tags);
        values.put("updated", System.currentTimeMillis());

        return db.update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // GET SINGLE NOTE
    // ------------------------------------------------------------

    public Note get(long id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(
                "notes",
                null,
                "id=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
            return null;
        } finally {
            c.close();
        }
    }

    // ------------------------------------------------------------
    // QUERY
    // mode:
    // all
    // favorites
    // archive
    // trash
    // pinned
    // folder
    // ------------------------------------------------------------

    public java.util.ArrayList<Note> query(
            String mode,
            String search,
            String sort
    ) {
        SQLiteDatabase db = getReadableDatabase();

        java.util.ArrayList<Note> result =
                new java.util.ArrayList<>();

        StringBuilder selection = new StringBuilder();
        java.util.ArrayList<String> args =
                new java.util.ArrayList<>();

        if (mode == null) {
            mode = "all";
        }

        switch (mode) {

            case "favorites":
                selection.append(
                        "favorite=1 AND trash=0 AND archived=0"
                );
                break;

            case "archive":
            case "archived":
                selection.append(
                        "archived=1 AND trash=0"
                );
                break;

            case "trash":
                selection.append("trash=1");
                break;

            case "pinned":
                selection.append(
                        "pinned=1 AND trash=0 AND archived=0"
                );
                break;

            case "folder":
                selection.append(
                        "trash=0 AND archived=0"
                );
                break;

            case "all":
            default:
                selection.append(
                        "trash=0 AND archived=0"
                );
                break;
        }

        if (search != null && !search.trim().isEmpty()) {

            String searchClause =
                    " AND (" +
                            "title LIKE ? OR " +
                            "body LIKE ? OR " +
                            "tags LIKE ?" +
                            ")";

            selection.append(searchClause);

            String s = "%" + search.trim() + "%";

            args.add(s);
            args.add(s);
            args.add(s);
        }

        String orderBy;

        if ("oldest".equalsIgnoreCase(sort)) {
            orderBy = "created ASC";
        } else if ("title".equalsIgnoreCase(sort)) {
            orderBy = "title COLLATE NOCASE ASC";
        } else if ("updated".equalsIgnoreCase(sort)) {
            orderBy = "updated DESC";
        } else {
            orderBy = "pinned DESC, updated DESC";
        }

        Cursor c = db.query(
                "notes",
                null,
                selection.toString(),
                args.toArray(new String[0]),
                null,
                null,
                orderBy
        );

        try {
            while (c.moveToNext()) {
                result.add(fromCursor(c));
            }
        } finally {
            c.close();
        }

        return result;
    }

    // ------------------------------------------------------------
    // CREATE NOTE OBJECT
    // ------------------------------------------------------------

    private Note fromCursor(Cursor c) {
        Note n = new Note();

        n.id = getLong(c, "id");
        n.title = getString(c, "title");
        n.body = getString(c, "body");
        n.folder = getString(c, "folder");
        n.tags = getString(c, "tags");
        n.created = getLong(c, "created");
        n.updated = getLong(c, "updated");
        n.pinned = getInt(c, "pinned");
        n.favorite = getInt(c, "favorite");
        n.archived = getInt(c, "archived");
        n.trash = getInt(c, "trash");
        n.color = getInt(c, "color");
        n.attachment = getString(c, "attachment");
        n.reminder = getLong(c, "reminder");

        return n;
    }

    private String getString(Cursor c, String column) {
        int index = c.getColumnIndex(column);

        if (index < 0 || c.isNull(index)) {
            return "";
        }

        return c.getString(index);
    }

    private int getInt(Cursor c, String column) {
        int index = c.getColumnIndex(column);

        if (index < 0 || c.isNull(index)) {
            return 0;
        }

        return c.getInt(index);
    }

    private long getLong(Cursor c, String column) {
        int index = c.getColumnIndex(column);

        if (index < 0 || c.isNull(index)) {
            return 0;
        }

        return c.getLong(index);
    }

    // ------------------------------------------------------------
    // PIN
    // ------------------------------------------------------------

    public boolean pin(long id, boolean value) {
        ContentValues values = new ContentValues();
        values.put("pinned", value ? 1 : 0);
        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // FAVORITE
    // ------------------------------------------------------------

    public boolean favorite(long id, boolean value) {
        ContentValues values = new ContentValues();
        values.put("favorite", value ? 1 : 0);
        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // ARCHIVE
    // ------------------------------------------------------------

    public boolean archive(long id, boolean value) {
        ContentValues values = new ContentValues();
        values.put("archived", value ? 1 : 0);

        if (value) {
            values.put("trash", 0);
        }

        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // MOVE TO TRASH
    // ------------------------------------------------------------

    public boolean trash(long id) {
        ContentValues values = new ContentValues();

        values.put("trash", 1);
        values.put("archived", 0);
        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // RESTORE
    // ------------------------------------------------------------

    public boolean restore(long id) {
        ContentValues values = new ContentValues();

        values.put("trash", 0);
        values.put("archived", 0);
        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // DELETE ONE FOREVER
    // ------------------------------------------------------------

    public boolean deleteForever(long id) {
        return getWritableDatabase().delete(
                "notes",
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // DELETE ALL TRASH
    // ------------------------------------------------------------

    public int deleteAllTrash() {
        return getWritableDatabase().delete(
                "notes",
                "trash=1",
                null
        );
    }

    // ------------------------------------------------------------
    // COLOR
    // ------------------------------------------------------------

    public boolean color(long id, int value) {
        ContentValues values = new ContentValues();

        values.put("color", value);
        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // ATTACHMENT
    // ------------------------------------------------------------

    public boolean attachment(long id, String uri) {
        ContentValues values = new ContentValues();

        values.put(
                "attachment",
                uri == null ? "" : uri
        );

        values.put(
                "updated",
                System.currentTimeMillis()
        );

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // REMINDER
    // ------------------------------------------------------------

    public boolean reminder(long id, long time) {
        ContentValues values = new ContentValues();

        values.put("reminder", time);
        values.put("updated", System.currentTimeMillis());

        return getWritableDatabase().update(
                "notes",
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // ------------------------------------------------------------
    // DUPLICATE NOTE
    // ------------------------------------------------------------

    public long duplicate(long id) {
        Note original = get(id);

        if (original == null) {
            return -1;
        }

        String title = original.title;

        if (title == null) {
            title = "";
        }

        if (!title.isEmpty()) {
            title = title + " Copy";
        } else {
            title = "Untitled Copy";
        }

        long newId = insert(
                title,
                original.body,
                original.folder,
                original.tags
        );

        if (newId > 0) {
            ContentValues values = new ContentValues();

            values.put("pinned", original.pinned);
            values.put("favorite", original.favorite);
            values.put("archived", original.archived);
            values.put("color", original.color);
            values.put(
                    "attachment",
                    original.attachment == null
                            ? ""
                            : original.attachment
            );
            values.put("reminder", original.reminder);

            getWritableDatabase().update(
                    "notes",
                    values,
                    "id=?",
                    new String[]{String.valueOf(newId)}
            );
        }

        return newId;
    }

    // ------------------------------------------------------------
    // EXPORT TEXT
    // ------------------------------------------------------------

    public String exportText(long id, String text) {

        try {

            File exportDir =
                    new File(
                            context.getFilesDir(),
                            "exports"
                    );

            if (!exportDir.exists()) {
                if (!exportDir.mkdirs()) {
                    return "";
                }
            }

            File file = new File(
                    exportDir,
                    "note_" + id + ".txt"
            );

            try (FileOutputStream output =
                         new FileOutputStream(file)) {

                output.write(
                        (text == null ? "" : text)
                                .getBytes(StandardCharsets.UTF_8)
                );
            }

            return file.getAbsolutePath();

        } catch (Exception e) {

            e.printStackTrace();

            return "";
        }
    }

    // ------------------------------------------------------------
    // CLOSE
    // ------------------------------------------------------------

    @Override
    public synchronized void close() {
        super.close();
    }
}
