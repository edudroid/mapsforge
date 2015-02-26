/*
 * Copyright 2013-2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package hu.edudroid.mapsforge_sample;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.util.MapViewerTemplate;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.scalebar.ImperialUnitAdapter;
import org.mapsforge.map.scalebar.MetricUnitAdapter;
import org.mapsforge.map.scalebar.NauticalUnitAdapter;

import java.io.IOException;
import java.util.Set;

/**
 * Standard map view with use of Rendertheme V4, loading the render theme from the Android
 * assets folder and showing a configuration menu based on the stylemenu.
 */
public class RenderTheme4 extends MapViewerTemplate implements  XmlRenderThemeMenuCallback {

    protected SharedPreferences sharedPreferences;

    @Override
    protected int getLayoutId() {
        return R.layout.mapviewer;
    }

    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    @Override
    protected MapPosition getDefaultInitialPosition() {
        return new MapPosition(new LatLong(52.517037, 13.38886), (byte) 12);
    }

    @Override
    protected void createLayers() {
        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(this.tileCaches.get(0),
                mapView.getModel().mapViewPosition, getMapFile(), getRenderTheme(), false, true);
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
    }

    @Override
    protected void createControls() {
        setMapScaleBar();
    }

    protected void createTileCaches() {
        boolean threaded = sharedPreferences.getBoolean(SamplesApplication.SETTING_TILECACHE_THREADING, true);
        int queueSize = Integer.parseInt(sharedPreferences.getString(SamplesApplication.SETTING_TILECACHE_QUEUESIZE, "4"));
        boolean persistent = sharedPreferences.getBoolean(SamplesApplication.SETTING_TILECACHE_PERSISTENCE, true);

        this.tileCaches.add(AndroidUtil.createTileCache(this, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(),
                threaded, queueSize, persistent
        ));
    }

    /**
     * @return the map file name to be used
     */
    protected String getMapFileName() {
        return "germany.map";
    }

	/*
	 * Settings related methods.
	 */

    @Override
    protected void createSharedPreferences() {
        super.createSharedPreferences();

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // problem that the first call to getAll() returns nothing, apparently the
        // following two calls have to be made to read all the values correctly
        // http://stackoverflow.com/questions/9310479/how-to-iterate-through-all-keys-of-shared-preferences
        this.sharedPreferences.edit().clear();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
    }

    /**
     * Sets the scale bar from preferences.
     */
    protected void setMapScaleBar() {
        AndroidUtil.setMapScaleBar(this.mapView, MetricUnitAdapter.INSTANCE, null);
    }

    @Override
	protected XmlRenderTheme getRenderTheme() {
		try {
            // Render theme prefix = ""
			return new AssetsRenderTheme(this, "", getRenderThemeFile(), this);
		} catch (IOException e) {
			Log.e(SamplesApplication.TAG, "Render theme failure " + e.toString());
		}
		return null;
	}

	@Override
	public Set<String> getCategories(XmlRenderThemeStyleMenu menuStyle) {
		this.renderThemeStyleMenu = menuStyle;
		String id = this.sharedPreferences.getString(this.renderThemeStyleMenu.getId(),
				this.renderThemeStyleMenu.getDefaultValue());

		XmlRenderThemeStyleLayer baseLayer = this.renderThemeStyleMenu.getLayer(id);
		if (baseLayer == null) {
			Log.w(SamplesApplication.TAG, "Invalid style " + id);
			return null;
		}
		Set<String> result = baseLayer.getCategories();

		// add the categories from overlays that are enabled
		for (XmlRenderThemeStyleLayer overlay : baseLayer.getOverlays()) {
            result.addAll(overlay.getCategories());
		}

		return result;
	}

	protected String getRenderThemeFile() {
		return "renderthemes/rendertheme-v4.xml";
	}

}
