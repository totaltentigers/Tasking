package me.jakemoritz.tasking_new.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.dialog.SimpleTextDialogFragment;

class LibraryAdapter extends BaseAdapter implements ListAdapter {

    private final static String APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";

    private Activity activity;
    private String[][] data;

    LibraryAdapter(Activity activity) {
        this.activity = activity;
        this.data = new String[3][3];

        this.data[0] = activity.getResources().getStringArray(R.array.retrofit_attrib);
        this.data[1] = activity.getResources().getStringArray(R.array.picasso_attrib);
        this.data[2] = activity.getResources().getStringArray(R.array.civ_attrib);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        final String[] libraryData = data[position];

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.library_list_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.mLibraryTitle = (TextView) convertView.findViewById(R.id.library_title);
            viewHolder.mLibraryLicense = (TextView) convertView.findViewById(R.id.library_license);

            viewHolder.mLicenseLocal = (TextView) convertView.findViewById(R.id.library_license_local);
            viewHolder.mLicenseWeb = (TextView) convertView.findViewById(R.id.library_license_web);
            viewHolder.mWebsite = (TextView) convertView.findViewById(R.id.library_website);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (libraryData != null) {
            viewHolder.mLibraryTitle.setText(libraryData[0]);
            viewHolder.mLibraryLicense.setText(libraryData[2]);

            viewHolder.mWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri websiteUri = Uri.parse(libraryData[1]);
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, websiteUri);
                    if (websiteIntent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivity(websiteIntent);
                    }
                }
            });

            viewHolder.mLicenseWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri licenseUri = Uri.parse(APACHE_LICENSE_URL);
                    Intent licenseIntent = new Intent(Intent.ACTION_VIEW, licenseUri);
                    if (licenseIntent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivity(licenseIntent);
                    }
                }
            });

            viewHolder.mLicenseLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleTextDialogFragment simpleTextDialogFragment = SimpleTextDialogFragment.newInstance(libraryData[0]);
                    simpleTextDialogFragment.show(activity.getFragmentManager(), SimpleTextDialogFragment.class.getSimpleName());
                }
            });
        }

        return convertView;
    }



    private static class ViewHolder {
        TextView mLibraryTitle;
        TextView mLibraryLicense;
        TextView mWebsite;
        TextView mLicenseLocal;
        TextView mLicenseWeb;
    }
}
