package diego.bezerra.com.touchcall;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by diego.bezerra on 19/12/2014.
 */
public class BitmapUtil {

    public static Bitmap decodeSampledBitmapFromStream(Context context, Uri photoUri, int reqWidth, int reqHeight) {

        Bitmap bitmap;

        try {

            FileInputStream fileInputStream = context.getContentResolver().openAssetFileDescriptor(photoUri, "r").createInputStream();

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fileInputStream, null, options);

            fileInputStream.close();
            fileInputStream = context.getContentResolver().openAssetFileDescriptor(photoUri, "r").createInputStream();

            // Calculate inSampleSize
            //int densityMultiplier = (int) context.getResources().getDisplayMetrics().density;
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap =  BitmapFactory.decodeStream(fileInputStream, null, options);

        } catch (IOException e) {
            e.printStackTrace();
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user);
        }

        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getUserBitmap(Contact contact, Context context) {

        Bitmap bitmapPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_user);
        if (contact.getPhotoUri() != null) {

//            Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                    new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO_URI, ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI},
//                    ContactsContract.CommonDataKinds.Phone._ID + " = " + contact.getRowId(),
//                    null, null);

            bitmapPhoto = BitmapUtil.decodeSampledBitmapFromStream(context,
                    contact.getPhotoUri(), Contact.USER_MAX_WIDTH_PHOTO, Contact.USER_MAX_HEIGHT_PHOTO);

            //cursor.close();
        }

        return bitmapPhoto;
    }
}
