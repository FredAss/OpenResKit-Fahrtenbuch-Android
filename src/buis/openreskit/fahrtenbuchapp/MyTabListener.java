package buis.openreskit.fahrtenbuchapp;

import android.app.ActionBar;

import android.app.ActionBar.Tab;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.gms.maps.MapFragment;


public class MyTabListener<T extends Fragment> implements ActionBar.TabListener {
    private Fragment mFragment;
    private final Activity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    
    /** Constructor used each time a new tab is created.
     * @param activity  The host Activity, used to instantiate the fragment
     * @param tag  The identifier tag for the fragment
     * @param clz  The fragment's Class, used to instantiate the fragment
     */
    public MyTabListener(Activity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
    }

    /* The following are each of the ActionBar.TabListener callbacks */
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = Fragment.instantiate(mActivity, mClass.getName());

            ft.add(R.id.placeholder, mFragment, mTag);
        } else {
            // If it exists, simply attach it in order to show it
            ft.show(mFragment);
        }

        if (mFragment instanceof ListOverview) {
            if (mActivity instanceof MainActivity) {
                MapFragment mapFragment = (MapFragment) mActivity.getFragmentManager().findFragmentById(R.id.mapframe);

                if (mapFragment != null) {
                    if (mapFragment.getMap() != null) {
                        mapFragment.getMap().clear();
                    }
                }
            }
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.hide(mFragment);
        }
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }
}
