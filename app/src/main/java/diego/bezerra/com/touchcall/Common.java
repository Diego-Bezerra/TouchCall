package diego.bezerra.com.touchcall;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import diego.bezerra.com.touchcall.providers.TouchCallWidgetProviderBig;
import diego.bezerra.com.touchcall.providers.TouchCallWidgetProviderBigger;
import diego.bezerra.com.touchcall.providers.TouchCallWidgetProviderMedium;
import diego.bezerra.com.touchcall.providers.TouchCallWidgetProviderSmall;
import diego.bezerra.com.touchcall.providers.TouchCallWidgetProviderSmaller;

/**
 * Created by diegobezerrasouza on 23/02/15.
 */
public class Common {

    public static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    };

    public static final String SELECTION = String.format("%s = ? ",
            ContactsContract.Contacts.HAS_PHONE_NUMBER);

    public static final String[] SELECTION_ARGS = new String[]{"1"};

    public static final String ORDER = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

    public final static String PREFERENCES_KEY_ROW_IDS = "diego.bezerra.com.touchcall.preferences";

    public final static String PREFERENCES_KEY_CONTACTS_JSON = "diego.bezerra.com.touchcall.preferences.contacts";

    public final static String PREFERENCES_SEPARATOR = ",";

    public static final String INTENT_UPDATE = "diego.bezerra.com.touchcall.action.UPDATE";

    public static final String INTENT_CONFIGURE = "diego.bezerra.com.touchcall.action.CONFIGURE";

    public static final String INTENT_WIDGET_PARAM_ID = "diego.bezerra.com.touchcall.widgetid";

    public static final String INTENT_IS_CONFIGURATION_PARAM = "diego.bezerra.com.touchcall.is.configuration";

    public static final String PREFERENCES_HAS_ALREADY = "diego.bezerra.com.touchcall.isAlreadyOnScreenVal";

    private static Toast toast;

    public enum ProviderClass {
        BIGGER("TouchCallWidgetProviderBigger", 16, TouchCallWidgetProviderBigger.class),
        BIG("TouchCallWidgetProviderBig", 12, TouchCallWidgetProviderBig.class),
        MEDIUM("TouchCallWidgetProviderMedium", 8, TouchCallWidgetProviderMedium.class),
        SMALL("TouchCallWidgetProviderSmall", 4, TouchCallWidgetProviderSmall.class),
        SMALLER("TouchCallWidgetProviderSmaller", 2, TouchCallWidgetProviderSmaller.class),
        DEFAULT("TouchCallWidgetProviderSmall", 4, TouchCallWidgetProviderSmall.class);

        private final String className;
        private int widgetSize;
        private final String startName = ".providers.";
        private Class classObj;


        ProviderClass(final String className, int widgetSize, Class classObj) {
            this.className = className;
            this.widgetSize = widgetSize;
            this.classObj = classObj;
        }

        public String getClassName() {
            return className;
        }

        public int getWidgetSize() {
            return widgetSize;
        }

        public Class getClassObj() {
            return classObj;
        }

        @Override
        public String toString() {
            return startName + className;
        }
    }

    public static ProviderClass createProviderClassFromName(String providerName) {

        ProviderClass providerClass;

        if (providerName.equals(ProviderClass.BIGGER.toString())) {
            providerClass = ProviderClass.BIGGER;
        } else if (providerName.equals(ProviderClass.BIG.toString())) {
            providerClass = ProviderClass.BIG;
        } else if (providerName.equals(ProviderClass.MEDIUM.toString())) {
            providerClass = ProviderClass.MEDIUM;
        } else if (providerName.equals(ProviderClass.SMALL.toString())) {
            providerClass = ProviderClass.SMALL;
        } else if (providerName.equals(ProviderClass.SMALLER.toString())) {
            providerClass = ProviderClass.SMALLER;
        } else {
            providerClass = ProviderClass.DEFAULT;
        }

        return providerClass;
    }

    public static String getStringRowIds(Context context, int mAppWidgetId) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_KEY_ROW_IDS, Context.MODE_PRIVATE);

        return preferences.getString(String.valueOf(mAppWidgetId), null);
    }

    public static String getContactJson(Context context, int mAppWidgetId) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_KEY_CONTACTS_JSON, Context.MODE_PRIVATE);

        return preferences.getString(String.valueOf(mAppWidgetId), null);
    }

    public static PendingIntent createBroadcastPendingIntent(Context context, String action,
                                                             int mAppWidgetId, Class providerClass) {

        Intent intent = new Intent(context, providerClass);
        intent.setAction(action);
        intent.putExtra(INTENT_WIDGET_PARAM_ID, mAppWidgetId);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void createContactsLayout(List<Contact> contacts, RemoteViews mainRemoteView, Context context) {

        RemoteViews remoteContactsContainer = new RemoteViews(context.getPackageName(), R.layout.contact_container);
        mainRemoteView.addView(R.id.mainLayout, remoteContactsContainer);

        int cont = 0;
        for (Contact contact : contacts) {

            RemoteViews remoteButton = createButtonRemoteView(contact, context);
            remoteContactsContainer.addView(R.id.container, remoteButton);

            //create another contact container if is no more room
            cont++;
            boolean hasMoreContacts = contacts.size() - cont != 0;
            if (hasMoreContacts && (cont == 4 || cont == 8 || cont == 12 || cont == 16)) {
                remoteContactsContainer = new RemoteViews(context.getPackageName(), R.layout.contact_container);
                mainRemoteView.addView(R.id.mainLayout, remoteContactsContainer);
            }
        }

        mainRemoteView.setViewVisibility(R.id.widgetProgressBar, View.GONE);
    }

    public static RemoteViews createButtonRemoteView(Contact contact, Context context) {

        RemoteViews remoteButton = new RemoteViews(context.getPackageName(), R.layout.contact_button);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
        setClickPendingIntent(remoteButton, callIntent, context, R.id.button);

        Bitmap userBitmap = BitmapUtil.getUserBitmap(contact, context);
        remoteButton.setImageViewBitmap(R.id.photo, userBitmap);

        remoteButton.setTextViewText(R.id.text, contact.getNameOrLabel());

        return remoteButton;
    }

    public static void setClickPendingIntent(RemoteViews remoteButton, Intent intent, Context context, int buttonId) {

        PendingIntent clickPI = PendingIntent
                .getActivity(context, 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        remoteButton.setOnClickPendingIntent(buttonId, clickPI);
    }

    public static Contact createContactFromCursorData(Cursor cursor, Context context, List<Contact> contactsJsonList) {

        long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String contactPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String contactPhotoThumbUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI));
        String contactPhotoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI));

        long rowId = getContactRowId(context, contactId, contactPhoneNumber);
        Contact contact = new Contact(rowId, contactName, contactPhoneNumber);
        setLabelIfHas(contact, contactsJsonList);

        if (contactPhotoThumbUri != null)
            contact.setPhotoThumbUri(Uri.parse(contactPhotoThumbUri));
        if (contactPhotoUri != null)
            contact.setPhotoUri(Uri.parse(contactPhotoUri));

        return contact;
    }

    private static void setLabelIfHas(Contact contact, List<Contact> contactsJsonList) {
        if (contactsJsonList != null) {
            for (Contact preferenceContact : contactsJsonList) {
                if (preferenceContact.getRowId() == contact.getRowId()) {
                    contact.setLabel(preferenceContact.getLabel());
                    break;
                }
            }
        }
    }

    public static List<Contact> getContactsJsonList(Context context, int mAppWidgetId) {
        String contactsJson = getContactJson(context, mAppWidgetId);
        List<Contact> contactsJsonList = new ArrayList<>();
        if (contactsJson != null) {
            Type collectionType = new TypeToken<List<Contact>>() {
            }.getType();
            contactsJsonList = new Gson().fromJson(contactsJson, collectionType);
        }

        return contactsJsonList;
    }

    private static long getContactRowId(Context context, long contactId, String phoneNumber) {

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone._ID},
                String.format("%s = ? AND %s = ?", ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER),
                new String[]{String.valueOf(contactId), phoneNumber},
                null
        );

        long rowId = 0;
        if (cursor.moveToNext()) {
            rowId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
        }
        cursor.close();

        return rowId;
    }

    public static void showToast(String message, Context context) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

}
