package com.example.habitforge.presentation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.habitforge.application.model.Category;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Category> {

    public CategorySpinnerAdapter(Context context, List<Category> categories) {
        super(context, android.R.layout.simple_spinner_item, categories);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        Category category = getItem(position);
        view.setText(category.getName());
        try {
            view.setTextColor(Color.parseColor(category.getColor()));
        } catch (Exception e) {
            view.setTextColor(Color.BLACK);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        Category category = getItem(position);
        view.setText(category.getName());
        try {
            view.setTextColor(Color.parseColor(category.getColor()));
        } catch (Exception e) {
            view.setTextColor(Color.BLACK);
        }
        return view;
    }
}
