package com.example.stayconnect;

import android.widget.Filter;

import com.example.stayconnect.adapters.AdapterChats;
import com.example.stayconnect.models.ModelChats;

import java.util.ArrayList;

public class FilterChats extends Filter {

    private AdapterChats adapter;
    private ArrayList<ModelChats> filterList;


    public FilterChats(AdapterChats adapter, ArrayList<ModelChats> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults filterResults = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelChats> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getName().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            filterResults.count = filteredModels.size();
            filterResults.values = filteredModels;
        }else{
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.chatsArrayList = (ArrayList<ModelChats>) results.values;
        adapter.notifyDataSetChanged();
    }
}
