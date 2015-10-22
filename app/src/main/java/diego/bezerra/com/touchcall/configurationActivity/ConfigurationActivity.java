package diego.bezerra.com.touchcall.configurationActivity;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.widget.BaseAdapter;
import android.widget.ListView;

import diego.bezerra.com.touchcall.Common;
import diego.bezerra.com.touchcall.Contact;
import diego.bezerra.com.touchcall.HasAlreadyTouchCallTypeException;
import diego.bezerra.com.touchcall.R;
import diego.bezerra.com.touchcall.configurationActivity.contactsFragment.ContactsFragment;
import diego.bezerra.com.touchcall.configurationActivity.contactsFragment.ContactsFragmentAdapter;
import diego.bezerra.com.touchcall.configurationActivity.contactsFragment.MySelectedContactList;
import diego.bezerra.com.touchcall.configurationActivity.previewFragment.PreviewFragment;

public class ConfigurationActivity extends FragmentActivity implements SearchView.OnQueryTextListener {

    private MySelectedContactList selectedContactsList;
    private Common.ProviderClass providerClass;
    private boolean isConfiguration = false;
    private int widgetId;
    private MyPageAdapter myPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);

        createWidgetId();
        setProviderClass();
        setIsConfiguration();
        selectedContactsList = new MySelectedContactList(providerClass.getWidgetSize());

        try {
            if (!isConfiguration) validateIfHasAlreadyTouchCallType();
        } catch (HasAlreadyTouchCallTypeException e) {
            finish();
            Common.showToast(String.format(e.getMessage(), providerClass.getWidgetSize()), this);
        }

        configureToolBar();
        createTabs();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void createTabs() {
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        myPageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager.setAdapter(myPageAdapter);
        pager.setCurrentItem(0);
    }

    public void updatePreview() {
        myPageAdapter.getPreviewFragment().updatePreview();
    }

    private void setProviderClass() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(widgetId);
        String providerName = appWidgetProviderInfo.provider.getShortClassName();
        providerClass = Common.createProviderClassFromName(providerName);
    }

    public Common.ProviderClass getProviderClass() {
        return providerClass;
    }

    private void setIsConfiguration() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            isConfiguration = extras.getBoolean(Common.INTENT_IS_CONFIGURATION_PARAM);
        } else {
            isConfiguration = false;
        }
    }

    private void configureToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolBar);
        toolbar.inflateMenu(R.menu.menu_widget_configuration);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(toolbar.getMenu().findItem(R.id.search));
        searchView.setOnQueryTextListener(this);
    }

    private void validateIfHasAlreadyTouchCallType() throws HasAlreadyTouchCallTypeException {
        SharedPreferences preferences = getSharedPreferences(Common.PREFERENCES_HAS_ALREADY, MODE_PRIVATE);
        boolean isAlreadyOnScreen = preferences.getBoolean(providerClass.getClassName(), false);
        if (isAlreadyOnScreen) {
            throw new HasAlreadyTouchCallTypeException(getResources().getString(R.string.hasAlreadyTouchCallTypeMsg));
        }
    }

    public void updatePreviewAdapter() {
        myPageAdapter.getPreviewFragment().
                updatePreviewAdapter(getSelectedContactsList());
    }

    public int getListViewPosition(Contact contact) {
        return ((ContactsFragmentAdapter) myPageAdapter.getContactsFragment().getListView().getAdapter())
                .getDisplayedContacts().indexOf(contact);
    }

    public void removeSelectedContact(Contact contact) {
        int position = getListViewPosition(contact);
        if (position != -1) {
            ListView listView = myPageAdapter.getContactsFragment().getListView();
            listView.performItemClick(
                    listView.getAdapter().getView(position, null, null),
                    position,
                    listView.getAdapter().getItemId(position)
            );
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        } else {
            myPageAdapter.getContactsFragment().removeSelectedContact(contact);
        }

        myPageAdapter.getContactsFragment().setButtonInfo();
    }

    private void createWidgetId() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.getBoolean(Common.INTENT_IS_CONFIGURATION_PARAM)) {
                widgetId = extras.getInt(Common.INTENT_WIDGET_PARAM_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            } else {
                widgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        }
    }

    public MySelectedContactList getSelectedContactsList() {
        return selectedContactsList;
    }

    public boolean isConfiguration() {
        return isConfiguration;
    }

    public int getWidgetId() {
        return widgetId;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        myPageAdapter.getContactsFragment().getContactsFragmentAdapter().getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        myPageAdapter.getContactsFragment().getContactsFragmentAdapter().getFilter().filter(newText);
        return true;
    }

    public class MyPageAdapter extends FragmentPagerAdapter {

        private String[] titles = new String[2];

        private ContactsFragment contactsFragment;

        private PreviewFragment previewFragment;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
            initiateFragments();
        }

        public ContactsFragment getContactsFragment() {
            return contactsFragment;
        }

        public PreviewFragment getPreviewFragment() {
            return previewFragment;
        }

        private void initiateFragments() {
            titles[0] = getResources().getString(R.string.contacts);
            titles[1] = getResources().getString(R.string.preview);
            contactsFragment = new ContactsFragment();
            previewFragment = new PreviewFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return contactsFragment;
                }
                case 1: {
                    return previewFragment;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
