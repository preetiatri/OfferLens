package com.offerlens.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Locally defined icons to avoid importing the entire material-icons-extended library (saves ~2MB)

val Icons.Filled.Visibility: ImageVector
    get() = if (_visibility != null) _visibility!! else {
        _visibility = materialIcon(name = "Filled.Visibility") {
            materialPath {
                moveTo(12.0f, 4.5f)
                curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        _visibility!!
    }
private var _visibility: ImageVector? = null

val Icons.Filled.VisibilityOff: ImageVector
    get() = if (_visibilityOff != null) _visibilityOff!! else {
        _visibilityOff = materialIcon(name = "Filled.VisibilityOff") {
            materialPath {
                moveTo(12.0f, 7.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                curveToRelative(0.0f, 0.64f, -0.13f, 1.25f, -0.36f, 1.81f)
                lineToRelative(2.9f, 2.9f)
                curveTo(19.83f, 15.34f, 20.0f, 13.73f, 20.0f, 12.0f)
                curveToRelative(0.0f, -4.42f, -3.58f, -8.0f, -8.0f, -8.0f)
                curveToRelative(-1.73f, 0.0f, -3.34f, 0.57f, -4.68f, 1.55f)
                lineToRelative(2.89f, 2.89f)
                curveToRelative(0.54f, -0.25f, 1.14f, -0.44f, 1.79f, -0.44f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                curveToRelative(0.0f, -0.65f, 0.19f, -1.26f, 0.44f, -1.79f)
                lineToRelative(-1.46f, -1.46f)
                curveTo(4.45f, 9.92f, 3.51f, 11.23f, 3.06f, 12.67f)
                curveTo(4.46f, 16.03f, 7.85f, 18.5f, 12.0f, 18.5f)
                curveToRelative(1.58f, 0.0f, 3.08f, -0.36f, 4.47f, -1.02f)
                lineToRelative(-1.42f, -1.42f)
                curveTo(14.28f, 16.73f, 13.16f, 17.0f, 12.0f, 17.0f)
                close()
                moveTo(2.26f, 4.26f)
                lineTo(1.0f, 5.52f)
                lineToRelative(2.28f, 2.28f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.13f, 9.07f, 1.34f, 10.48f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                curveToRelative(1.55f, 0.0f, 3.03f, -0.3f, 4.38f, -0.84f)
                lineToRelative(0.42f, 0.42f)
                lineToRelative(2.94f, 2.94f)
                lineToRelative(1.26f, -1.27f)
                lineTo(2.26f, 4.26f)
                close()
            }
        }
        _visibilityOff!!
    }
private var _visibilityOff: ImageVector? = null

val Icons.Filled.ContentCopy: ImageVector
    get() = if (_contentCopy != null) _contentCopy!! else {
        _contentCopy = materialIcon(name = "Filled.ContentCopy") {
            materialPath {
                moveTo(16.0f, 1.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 1.0f, 2.0f, 1.9f, 2.0f, 3.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(12.0f)
                verticalLineTo(1.0f)
                close()
                moveTo(19.0f, 5.0f)
                horizontalLineTo(8.0f)
                curveTo(6.9f, 5.0f, 6.0f, 5.9f, 6.0f, 7.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(11.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(7.0f)
                curveTo(21.0f, 5.9f, 20.1f, 5.0f, 19.0f, 5.0f)
                close()
                moveTo(19.0f, 21.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(11.0f)
                verticalLineTo(21.0f)
                close()
            }
        }
        _contentCopy!!
    }
private var _contentCopy: ImageVector? = null

val Icons.Filled.Tune: ImageVector
    get() = if (_tune != null) _tune!! else {
        _tune = materialIcon(name = "Filled.Tune") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(3.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(13.0f, 21.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineTo(13.0f)
                close()
                moveTo(7.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(21.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(21.0f)
                close()
                moveTo(15.0f, 9.0f)
                horizontalLineToRelative(6.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(-6.0f)
                verticalLineTo(9.0f)
                close()
            }
        }
        _tune!!
    }
private var _tune: ImageVector? = null

val Icons.Filled.OpenInNew: ImageVector
    get() = if (_openInNew != null) _openInNew!! else {
        _openInNew = materialIcon(name = "Filled.OpenInNew") {
            materialPath {
                moveTo(19.0f, 19.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(14.0f, 3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.59f)
                lineTo(8.86f, 13.73f)
                lineToRelative(1.41f, 1.41f)
                lineTo(19.0f, 6.41f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(14.0f)
                close()
            }
        }
        _openInNew!!
    }
private var _openInNew: ImageVector? = null

val Icons.Filled.Link: ImageVector
    get() = if (_link != null) _link!! else {
        _link = materialIcon(name = "Filled.Link") {
            materialPath {
                moveTo(3.9f, 12.0f)
                curveToRelative(0.0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-1.9f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.71f, 0.0f, -3.1f, -1.39f, -3.1f, -3.1f)
                close()
                moveTo(8.0f, 13.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(17.0f, 7.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(1.9f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.71f, 0.0f, 3.1f, 1.39f, 3.1f, 3.1f)
                reflectiveCurveToRelative(-1.39f, 3.1f, -3.1f, 3.1f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(17.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                reflectiveCurveToRelative(-2.24f, -5.0f, -5.0f, -5.0f)
                close()
            }
        }
        _link!!
    }
private var _link: ImageVector? = null

val Icons.Filled.Diamond: ImageVector
    get() = if (_diamond != null) _diamond!! else {
        _diamond = materialIcon(name = "Filled.Diamond") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                lineTo(2.0f, 9.0f)
                lineToRelative(10.0f, 12.0f)
                lineToRelative(10.0f, -12.0f)
                lineTo(19.0f, 3.0f)
                close()
                moveTo(5.49f, 5.0f)
                horizontalLineToRelative(13.02f)
                lineToRelative(2.17f, 5.0f)
                horizontalLineTo(3.33f)
                lineTo(5.49f, 5.0f)
                close()
            }
        }
        _diamond!!
    }
private var _diamond: ImageVector? = null

val Icons.Filled.Wallet: ImageVector
    get() = if (_wallet != null) _wallet!! else {
        _wallet = materialIcon(name = "Filled.Wallet") {
            materialPath {
                moveTo(21.0f, 7.28f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.28f)
                curveToRelative(0.59f, -0.35f, 1.0f, -0.98f, 1.0f, -1.72f)
                verticalLineTo(9.0f)
                curveTo(22.0f, 8.26f, 21.59f, 7.63f, 21.0f, 7.28f)
                close()
                moveTo(20.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveTo(20.55f, 12.0f, 20.0f, 12.0f)
                close()
            }
        }
        _wallet!!
    }
private var _wallet: ImageVector? = null

val Icons.Filled.Calculate: ImageVector
    get() = if (_calculate != null) _calculate!! else {
        _calculate = materialIcon(name = "Filled.Calculate") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f)
                close()
                moveTo(13.03f, 7.06f)
                lineToRelative(1.41f, -1.41f)
                lineTo(15.86f, 7.06f)
                lineToRelative(1.41f, -1.41f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(1.41f, -1.41f)
                lineTo(13.03f, 7.06f)
                close()
                moveTo(6.25f, 7.72f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineTo(7.72f)
                close()
                moveTo(11.25f, 16.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(16.0f)
                close()
                moveTo(11.25f, 18.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(18.0f, 18.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(18.0f)
                close()
            }
        }
        _calculate!!
    }
private var _calculate: ImageVector? = null
