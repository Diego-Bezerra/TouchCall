package diego.bezerra.com.touchcall.configurationActivity.previewFragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import diego.bezerra.com.touchcall.Common;
import diego.bezerra.com.touchcall.Contact;
import diego.bezerra.com.touchcall.R;
import diego.bezerra.com.touchcall.configurationActivity.ConfigurationActivity;
import diego.bezerra.com.touchcall.configurationActivity.contactsFragment.MySelectedContactList;

/**
 * Created by diegobezerrasouza on 23/02/15.
 */
public class PreviewFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ConfigurationActivity parentActivity;

    private ViewGroup fragmentView;

    private PreviewFragmentAdapter previewFragmentAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_preview, null);
        parentActivity = (ConfigurationActivity) getActivity();

        return fragmentView;
    }

    private void loadGridPreview(Common.ProviderClass providerClass) {
        GridView gridView = (GridView) fragmentView.findViewById(R.id.contactsGrid);
        previewFragmentAdapter = new PreviewFragmentAdapter(parentActivity, gridView,
                parentActivity.getSelectedContactsList());

        gridView.setAdapter(previewFragmentAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        if (providerClass.getWidgetSize() == 2) {
            gridView.setNumColumns(2);
            gridView.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 230,
                    parentActivity.getResources().getDisplayMetrics());
        }
    }

    public void updatePreview() {
        Common.ProviderClass providerClass = parentActivity.getProviderClass();
        loadGridPreview(providerClass);
    }

    public void updatePreviewAdapter(MySelectedContactList contactList) {
        previewFragmentAdapter.setContactsList(contactList);
        previewFragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Contact contact = (Contact) parent.getItemAtPosition(position);
        final View view = v;
        if (contact != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.dialog_preview, null);
            final EditText editText = (EditText) dialogView.findViewById(R.id.nameText);
            editText.setText(contact.getName());

            builder.setView(dialogView);
            builder.setPositiveButton(parentActivity.getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newName = editText.getText().toString();
                            contact.setLabel(newName);
                            ((TextView) view.findViewById(R.id.text)).setText(contact.getLabel());
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton(parentActivity.getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.create();
            final AlertDialog alertDialog = builder.show();

            dialogView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.removeSelectedContact(contact);
                    alertDialog.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final int POSITION_TAG = 1;
        ClipData clipData = ClipData.newPlainText(String.valueOf(POSITION_TAG), String.valueOf(position));
        MyDragShadowBuilder myDragShadowBuilder = new MyDragShadowBuilder(view, getActivity());
        view.startDrag(clipData, myDragShadowBuilder, null, 0);

        return false;
    }

    private class MyDragShadowBuilder extends View.DragShadowBuilder {

        private Drawable shadow;

        private View view;


        public MyDragShadowBuilder(View v, Context context) {
            super(v);
            this.view = v;
            view.setDrawingCacheEnabled(true);
            shadow = new BitmapDrawable(context.getResources(), v.getDrawingCache());
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            int width, height;
            width = getView().getWidth() - 50;
            height = getView().getHeight() - 50;

            shadow.setBounds(0, 0, width, height);
            shadow.setAlpha(255);
            shadowSize.set(width, height);
            shadowTouchPoint.set(width / 2, height / 2);

            super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
            view.setDrawingCacheEnabled(false);
        }
    }

}
