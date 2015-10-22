package diego.bezerra.com.touchcall.configurationActivity.contactsFragment;

import java.util.ArrayList;
import java.util.List;

import diego.bezerra.com.touchcall.Contact;

/**
 * Created by diegobezerrasouza on 24/02/15.
 */
public class MySelectedContactList {

    private Contact[] mContacts;

    public MySelectedContactList(int size) {
        this.mContacts = new Contact[size];
    }

    public int add(Contact contact) {
        int indexAdd = this.size() != 0 ? this.size() - 1 : 0;
        for (int i = 0; i < this.sizeWithNulls(); i++) {
            if (mContacts[i] == null) {
                indexAdd = i;
                break;
            }
        }

        mContacts[indexAdd] = contact;
        return indexAdd;
    }

    public int add(int index, Contact contact) {
        if (index > mContacts.length) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + mContacts.length);
        } else {
            mContacts[index] = contact;
        }
        return index;
    }

    public int remove(Contact contact) {

        int indexToRemove = indexOf(contact);
        if (indexToRemove != -1) {
            mContacts[indexToRemove] = null;
        }
        return indexToRemove;
    }

    public int indexOf(Contact contact) {
        int index = -1;
        for (int i = 0; i < mContacts.length; i++) {
            Contact mContact = mContacts[i];
            if (mContact != null && mContact.equals(contact)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < this.sizeWithNulls(); i++) {
            if (mContacts[i] != null) {
                size++;
            }
        }
        return size;
    }

    public int sizeWithNulls() {
        return mContacts.length;
    }

    public List<Contact> getContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (Contact contact : mContacts) {
            if (contact != null) {
                contacts.add(contact);
            }
        }
        return contacts;
    }

    public Contact get(int position) {
        Contact contact = null;
        if (mContacts.length > position) {
            contact = mContacts[position];
        }
        return contact;
    }

    public void changeContactPlaces(int position_1, int position_2) {
        Contact contactAux = mContacts[position_1];
        mContacts[position_1] = mContacts[position_2];
        mContacts[position_2] = contactAux;
    }
}