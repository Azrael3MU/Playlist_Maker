package com.example.playlist_maker_main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val searchRoot = findViewById<LinearLayout>(R.id.searchRoot)
        val backBtn = findViewById<ImageView>(R.id.back_button)
        val editText = findViewById<EditText>(R.id.edit_text_id)

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.search_mini_img)
        val clearIcon = ContextCompat.getDrawable(this, R.drawable.clear_search)

        ViewCompat.setOnApplyWindowInsetsListener(searchRoot) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(
                top = bars.top
            )
            insets
        }


        backBtn.setOnClickListener { finish() }

        val searchTextWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val clearIcon = if (s.isNullOrEmpty()) null else clearIcon
                editText.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, clearIcon, null)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }

        editText.addTextChangedListener(searchTextWatcher)

        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = 2
                val clear = editText.compoundDrawables[drawableRight]
                if (clear != null) {
                    val iconWidth = clear.bounds.width()
                    val clickAreaStart = editText.width - editText.paddingEnd - iconWidth
                    if (event.x > clickAreaStart) {
                        editText.text.clear()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        editText.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null)
    }
}
