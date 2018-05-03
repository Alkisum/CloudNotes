package com.alkisum.android.cloudnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.adapters.ColorGridAdapter;
import com.alkisum.android.cloudnotes.ui.Color;
import com.alkisum.android.cloudnotes.ui.ColorPref;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog showing the color palette in Settings.
 *
 * @author Alkisum
 * @version 2.4
 * @since 1.1
 */
public class ColorPaletteDialog extends DialogFragment {

    /**
     * Fragment tag for FragmentManager.
     */
    public static final String FRAGMENT_TAG = "color_palette_dialog";

    /**
     * Argument for usage.
     */
    private static final String ARG_USAGE = "usage";

    /**
     * The dialog is used to choose the primary color.
     */
    public static final int PRIMARY_USE = 1;

    /**
     * The dialog is used to choose the accent color.
     */
    public static final int ACCENT_USE = 2;

    /**
     * Use of the dialog.
     */
    private int usage;

    /**
     * List of available colors.
     */
    private final List<Color> colors = new ArrayList<>(
            ColorPref.getColors().values());

    /**
     * GridView showing the colors.
     */
    @BindView(R.id.color_palette_grid)
    GridView grid;

    /**
     * Create new instance of ColorPaletteDialog.
     *
     * @param usage Use of the dialog
     * @return Instance of ColorPaletteDialog
     */
    public static ColorPaletteDialog newInstance(final int usage) {
        ColorPaletteDialog dialog = new ColorPaletteDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_USAGE, usage);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        if (getArguments() == null) {
            throw new IllegalArgumentException(
                    "Argument " + ARG_USAGE + " required");
        }
        usage = getArguments().getInt(ARG_USAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.dialog_color_palette,
                null);

        ButterKnife.bind(this, view);

        String currentColorKey = "";
        if (usage == PRIMARY_USE) {
            currentColorKey = ColorPref.getPrimaryKey(getActivity());
        } else if (usage == ACCENT_USE) {
            currentColorKey = ColorPref.getAccentKey(getActivity());
        }

        ColorGridAdapter adapter = new ColorGridAdapter(getActivity(), colors,
                currentColorKey);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent,
                                    final View view, final int position,
                                    final long id) {
                onItemClicked(position);
            }
        });

        String title = "";
        if (usage == PRIMARY_USE) {
            title = getString(R.string.palette_primary_title);
        } else if (usage == ACCENT_USE) {
            title = getString(R.string.palette_accent_title);
        }
        builder.setView(view)
                .setTitle(title)
                .setNeutralButton(R.string.action_default,
                        onNeutralButtonClicked)
                .setNegativeButton(android.R.string.cancel,
                        onNegativeButtonClicked);
        return builder.create();
    }

    /**
     * Called when a GridView item has been clicked.
     *
     * @param position Position of the item in the list of available colors
     */
    private void onItemClicked(final int position) {
        Color color = colors.get(position);
        if (usage == PRIMARY_USE) {
            ColorPref.setPrimaryColor(getActivity(), color);
        } else if (usage == ACCENT_USE) {
            ColorPref.setAccentColor(getActivity(), color);
        }
        dismiss();
    }

    /**
     * OnClickListener for the neutral button.
     */
    private final DialogInterface.OnClickListener onNeutralButtonClicked =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog,
                                    final int which) {
                    if (usage == PRIMARY_USE) {
                        ColorPref.applyDefaultPrimary(getActivity());
                    } else if (usage == ACCENT_USE) {
                        ColorPref.applyDefaultAccent(getActivity());
                    }
                }
            };

    /**
     * OnClickListener for the negative button.
     */
    private final DialogInterface.OnClickListener onNegativeButtonClicked =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog,
                                    final int which) {
                    getDialog().cancel();
                }
            };

}
