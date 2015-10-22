package diego.bezerra.com.touchcall.providers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import diego.bezerra.com.touchcall.Common;
import diego.bezerra.com.touchcall.Contact;
import diego.bezerra.com.touchcall.LogApp;
import diego.bezerra.com.touchcall.R;
import diego.bezerra.com.touchcall.configurationActivity.ConfigurationActivity;

/**
 * Created by diego.bezerra on 18/12/2014.
 */
public class TouchCallWidgetProvider extends AppWidgetProvider {

    private static Toast toast;

    private static List<Integer> updatedWidgetIds = new ArrayList<>();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        try {
            for (int widgetId : appWidgetIds) {
                if (!updatedWidgetIds.contains(Integer.valueOf(widgetId))) {
                    updatedWidgetIds.add(widgetId);
                    LogApp.i(String.valueOf(widgetId));
                    LogApp.i("Update!!");
                    updateWidget(context, widgetId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        LogApp.i("Receiver -- " + intent.getAction());

        if (intent.getExtras() != null) {

            int widgetId = intent.getExtras().getInt(
                    Common.INTENT_WIDGET_PARAM_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                if (intent.getAction().contains(Common.INTENT_UPDATE)) {
                    updateWidget(context, widgetId);
                    showToast(context, context.getResources().getString(R.string.update_toast));
                }
                if (intent.getAction().contains(Common.INTENT_CONFIGURE)) {
                    openConfigurationActivity(context, widgetId);
                }
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences preferences = context.getSharedPreferences(
                Common.PREFERENCES_KEY_ROW_IDS, Context.MODE_PRIVATE);

        for (int widgetId : appWidgetIds) {
            preferences.edit().remove(String.valueOf(widgetId)).apply();
        }
    }

    private void updateWidget(Context context, int mAppWidgetId) {

        try {

            List<Contact> contacts = queryWidgetContacts(context, mAppWidgetId);

            if (contacts != null && contacts.size() > 0) {

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                Common.ProviderClass providerClass = getProviderClass(appWidgetManager, mAppWidgetId);

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                remoteViews.removeAllViews(R.id.mainLayout);

                remoteViews.setOnClickPendingIntent(R.id.update,
                        Common.createBroadcastPendingIntent(context,
                                Common.INTENT_UPDATE, mAppWidgetId, providerClass.getClassObj()));

                remoteViews.setOnClickPendingIntent(R.id.configure,
                        Common.createBroadcastPendingIntent(context,
                                Common.INTENT_CONFIGURE, mAppWidgetId, providerClass.getClassObj()));

                remoteViews.setViewVisibility(R.id.widgetProgressBar, View.VISIBLE);
                appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

                Common.createContactsLayout(contacts, remoteViews, context);
                appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<Contact> queryWidgetContacts(Context context, int mAppWidgetId) {

        List<Contact> contacts = new ArrayList<>();
        String stringRowIds = Common.getStringRowIds(context, mAppWidgetId);

        if (stringRowIds != null) {

            final String SELECTION = String.format("%s IN (%s)",
                    ContactsContract.CommonDataKinds.Phone._ID,
                    stringRowIds);

            Cursor data = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    Common.PROJECTION,
                    SELECTION,
                    null,
                    Common.ORDER);

            List<Contact> contactsJsonList = Common.getContactsJsonList(context, mAppWidgetId);
            while (data.moveToNext()) {
                Contact contact = Common.createContactFromCursorData(data, context, contactsJsonList);
                contacts.add(contact);
            }
            data.close();

            List<String> rowIds = new ArrayList<>
                    (Arrays.asList(stringRowIds.split(Common.PREFERENCES_SEPARATOR)));
            orderByRowId(rowIds, contacts);
        }

        return contacts;
    }

    private void orderByRowId(List<String> rowIds, List<Contact> contacts) {
        final HashMap<String, Integer> order = new HashMap<>();
        for (int i = 0; i < rowIds.size(); i++) {
            String rowId = rowIds.get(i);
            order.put(rowId, i);
        }

        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return order.get(String.valueOf(lhs.getRowId())) - order.get(String.valueOf(rhs.getRowId()));
            }
        });
    }

    private void openConfigurationActivity(Context context, int widgetId) {
        Intent configurationIntent = new Intent(context, ConfigurationActivity.class);
        configurationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        configurationIntent.putExtra(Common.INTENT_IS_CONFIGURATION_PARAM, true);
        configurationIntent.putExtra(Common.INTENT_WIDGET_PARAM_ID, widgetId);
        context.startActivity(configurationIntent);
    }

    private void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }

        toast.show();
    }

    private Common.ProviderClass getProviderClass(AppWidgetManager appWidgetManager, int mAppWidgetId) {
        AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(mAppWidgetId);
        return Common.createProviderClassFromName(appWidgetProviderInfo.provider.getShortClassName());
    }

    protected void setHasAlreadyTouchCallFalse(Context context, String className) {
        SharedPreferences preferences = context.getSharedPreferences(Common.PREFERENCES_HAS_ALREADY,
                Context.MODE_PRIVATE);
        preferences.edit().putBoolean(className, false).apply();
    }
}
