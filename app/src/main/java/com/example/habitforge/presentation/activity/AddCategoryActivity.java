package com.example.habitforge.presentation.activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.habitforge.R;


import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.service.CategoryService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCategoryActivity extends AppCompatActivity {
    private TextInputEditText etName;
    private TextInputLayout tilName;
    private Button btnSave;
    private CategoryService categoryService;
    private String selectedColor; // sada koristimo samo ovo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String editId = getIntent().getStringExtra("editCategoryId");
        if (editId != null) {
            categoryService.getCategoryById(editId, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Category cat = task.getResult();
                    etName.setText(cat.getName());
                    selectedColor = cat.getColor();
                }
            });
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        categoryService = new CategoryService(this);

        tilName = findViewById(R.id.tilCategoryName);
        etName = findViewById(R.id.etCategoryName);
        btnSave = findViewById(R.id.btnSaveCategory);

        // 🔹 Osnovne boje
        // 🔹 Osnovne boje
        Map<Integer, String> colorMap = new HashMap<>();
        colorMap.put(R.id.colorRed, "#FF0000");
        colorMap.put(R.id.colorBlue, "#2196F3");
        colorMap.put(R.id.colorGreen, "#4CAF50");
        colorMap.put(R.id.colorYellow, "#FFEB3B");
        colorMap.put(R.id.colorPurple, "#9C27B0");
        colorMap.put(R.id.colorOrange, "#FF9800");
        colorMap.put(R.id.colorTeal, "#009688");
        colorMap.put(R.id.colorPink, "#E91E63");
        colorMap.put(R.id.colorBrown, "#795548");
        colorMap.put(R.id.colorGray, "#9E9E9E");
        colorMap.put(R.id.colorBlack, "#000000");
        colorMap.put(R.id.colorWhite, "#FFFFFF");

        LinearLayout paletteExtra = findViewById(R.id.colorPaletteExtra);

// 🔸 Klik za osnovne boje
        for (Map.Entry<Integer, String> entry : colorMap.entrySet()) {
            View colorView = findViewById(entry.getKey());
            if (colorView != null) {
                colorView.setOnClickListener(v -> {
                    // Ako klikne istu boju → deselect
                    if (v.isSelected()) {
                        v.setSelected(false);
                        selectedColor = null;
                        return;
                    }

                    // Inače, deselectuj sve u oba reda
                    for (Integer id : colorMap.keySet()) {
                        View other = findViewById(id);
                        if (other != null) other.setSelected(false);
                    }
                    for (int i = 0; i < paletteExtra.getChildCount(); i++) {
                        paletteExtra.getChildAt(i).setSelected(false);
                    }

                    // Označi novu boju
                    v.setSelected(true);
                    selectedColor = entry.getValue();
                });
            }
        }


        // 🔹 Dodatne boje
        String[] extraColors = {
                "#00BCD4", // Cyan
                "#FFC107", // Amber
                "#8BC34A", // Light Green
                "#FF5722", // Deep Orange
                "#607D8B", // Blue Gray
                "#673AB7", // Deep Purple
                "#CDDC39", // Lime
                "#3F51B5"  // Indigo
        };

        ImageView moreBtn = findViewById(R.id.colorMore);
        TextView tvMoreColors = findViewById(R.id.tvMoreColors);
        HorizontalScrollView scrollExtra = findViewById(R.id.colorScrollExtra);
//        LinearLayout paletteExtra = findViewById(R.id.colorPaletteExtra);

        // Toggle stanje
        final boolean[] extraVisible = {false};

        moreBtn.setOnClickListener(v -> {
            if (!extraVisible[0]) {
                // Prikazujemo dodatne boje
                tvMoreColors.setVisibility(View.VISIBLE);
                scrollExtra.setVisibility(View.VISIBLE);

                if (paletteExtra.getChildCount() == 0) {
                    for (String hex : extraColors) {
                        View newColor = new View(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                        params.setMargins(16, 8, 16, 8);
                        newColor.setLayoutParams(params);
                        newColor.setBackgroundResource(R.drawable.color_circle);
                        newColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hex)));

                        newColor.setOnClickListener(c -> {
                            // Ako klikne istu boju → deselect
                            if (newColor.isSelected()) {
                                newColor.setSelected(false);
                                selectedColor = null;
                                return;
                            }

                            // Deselectuj sve dodatne i osnovne
                            for (int i = 0; i < paletteExtra.getChildCount(); i++) {
                                paletteExtra.getChildAt(i).setSelected(false);
                            }
                            for (Map.Entry<Integer, String> entry2 : colorMap.entrySet()) {
                                View other = findViewById(entry2.getKey());
                                if (other != null) other.setSelected(false);
                            }

                            // Označi novu i zapamti boju
                            newColor.setSelected(true);
                            selectedColor = hex;
                        });
                        paletteExtra.addView(newColor);

                    }
                }

                moreBtn.setImageResource(android.R.drawable.ic_delete);
                extraVisible[0] = true;

            } else {
                // Sakrivamo dodatne boje
                tvMoreColors.setVisibility(View.GONE);
                scrollExtra.setVisibility(View.GONE);
                moreBtn.setImageResource(android.R.drawable.ic_input_add);
                extraVisible[0] = false;
            }
        });

        btnSave.setOnClickListener(v -> validateAndSave());
    }


    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        tilName.setError(null);

        if (name.isEmpty()) {
            tilName.setError("Insert the name of category");
            return;
        }
        if (selectedColor == null || selectedColor.isEmpty()) {
            Toast.makeText(this, "Please select a color", Toast.LENGTH_SHORT).show();
            return;
        }

        String editId = getIntent().getStringExtra("editCategoryId");
        Category category;

        if (editId != null) {
            category = new Category(editId, name, selectedColor);

            try {
                categoryService.updateCategory(category, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Category successfully updated!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error updating: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            String id = UUID.randomUUID().toString();
            category = new Category(id, name, selectedColor);

            try {
                categoryService.addCategory(category, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Category successfully added!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
