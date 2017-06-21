package me.jakemoritz.tasking.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import me.jakemoritz.tasking.R;

public class AboutFragment extends Fragment {

    public AboutFragment() {

    }

    public static AboutFragment newInstance(){
        AboutFragment fragment = new AboutFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View aboutFragmentLayout = inflater.inflate(R.layout.fragment_about, container, false);

        ListView libraryListView = (ListView) aboutFragmentLayout.findViewById(R.id.library_list);
        libraryListView.setAdapter(new LibraryAdapter(getActivity()));

        return aboutFragmentLayout;
    }

}
