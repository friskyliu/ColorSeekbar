package com.frisky.utils

import android.content.Context

import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ArrayRes
import kotlin.math.abs
import kotlin.math.roundToInt

class ColorSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var minThumbRadius = 16f
    private var maxThumbRadius = 24f
    private var canvasHeight: Int = 60
    private var barHeight: Int = 20
    private var rectf: RectF = RectF()
    private var rectPaint: Paint = Paint()
    private var thumbBorderPaint: Paint = Paint()
    private var thumbPaint: Paint = Paint()
    private var thumbX: Float = 24f
    private var thumbY: Float = (canvasHeight / 2).toFloat()
    private var thumbBorder: Float = 4f
    private var thumbRadius: Float = 16f
    private var thumbBorderColor = Color.BLACK
    private var barCornerRadius: Float = 8f
    private var lastSelectedColor: Int = 0
    private var isTouched = false
    private var colorGradient: LinearGradient? = null
    private var bitmap: Bitmap? = null
    private var canvasBounds = Rect()
    private var lastW = 0
    private var lastH = 0
    private var lastOffsetX = 0
    private var autoSetPosition = true

    var listener: OnColorChangeListener? = null
    var selectedColor: Int = 0
        set(value) {
            field = value
            autoSetPosition = true
            postInvalidate()
        }

    var colorSeeds = intArrayOf(0xFF000000.toInt(), 0xFFFFFF00.toInt())
        set(value) {
            field = value
            autoSetPosition = true
            lastW = 0
            lastH = 0
            lastOffsetX = 0
            colorGradient = null
            bitmap = null
            postInvalidate()
        }

    init {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.ColorSeekBar).run {
                val colorsId = getResourceId(R.styleable.ColorSeekBar_csb_colorSeeds, 0)
                if (colorsId != 0) {
                    colorSeeds = getColorsById(colorsId)
                }
                barCornerRadius = getDimension(R.styleable.ColorSeekBar_csb_cornerRadius, 8f)
                barHeight = getDimension(R.styleable.ColorSeekBar_csb_barHeight, 20f).toInt()
                thumbBorder = getDimension(R.styleable.ColorSeekBar_csb_thumbBorder, 4f)
                minThumbRadius = getDimension(R.styleable.ColorSeekBar_csb_thumbRadius, barHeight * 0.6f)
                maxThumbRadius = getDimension(R.styleable.ColorSeekBar_csb_thumbSelectedRadius, minThumbRadius * 1.2f)
                thumbBorderColor = getColor(R.styleable.ColorSeekBar_csb_thumbBorderColor, Color.BLACK)
                selectedColor = getColor(R.styleable.ColorSeekBar_csb_value, 0)
                recycle()
            }
        }

        autoSetPosition = selectedColor != 0

        rectPaint.isAntiAlias = true

        thumbBorderPaint.isAntiAlias = true
        thumbBorderPaint.color = thumbBorderColor

        thumbPaint.isAntiAlias = true

        thumbRadius = (barHeight / 2.0f).let { if (it < minThumbRadius) minThumbRadius else it }
        canvasHeight = ((thumbRadius + thumbBorder) * 3).toInt()
        thumbY = (canvasHeight / 2).toFloat()
    }

    private fun getColorsById(@ArrayRes id: Int): IntArray {
        if (isInEditMode) {
            val s = context.resources.getStringArray(id)
            val colors = IntArray(s.size)
            for (j in s.indices) {
                colors[j] = Color.parseColor(s[j])
            }
            return colors
        } else {
            val typedArray = context.resources.obtainTypedArray(id)
            val colors = IntArray(typedArray.length())
            for (j in 0 until typedArray.length()) {
                colors[j] = typedArray.getColor(j, Color.BLACK)
            }
            typedArray.recycle()
            return colors
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.getClipBounds(canvasBounds)
        //color bar position
        val barLeft = paddingLeft
        val barRight = canvasBounds.width() - paddingRight
        val barWidth = barRight - barLeft
        val barTop = (canvasHeight - barHeight) / 2
        val barBottom = (canvasHeight + barHeight) / 2
        initInDraw(barRight - barLeft, barBottom - barTop, barLeft)

        //draw color bar
        rectf.set(barLeft.toFloat(), barTop.toFloat(), barRight.toFloat(), barBottom.toFloat())
        canvas.drawRoundRect(rectf, barCornerRadius, barCornerRadius, rectPaint)
        var position = 0
        if (autoSetPosition) {
            if (selectedColor == 0) {
                selectedColor = colorSeeds[0]
            }
            autoSetPosition = false
            position = getPosition(selectedColor)
            thumbX = barLeft.toFloat() + position
        } else {
            position = when {
                thumbX < barLeft -> {
                    thumbX = barLeft.toFloat()
                    0
                }
                thumbX > barRight -> {
                    thumbX = barRight.toFloat()
                    barWidth
                }
                else -> (thumbX - barLeft).roundToInt()
            }
            selectedColor = pickColor(position)
        }

        // draw color bar thumb
//        thumbPaint.color = Color.WHITE
        thumbPaint.color = selectedColor
        if (isTouched) {
            canvas.drawCircle(thumbX, thumbY, maxThumbRadius + thumbBorder, thumbBorderPaint)
            canvas.drawCircle(thumbX, thumbY, maxThumbRadius, thumbPaint)
        } else {
            canvas.drawCircle(thumbX, thumbY, thumbRadius + thumbBorder, thumbBorderPaint)
            canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
        }
    }

    private fun getPosition(color: Int): Int {
        if (color == 0) {
            return 0
        }

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val ret = bitmap?.let { bmp ->
            var nearX = 0
            var nearColorOffset = Integer.MAX_VALUE
            (0 until bmp.width).forEach { x ->
                val c = bmp.getPixel(x, 0)
                val offset =
                    abs(Color.red(c) - r) + abs(Color.green(c) - g) + abs(Color.blue(c) - b)  //颜色差值
                if (offset < nearColorOffset) {
                    nearColorOffset = offset
                    nearX = x
                    if (offset == 0) {
                        return@forEach
                    }
                }
            }
            nearX
        }
        return ret ?: 0
    }

    private fun pickColor(offsetX: Int): Int {
        val ret = bitmap?.let { bmp ->
            when {
                offsetX <= 0 -> colorSeeds[0]
                offsetX >= bmp.width -> colorSeeds[colorSeeds.size - 1]
                else -> bmp.getPixel(offsetX, 0)
            }
        }
        return ret ?: 0xFF000000.toInt()
    }

    private fun initInDraw(w: Int, h: Int, offsetX: Int) {
        if (w <= 0 || h <= 0) {
            return
        }
        if (lastW == w && lastH == h && lastOffsetX == offsetX) {
            return
        }
        if (colorGradient == null) {
            colorGradient = createLinearGradient(w, offsetX, colorSeeds)
            rectPaint.shader = colorGradient
        }

        var tmpBitmap = bitmap
        if (tmpBitmap == null || tmpBitmap.width != w || tmpBitmap.height != h) {
            tmpBitmap = Bitmap.createBitmap(w, 2, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(tmpBitmap)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.shader = createLinearGradient(w, 0, colorSeeds)
            canvas.drawRect(0F, 0F, w.toFloat(), h.toFloat(), paint)
            bitmap = tmpBitmap
        }

        lastW = w
        lastH = h
        lastOffsetX = offsetX
    }

    private fun createLinearGradient(w: Int, offsetX: Int, colors: IntArray): LinearGradient {
        return LinearGradient(offsetX.toFloat(), 0f, (offsetX + w).toFloat(), 0f, colors, null, Shader.TileMode.CLAMP)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            canvasHeight = height
            thumbY = (canvasHeight / 2).toFloat()
        }
        setMeasuredDimension(widthMeasureSpec, canvasHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                listener?.onColorSeekBarDown(selectedColor)
                isTouched = true
                event.x.let {
                    thumbX = it
                    invalidate()
                }
                if (lastSelectedColor != selectedColor) {
                    listener?.onColorChanged(selectedColor)
                    lastSelectedColor = selectedColor
                }
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                event.x.let {
                    thumbX = it
                    invalidate()
                }
                if (lastSelectedColor != selectedColor) {
                    listener?.onColorChanged(selectedColor)
                    lastSelectedColor = selectedColor
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener?.onColorSeekBarUp(selectedColor)
                isTouched = false
                invalidate()
            }
        }
        return true
    }

    interface OnColorChangeListener {
        fun onColorSeekBarDown(color: Int)
        fun onColorChanged(color: Int)
        fun onColorSeekBarUp(color: Int)
    }
}