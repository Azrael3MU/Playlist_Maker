package com.example.playlist_maker_main

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

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }

    private lateinit var editText: EditText
    private var currentSearchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val searchRoot = findViewById<LinearLayout>(R.id.searchRoot)
        val backBtn = findViewById<ImageView>(R.id.back_button)
        editText = findViewById(R.id.edit_text_id)
        val clearBtn = findViewById<ImageView>(R.id.clear_btn)

        val searchIcon = ContextCompat.getDrawable(this, R.drawable.search_mini_img)
        val clearIcon = ContextCompat.getDrawable(this, R.drawable.clear_search)

        ViewCompat.setOnApplyWindowInsetsListener(searchRoot) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(top = bars.top)
            insets
        }

        backBtn.setOnClickListener {
            finish()
        }

        val searchTextWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val clear = if (s.isNullOrEmpty()) null else clearIcon
                currentSearchText = s?.toString() ?: ""
                clearBtn.visibility = if (currentSearchText.isEmpty()) ImageView.GONE else ImageView.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }

        editText.addTextChangedListener(searchTextWatcher)

        clearBtn.setOnClickListener {
            editText.text.clear()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString("SEARCH_TEXT", "")
        editText.setText(restoredText)
    }
}
