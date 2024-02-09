package fr.sdis83.remocra.mobile.ui.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import fr.sdis83.remocra.mobile.database.Hydrant
import fr.sdis83.remocra.mobile.database.HydrantVisite
import fr.sdis83.remocra.mobile.utils.TypeHydrantNatureEnum
import fr.sdis83.remocra.mobile.viewmodels.MapViewModel.HydrantGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions

class SimpleFastPointOverlayCustom(
    pointList: PointAdapter,
    val listHydrantGeoPoint: List<HydrantGeoPoint>,
    private val style: SimpleFastPointOverlayOptions,
    private val drawableCheck: Drawable?,
    private val drawableSymboleInconnu: Drawable,
    private val affichageIndispo: Boolean,
    private val affichageSymbolesNormalises: Boolean,
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

                if (!affichageSymbolesNormalises) {
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
                } else {
                    when (pt1.codeNature) {
                        TypeHydrantNatureEnum.PI.getCode() -> {
                            canvas?.drawCircle(
                                mPositionPixels.x.toFloat(),
                                mPositionPixels.y
                                    .toFloat(),
                                style.circleRadius,
                                style.pointStyle,
                            )
                        }
                        TypeHydrantNatureEnum.BI.getCode() -> {
                            canvas?.drawRect(
                                mPositionPixels.x.toFloat() - style.circleRadius,
                                mPositionPixels.y.toFloat() - style.circleRadius,
                                mPositionPixels.x.toFloat() + style.circleRadius,
                                mPositionPixels.y.toFloat() + style.circleRadius,
                                style.pointStyle,
                            )
                        }
                        TypeHydrantNatureEnum.PA.getCode() -> {
                            drawTriangle(
                                mPositionPixels.x - style.circleRadius.toInt(),
                                mPositionPixels.y + style.circleRadius.toInt(),
                                style.circleRadius.toInt() * 2,
                                style.circleRadius.toInt() * 2,
                                style.pointStyle,
                                canvas!!,
                            )
                        }
                        TypeHydrantNatureEnum.CI.getCode() -> {
                            canvas?.drawRect(
                                mPositionPixels.x.toFloat() - style.circleRadius * 2,
                                mPositionPixels.y.toFloat() - style.circleRadius,
                                mPositionPixels.x.toFloat() + style.circleRadius * 2,
                                mPositionPixels.y.toFloat() + style.circleRadius,
                                style.pointStyle,
                            )
                        }
                        // Par défaut, on met un symbole inconnu
                        else -> {
                            canvas?.drawBitmap(
                                drawableSymboleInconnu.toBitmap(style.circleRadius.toInt() * 4, style.circleRadius.toInt() * 4),
                                mPositionPixels.x.toFloat() - style.circleRadius * 2,
                                mPositionPixels.y.toFloat() - style.circleRadius * 2,
                                style.pointStyle,
                            )
                        }
                    }
                }

                // Si le paramètre est vrai, on permet d'afficher les indispo
                if (affichageIndispo) {
                    // Si le PEI est indisponible, on met une croix rouge
                    if (pt1.dispoTerrestre == Hydrant.Disponibilite.INDISPO) {
                        drawSymboleIndispo(canvas, mPositionPixels)
                    }
                }

                if (pt1.statutVisite == HydrantVisite.HydrantVisiteStatut.TERMINE) {
                    val bitmap = drawableCheck!!.toBitmap()
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

    private fun drawSymboleIndispo(canvas: Canvas?, mPositionPixels: Point) {
        val coutour = Paint()
        coutour.strokeWidth = 6f
        coutour.color = Color.WHITE
        canvas?.drawLine(
            mPositionPixels.x.toFloat() - style.circleRadius,
            mPositionPixels.y.toFloat() + style.circleRadius,
            mPositionPixels.x.toFloat() + style.circleRadius,
            mPositionPixels.y.toFloat() - style.circleRadius,
            coutour,
        )

        canvas?.drawLine(
            mPositionPixels.x.toFloat() - style.circleRadius,
            mPositionPixels.y.toFloat() - style.circleRadius,
            mPositionPixels.x.toFloat() + style.circleRadius,
            mPositionPixels.y.toFloat() + style.circleRadius,
            coutour,
        )

        val fill = Paint()
        fill.strokeWidth = 3f
        fill.color = Color.argb(1f, 0.9f, 0.1f, 0.2f)

        canvas?.drawLine(
            mPositionPixels.x.toFloat() - style.circleRadius,
            mPositionPixels.y.toFloat() + style.circleRadius,
            mPositionPixels.x.toFloat() + style.circleRadius,
            mPositionPixels.y.toFloat() - style.circleRadius,
            fill,
        )
        canvas?.drawLine(
            mPositionPixels.x.toFloat() - style.circleRadius,
            mPositionPixels.y.toFloat() - style.circleRadius,
            mPositionPixels.x.toFloat() + style.circleRadius,
            mPositionPixels.y.toFloat() + style.circleRadius,
            fill,
        )
    }

    private fun drawTriangle(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        paint: Paint,
        canvas: Canvas,
    ) {
        val p1 = Point(x, y)
        val pointX = x + width / 2
        val pointY = y - height
        val p2 = Point(pointX, pointY)
        val p3 = Point(x + width, y)
        val path = Path()
        path.moveTo(p1.x.toFloat(), p1.y.toFloat())
        path.lineTo(p2.x.toFloat(), p2.y.toFloat())
        path.lineTo(p3.x.toFloat(), p3.y.toFloat())
        path.close()
        canvas.drawPath(path, paint)
    }
}
