package code.name.monkey.retromusic.appwidgets;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;
import code.name.monkey.retromusic.Constants;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.appwidgets.base.BaseAppWidget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.glide.palette.BitmapPaletteWrapper;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.service.MusicService;
import code.name.monkey.retromusic.ui.activities.MainActivity;
import code.name.monkey.retromusic.util.RetroUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

public class AppWidgetCard extends BaseAppWidget {

  public static final String NAME = "app_widget_card";

  private static AppWidgetCard mInstance;
  private static int imageSize = 0;
  private static float cardRadius = 0f;
  private Target<BitmapPaletteWrapper> target; // for cancellation

  public static synchronized AppWidgetCard getInstance() {
    if (mInstance == null) {
      mInstance = new AppWidgetCard();
    }
    return mInstance;
  }

  /**
   * Initialize given widgets to default state, where we launch Music on default click and hide
   * actions if service not running.
   */
  protected void defaultAppWidget(final Context context, final int[] appWidgetIds) {
    final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(),
        R.layout.app_widget_card);

    appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
    appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
    appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(
        RetroUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_next_white_24dp,
            MaterialValueHelper.getSecondaryTextColor(context, true)), 1f));
    appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(
        RetroUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_previous_white_24dp,
            MaterialValueHelper.getSecondaryTextColor(context, true)), 1f));
    appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, createBitmap(
        RetroUtil.getTintedVectorDrawable(context, R.drawable.ic_play_arrow_white_24dp,
            MaterialValueHelper.getSecondaryTextColor(context, true)), 1f));

    linkButtons(context, appWidgetView);
    pushUpdate(context, appWidgetIds, appWidgetView);
  }

  /**
   * Update all active widget instances by pushing changes
   */
  public void performUpdate(final MusicService service, final int[] appWidgetIds) {
    final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(),
        R.layout.app_widget_card);

    final boolean isPlaying = service.isPlaying();
    final Song song = service.getCurrentSong();

    // Set the titles and artwork
    if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
      appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
    } else {
      appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
      appWidgetView.setTextViewText(R.id.title, song.title);
      appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song));
    }

    // Set correct drawable for pause state
    int playPauseRes =
        isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
    appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, createBitmap(
        RetroUtil.getTintedVectorDrawable(service, playPauseRes,
            MaterialValueHelper.getSecondaryTextColor(service, true)), 1f));

    // Set prev/next button drawables
    appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(
        RetroUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp,
            MaterialValueHelper.getSecondaryTextColor(service, true)), 1f));
    appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(
        RetroUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp,
            MaterialValueHelper.getSecondaryTextColor(service, true)), 1f));

    // Link actions buttons to intents
    linkButtons(service, appWidgetView);

    if (imageSize == 0) {
      imageSize = service.getResources().getDimensionPixelSize(R.dimen.app_widget_card_image_size);
    }
    if (cardRadius == 0f) {
      cardRadius = service.getResources().getDimension(R.dimen.app_widget_card_radius);
    }

    // Load the album cover async and push the update on completion
    service.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (target != null) {
          Glide.clear(target);
        }
        target = SongGlideRequest.Builder.from(Glide.with(service), song)
            .checkIgnoreMediaStore(service)
            .generatePalette(service).build()
            .centerCrop()
            .into(new SimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
              @Override
              public void onResourceReady(BitmapPaletteWrapper resource,
                  GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                Palette palette = resource.getPalette();
                update(resource.getBitmap(), palette.getVibrantColor(palette
                    .getMutedColor(MaterialValueHelper.getSecondaryTextColor(service, true))));
              }

              @Override
              public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                update(null, MaterialValueHelper.getSecondaryTextColor(service, true));
              }

              private void update(@Nullable Bitmap bitmap, int color) {
                // Set correct drawable for pause state
                int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp
                    : R.drawable.ic_play_arrow_white_24dp;
                appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause,
                    createBitmap(RetroUtil.getTintedVectorDrawable(service, playPauseRes, color), 1f));

                // Set prev/next button drawables
                appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(
                    RetroUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp,
                        color), 1f));
                appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(
                    RetroUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp,
                        color), 1f));

                final Drawable image = getAlbumArtDrawable(service.getResources(), bitmap);
                final Bitmap roundedBitmap = createRoundedBitmap(image, imageSize, imageSize,
                    cardRadius, 0, cardRadius, 0);
                appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap);

                pushUpdate(service, appWidgetIds, appWidgetView);
              }
            });
      }
    });
  }

  /**
   * Link up various button actions using {@link PendingIntent}.
   */
  private void linkButtons(final Context context, final RemoteViews views) {
    Intent action;
    PendingIntent pendingIntent;

    final ComponentName serviceName = new ComponentName(context, MusicService.class);

    // Home
    action = new Intent(context, MainActivity.class);
    action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
    views.setOnClickPendingIntent(R.id.image, pendingIntent);
    views.setOnClickPendingIntent(R.id.media_titles, pendingIntent);

    // Previous track
    pendingIntent = buildPendingIntent(context, Constants.ACTION_REWIND, serviceName);
    views.setOnClickPendingIntent(R.id.button_prev, pendingIntent);

    // Play and pause
    pendingIntent = buildPendingIntent(context, Constants.ACTION_TOGGLE_PAUSE, serviceName);
    views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent);

    // Next track
    pendingIntent = buildPendingIntent(context, Constants.ACTION_SKIP, serviceName);
    views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
  }
}
