package diego.bezerra.com.touchcall.configurationActivity.previewFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import diego.bezerra.com.touchcall.BitmapUtil;
import diego.bezerra.com.touchcall.Contact;
import diego.bezerra.com.touchcall.R;
import diego.bezerra.com.touchcall.configurationActivity.ConfigurationActivity;
import diego.bezerra.com.touchcall.configurationActivity.contactsFragment.MySelectedContactList;

/**
 * Created by diegobezerrasouza on 10/03/15.
 */
public class PreviewFragmentAdapter extends BaseAdapter {

    private MySelectedContactList contactsList;

    private ConfigurationActivity context;

    private GridView gridView;

    private Handler handler;

    public PreviewFragmentAdapter(ConfigurationActivity context, GridView gridView, MySelectedContactList contactsList) {
        this.gridView = gridView;
        setContactsList(contactsList);
        this.context = context;
        this.handler = new Handler();
    }

//    private void setGridInvisible(boolean invisible) {
//        int visibility = invisible ? View.INVISIBLE : View.VISIBLE;
//        this.gridView.setVisibility(visibility);
//    }

    public void setContactsList(MySelectedContactList contactsList) {
        this.contactsList = contactsList;
        // setGridInvisible(contactsList.size() == 0);
    }

    @Override
    public int getCount() {
        return contactsList.sizeWithNulls();
    }

    @Override
    public Object getItem(int position) {
        return contactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        long id = 0;
        if (contactsList.get(position) != null) {
            id = contactsList.get(position).getRowId();
        }

        return id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.contact_button, null);
            convertView.setTag(position);
        }

        final View view = convertView;
        Contact contact = (Contact) getItem(position);
        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        TextView text = (TextView) view.findViewById(R.id.text);
        View mainBack = view.findViewById(R.id.mainBack);

        if (contact != null) {
            Bitmap userBitmap = BitmapUtil.getUserBitmap(contact, context);

            photo.setImageBitmap(userBitmap);
            photo.setVisibility(View.VISIBLE);
            text.setText(contact.getNameOrLabel());
            text.setVisibility(View.VISIBLE);
            mainBack.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            photo.setVisibility(View.INVISIBLE);
            text.setVisibility(View.INVISIBLE);
            mainBack.setBackgroundColor(context.getResources().getColor(R.color.gray_border));
        }

        view.findViewById(R.id.button).setClickable(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setViewLongClickWithDrag(view, String.valueOf(position));
                setViewDragListener(view);
            }
        }, view.getDrawingTime());

        return view;
    }

    private void setViewDragListener(View view) {
        view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    int position_1 = Integer.valueOf(event.getClipData().getItemAt(0).getText().toString());
                    int position_2 = (int) v.getTag();
                    if (position_1 != position_2) {
                        context.getSelectedContactsList().changeContactPlaces(position_1, position_2);
                        contactsList = context.getSelectedContactsList();
                        PreviewFragmentAdapter.this.notifyDataSetChanged();
                    }
                }
                return true;
            }
        });
    }

    private void setViewLongClickWithDrag(View view, final String pos) {
        final View vv = view;
        View button = view.findViewById(R.id.button);
////        button.setOnLongClickListener(new View.OnLongClickListener() {
////            @Override
////            public boolean onLongClick(View v) {
////                PreviewFragment.IS_LONG_PRESS = true;
////                ClipData clipData = ClipData.newPlainText(String.valueOf(POSITION_TAG), pos);
////                MyDragShadowBuilder myDragShadowBuilder = new MyDragShadowBuilder(vv);
////                vv.startDrag(clipData, myDragShadowBuilder, null, 0);
////
////                return false;
////            }
////        });
//        button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    PreviewFragment.IS_LONG_PRESS = false;
//                }
//                return false;
//            }
//        });
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//
////                switch (event.getAction() & MotionEvent.ACTION_MASK) {
////                    case MotionEvent.ACTION_DOWN:
////                        mDownX = event.getX();
////                        mDownY = event.getY();
////                        isOnClick = true;
////                        break;
////                    case MotionEvent.ACTION_CANCEL:
////                    case MotionEvent.ACTION_UP:
////                        isMove = false;
////                        break;
////                    case MotionEvent.ACTION_MOVE:
////                        if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD
////                                || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
////                            isMove = true;
////                            isOnClick = false;
////                        }
////                        break;
////                    default:
////                        break;
////                }
//
//                final View vv = v;
//                GestureDetector.SimpleOnGestureListener simpleOnGestureListener =
//                        new GestureDetector.SimpleOnGestureListener() {
//
//                            @Override
//                            public void onLongPress(MotionEvent e) {
//                                super.onLongPress(e);
//                                if (!isMove) {
//                                    PreviewFragment.IS_LONG_PRESS = true;
//                                    ClipData clipData = ClipData.newPlainText(String.valueOf(POSITION_TAG), pos);
//                                    MyDragShadowBuilder myDragShadowBuilder = new MyDragShadowBuilder(vv);
//                                    vv.startDrag(clipData, myDragShadowBuilder, null, 0);
//                                }
//                            }
//
//                            @Override
//                            public boolean onDown(MotionEvent e) {
//                                PreviewFragment.IS_LONG_PRESS = false;
//                                return super.onDown(e);
//                            }
//                        };
//
//                GestureDetector gestureDetector = new
//                        GestureDetector(context, simpleOnGestureListener);
//                gestureDetector.onTouchEvent(event);
//
//
//                return true;
//            }
//        });
    }

    private class MyDragShadowBuilder extends View.DragShadowBuilder {

        private Drawable shadow;

        private View view;

        public MyDragShadowBuilder(View v) {
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
