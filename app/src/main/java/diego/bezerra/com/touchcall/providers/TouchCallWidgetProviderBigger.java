package diego.bezerra.com.touchcall.providers;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.view.ViewGroup;

import diego.bezerra.com.touchcall.Common;
import diego.bezerra.com.touchcall.configurationActivity.ConfigurationActivity;

/**
 * Created by diego.bezerra on 05/01/2015.
 */
public class TouchCallWidgetProviderBigger extends TouchCallWidgetProvider {

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        setHasAlreadyTouchCallFalse(context, Common.ProviderClass.BIGGER.getClassName());
    }
}
