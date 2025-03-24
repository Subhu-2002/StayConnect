package com.example.stayconnect;

import android.widget.Filter;

import com.example.stayconnect.adapters.AdapterHostel;
import com.example.stayconnect.models.ModelHostel;

import java.util.ArrayList;

public class FilterService extends Filter {

    private AdapterHostel adapter;
    private ArrayList<ModelHostel> filterList;

    public FilterService(AdapterHostel adapter, ArrayList<ModelHostel> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results = new FilterResults();

        if(constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelHostel> filteredModels = new ArrayList<>();
            for(int i = 0; i<filterList.size(); i++){

                if(filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                        filterList.get(i).getHostelName().toUpperCase().contains(constraint)
                ){
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }else{

            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.hostelArrayList = (ArrayList<ModelHostel>) results.values;

        adapter.notifyDataSetChanged();
    }
}
