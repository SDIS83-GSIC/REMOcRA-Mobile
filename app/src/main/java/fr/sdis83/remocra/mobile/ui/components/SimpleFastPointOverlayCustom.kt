package fr.sdis83.remocra.mobile.ui.components

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import fr.sdis83.remocra.mobile.database.HydrantVisite
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel.HydrantGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions

class SimpleFastPointOverlayCustom(
    pointList: PointAdapter,
    private val listHydrantGeoPoint: List<HydrantGeoPoint>,
    private val style: SimpleFastPointOverlayOptions,
    private val drawableCheck: Drawable,
) : SimpleFastPointOverlay(pointList, style) {

    /**
     * On override la fonction puisque la librairie
     * ne permet pas de dessiner autre chose que des cercles ou des rectangles
     * On peut donc ajouter l'image check si la visite du point d'eau a été faite
     */
    override fun draw(canvas: Canvas?, mapView: MapView?, b: Boolean) {
        val viewBBox: BoundingBox = mapView!!.boundingBox
        if (b) return
        val mPositionPixels = Point()
        val pj = mapView.projection
        for (pt1 in listHydrantGeoPoint) {
            if (pt1.latitude > viewBBox.latSouth && pt1.latitude < viewBBox.latNorth &&
                pt1.longitude > viewBBox.lonWest && pt1.longitude < viewBBox.lonEast
            ) {
                pj.toPixels(pt1, mPositionPixels)
                // style may come individually or from the whole theme setting
                drawPointAt(
                    canvas,
                    mPositionPixels.x.toFloat(),
                    mPositionPixels.y.toFloat(),
                    false,
                    null,
                    style.pointStyle,
                    null,
                    mapView,
                )
                if (pt1.statutVisite == HydrantVisite.HydrantVisiteStatut.TERMINE) {
                    val bitmap = drawableCheck.toBitmap()
                    canvas?.drawBitmap(
                        bitmap,
                        mPositionPixels.x.toFloat() - bitmap.width / 4,
                        mPositionPixels.y.toFloat() - bitmap.height / 2,
                        style.pointStyle,
                    )
                }
            }
        }

        if (selectedPoint != null && listHydrantGeoPoint[selectedPoint] != null &&
            style.selectedPointStyle != null
        ) {
            pj.toPixels(listHydrantGeoPoint[selectedPoint], mPositionPixels)
            if (style.symbol == SimpleFastPointOverlayOptions.Shape.CIRCLE) {
                canvas?.drawCircle(
                    mPositionPixels.x.toFloat(),
                    mPositionPixels.y
                        .toFloat(),
                    style.selectedCircleRadius,
                    style.selectedPointStyle,
                )
            } else {
                canvas?.drawRect(
                    mPositionPixels.x.toFloat() - style.selectedCircleRadius,
                    mPositionPixels.y.toFloat() - style.selectedCircleRadius,
                    mPositionPixels.x.toFloat() + style.selectedCircleRadius,
                    mPositionPixels.y.toFloat() + style.selectedCircleRadius,
                    style.selectedPointStyle,
                )
            }
        }
    }
}
