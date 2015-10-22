package diego.bezerra.com.touchcall.configurationActivity.contactsFragment;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import diego.bezerra.com.touchcall.Contact;
import diego.bezerra.com.touchcall.R;

/**
 * Created by diego.bezerra on 22/12/2014.
 */
public class ContactsFragmentAdapter extends BaseAdapter implements Filterable {

    private List<Contact> contacts = new ArrayList<>();

    private List<Contact> displayedContacts = new ArrayList<>();

    private Context context;

    private LayoutInflater inflater;

    public ContactsFragmentAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
        this.displayedContacts = contacts;
        this.inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Override
    public int getCount() {
        return displayedContacts.size();
    }

    @Override
    public Contact getItem(int position) {
        return displayedContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        Contact contact = getItem(position);
        if (view == null) {
            view = inflater.inflate(R.layout.contact_item_list, null);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.photo);
        if (contact.getPhotoThumbUri() != null) {
            try {

                InputStream inputStream = context.getContentResolver().openInputStream(contact.getPhotoThumbUri());
                imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream, null, null));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                imageView.setImageResource(Contact.DEFAULT_IMAGE_USER_ID);
            }
        } else {
            imageView.setImageResource(Contact.DEFAULT_IMAGE_USER_ID);
        }

        ((TextView) view.findViewById(R.id.name)).setText(contact.getName());
        ((TextView) view.findViewById(R.id.phoneNumber)).setText(contact.getPhoneNumber());
        ((CheckBox) view.findViewById(R.id.selected)).setChecked(contact.isSelected());
        setListItemBackColorBySelection(contact.isSelected(), view);

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<Contact> filteredContacts = new ArrayList<>();

                if (!constraint.equals("")) {
                    for (Contact contact : contacts) {
                        if (contact.getName().trim().toLowerCase()
                                .startsWith(constraint.toString().toLowerCase())) {
                            filteredContacts.add(contact);
                        }
                    }
                } else {
                    filteredContacts = contacts;
                }

                filterResults.values = filteredContacts;
                filterResults.count = filteredContacts.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                displayedContacts = (List<Contact>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public List<Contact> getDisplayedContacts() {
        return displayedContacts;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    private void setListItemBackColorBySelection(boolean checked, View view) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.contact_item_list);
        ColorDrawable drawable = (ColorDrawable) viewGroup.getBackground();
        int colorId = drawable.getColor();

        if (checked) {
            if (colorId != R.color.black_more_transparent)
                viewGroup.setBackgroundColor(context.getResources().getColor(R.color.black_more_transparent));
        } else {
            if (colorId != R.color.black_more_transparent)
                viewGroup.setBackgroundColor(context.getResources().getColor(R.color.gray_contact_item));
        }
    }
}
