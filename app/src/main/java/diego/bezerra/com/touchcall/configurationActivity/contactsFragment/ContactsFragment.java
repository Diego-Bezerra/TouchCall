package diego.bezerra.com.touchcall.configurationActivity.contactsFragment;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import diego.bezerra.com.touchcall.Common;
import diego.bezerra.com.touchcall.Contact;
import diego.bezerra.com.touchcall.LogApp;
import diego.bezerra.com.touchcall.R;
import diego.bezerra.com.touchcall.WidgetSizeException;
import diego.bezerra.com.touchcall.configurationActivity.ConfigurationActivity;

/**
 * Created by diegobezerrasouza on 23/02/15.
 */
public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView listView;
    private ConfigurationActivity parentActivity;
    private View fragmentView;
    private Button buttonOk;
    private String buttonInfoFormat;
    private ContactsFragmentAdapter contactsFragmentAdapter;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);
        parentActivity = (ConfigurationActivity) getActivity();
        configureContactsListView();
        configureOkButton();
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        return new CursorLoader(parentActivity, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                Common.PROJECTION, Common.SELECTION, Common.SELECTION_ARGS, Common.ORDER);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.getCount() == 0) {
            fragmentView.findViewById(R.id.noContactsText).setVisibility(View.VISIBLE);
        } else {
            List<Contact> contacts = createContactsListFromCursor(data);
            contactsFragmentAdapter = new ContactsFragmentAdapter(getActivity(), contacts);
            listView.setAdapter(contactsFragmentAdapter);
            listView.setOnItemClickListener(this);
            setButtonInfo();
            parentActivity.updatePreview();
        }

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ContactsFragmentAdapter contactsFragmentAdapter = (ContactsFragmentAdapter) parent.getAdapter();
        Contact selectedContact = contactsFragmentAdapter.getItem(position);
        if (selectedContact.isSelected()) {
            removeSelectedContact(selectedContact);
        } else {
            addSelectedContact(selectedContact);
        }

        setListItemBackColorBySelection(selectedContact.isSelected(), view);
        ((CheckBox) view.findViewById(R.id.selected)).setChecked(selectedContact.isSelected());
        setButtonInfo();
        parentActivity.updatePreviewAdapter();
    }

    private void addSelectedContact(Contact selectedContact) {
        if (parentActivity.getSelectedContactsList().size() < parentActivity.getProviderClass().getWidgetSize()) {
            selectedContact.setSelected(true);
            int index = parentActivity.getSelectedContactsList().add(selectedContact);
        }
    }

    public void removeSelectedContact(Contact selectedContact) {
        selectedContact.setSelected(false);
        int index = parentActivity.getSelectedContactsList().remove(selectedContact);
    }

    private void configureContactsListView() {
        listView = (ListView) fragmentView.findViewById(R.id.contactsList);
        listView.setOnItemClickListener(this);
    }

    public ListView getListView() {
        return listView;
    }

    public ContactsFragmentAdapter getContactsFragmentAdapter() {
        return contactsFragmentAdapter;
    }

    private void setListItemBackColorBySelection(boolean checked, View view) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.contact_item_list);
        if (checked) {
            viewGroup.setBackgroundColor(getResources().getColor(R.color.black_more_transparent));
        } else {
            viewGroup.setBackgroundColor(getResources().getColor(R.color.gray_contact_item));
        }
    }

    private void configureOkButton() {
        buttonOk = (Button) fragmentView.findViewById(R.id.btnOk);
        buttonOk.setOnClickListener(this);
        setButtonInfo();
    }

    public List<Contact> createContactsListFromCursor(Cursor cursor) {

        List<Contact> contacts = new ArrayList<>();
        List<String> preferencesContactsRowIds = getPreferencesContactsRowIds();

        if (preferencesContactsRowIds != null) {
            openNullSpacesOnSelectedContacts(preferencesContactsRowIds.size());
        }

        if (cursor != null) {

            List<Contact> contactJsonList = Common.getContactsJsonList(parentActivity, parentActivity.getWidgetId());
            while (cursor.moveToNext()) {

                Contact contact = Common.createContactFromCursorData(cursor, parentActivity, contactJsonList);

                if (preferencesContactsRowIds != null &&
                        preferencesContactsRowIds.indexOf(String.valueOf(contact.getRowId())) != -1) {

                    contact.setSelected(true);
                    int index = preferencesContactsRowIds.indexOf(String.valueOf(contact.getRowId()));
                    parentActivity.getSelectedContactsList().add(index, contact);
                }


                contacts.add(contact);
            }

            cursor.close();
        }


        if (contacts.size() > 0) Collections.sort(contacts);

        return contacts;
    }

    public List<Contact> getPreferencesContacts() {
        List<Contact> contactsList = new ArrayList<>();
        String contactsJson = Common.getContactJson(parentActivity, parentActivity.getWidgetId());
        if (contactsJson != null) {
            Type collectionType = new TypeToken<List<Contact>>() {
            }.getType();
            contactsList = new Gson().fromJson(contactsJson, collectionType);
        }

        return contactsList;
    }

    public List<String> getPreferencesContactsRowIds() {

        List<String> selectedContacts = null;

        if (parentActivity.isConfiguration()) {
            String selectedContactsSrt = Common.getStringRowIds(parentActivity,
                    parentActivity.getWidgetId());

            if (selectedContactsSrt != null) {
                selectedContacts = Arrays.asList(selectedContactsSrt.split(Common.PREFERENCES_SEPARATOR));
            }
        }

        return selectedContacts;
    }

    private void openNullSpacesOnSelectedContacts(int size) {
        for (int i = 0; i < size; i++) {
            parentActivity.getSelectedContactsList().add(null);
        }
    }

    public void setButtonInfo() {

        if (buttonOk != null) {

            if (buttonInfoFormat == null)
                buttonInfoFormat = getResources().getString(R.string.selected_info);

            int missingContacts = Math.abs(parentActivity.getSelectedContactsList().size() -
                    parentActivity.getProviderClass().getWidgetSize());

            if (missingContacts != 0) {

                String buttonInfo = String.format(buttonInfoFormat, missingContacts);
                buttonOk.setText(buttonInfo);

            } else {

                buttonOk.setText(getResources().getString(R.string.ok));
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {

            if (parentActivity.getSelectedContactsList().size() < parentActivity.getProviderClass().getWidgetSize())
                throw new WidgetSizeException(getResources().getString(R.string.widget_size_error));

            SharedPreferences preferences = parentActivity.getSharedPreferences(Common.PREFERENCES_HAS_ALREADY, Context.MODE_PRIVATE);
            preferences.edit().putBoolean(parentActivity.getProviderClass().getClassName(), true).apply();
            LogApp.i(parentActivity.getProviderClass().getClassName() + " - set");

            mountWidget();

        } catch (WidgetSizeException e) {
            Common.showToast(e.getMessage(), parentActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveContactsRowIdsPreferences(int mAppWidgetId) {
        SharedPreferences preferences = parentActivity.getSharedPreferences(Common.PREFERENCES_KEY_ROW_IDS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String contactIds = createStringContactRowIds();
        if (!contactIds.equals("")) {
            editor.putString(String.valueOf(mAppWidgetId), contactIds);
            editor.apply();
        }
    }

    private String createStringContactRowIds() {
        StringBuilder sb = new StringBuilder();

        int cont = 0;
        for (Contact contact : parentActivity.getSelectedContactsList().getContacts()) {
            if (sb.indexOf(String.valueOf(contact.getRowId())) == -1) {
                sb.append(contact.getRowId());
                if (parentActivity.getSelectedContactsList().size() - 1 > cont)
                    sb.append(Common.PREFERENCES_SEPARATOR);
            }
            cont++;
        }
        return sb.toString();
    }

    private void saveContactsJsonPreferences(int mAppWidgetId) {
        SharedPreferences preferences = parentActivity.getSharedPreferences(Common.PREFERENCES_KEY_CONTACTS_JSON,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String contactJson = createContactJson();
        if (contactJson != null) {
            editor.putString(String.valueOf(mAppWidgetId), contactJson);
            editor.apply();
        }
    }

    private String createContactJson() {
        GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        return gsonBuilder.create().toJson(parentActivity.getSelectedContactsList().getContacts());
    }

    private void mountWidget() {

        if (parentActivity.getWidgetId() > 0) {

            int widgetId = parentActivity.getWidgetId();

            saveContactsRowIdsPreferences(widgetId);
            saveContactsJsonPreferences(widgetId);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(parentActivity);

            RemoteViews remoteViews = new RemoteViews(parentActivity.getPackageName(), R.layout.widget_layout);
            remoteViews.removeAllViews(R.id.mainLayout);

            remoteViews.setOnClickPendingIntent(R.id.update,
                    Common.createBroadcastPendingIntent(parentActivity, Common.INTENT_UPDATE, widgetId,
                            parentActivity.getProviderClass().getClassObj()));
            remoteViews.setOnClickPendingIntent(R.id.configure,
                    Common.createBroadcastPendingIntent(parentActivity, Common.INTENT_CONFIGURE, widgetId,
                            parentActivity.getProviderClass().getClassObj()));

            Common.createContactsLayout(parentActivity.getSelectedContactsList().getContacts(), remoteViews, parentActivity);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            parentActivity.setResult(Activity.RESULT_OK, resultValue);
            parentActivity.finish();
        }
    }
}
