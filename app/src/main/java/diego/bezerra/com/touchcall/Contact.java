package diego.bezerra.com.touchcall;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.text.Collator;
import java.util.Locale;

/**
 * Created by diego.bezerra on 18/12/2014.
 */
public class Contact implements Comparable<Contact> {

    private int id;

    @Expose
    private long rowId;

    private String name;

    @Expose
    private String label;

    private String phoneNumber;

    private Uri photoThumbUri;

    private Uri photoUri;

    private boolean selected;

    private static Collator COT;

    public static final int DEFAULT_IMAGE_USER_ID = R.drawable.ic_user;

    public static final int USER_MAX_HEIGHT_PHOTO = 90;

    public static final int USER_MAX_WIDTH_PHOTO = 90;

    public Contact(long rowId, String name, String phoneNumber) {
        this.rowId = rowId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public String getNameOrLabel() {
        String nameOrLabel = getName();
        if (label != null && !label.trim().equals("")) {
            nameOrLabel = label;
        }
        return nameOrLabel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Uri getPhotoThumbUri() {
        return photoThumbUri;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public static Collator getCot() {
        if (COT == null)
            COT = Collator.getInstance(new Locale("pt", "BR"));

        return COT;
    }

    public void setPhotoThumbUri(Uri photoThumbUri) {
        this.photoThumbUri = photoThumbUri;
    }


    @Override
    public int compareTo(@NonNull Contact another) {
        return getCot().compare(this.name, another.getName());
    }

    @Override
    public boolean equals(Object o) {
        boolean result = super.equals(o);
        if (o != null && o instanceof Contact) {
            result = this.getRowId() == ((Contact)o).getRowId();
        }
        return result;
    }
}
