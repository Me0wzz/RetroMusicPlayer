package code.name.monkey.retromusic.ui.fragments;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import code.name.monkey.retromusic.R;


public enum AlbumCoverStyle {
    NORMAL(R.string.normal, R.drawable.album_cover_card, 0),
    FLAT(R.string.flat, R.drawable.album_cover_square, 1),
    CIRCLE(R.string.circular, R.drawable.album_cover_circle, 2),
    MATERIAL(R.string.material, R.drawable.album_cover_card, 3),
    CARD(R.string.card, R.drawable.album_cover_card, 4),
    CAROUSAL(R.string.carousal, R.drawable.album_cover_carousal, 5),
    FULL(R.string.full, R.drawable.album_cover_full, 6);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    AlbumCoverStyle(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
