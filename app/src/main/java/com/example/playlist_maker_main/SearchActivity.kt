package com.example.playlist_maker_main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }

    private lateinit var editText: EditText
    private lateinit var clearBtn: ImageView
    private lateinit var backBtn: ImageView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    private lateinit var emptyContainer: View
    private lateinit var errorContainer: View
    private lateinit var retryBtn: View

    private var currentSearchText: String = ""
    private var lastQuery: String = ""
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        // Insets для статус-бара
        val root: View = findViewById(R.id.searchRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top)
            insets
        }

        // View binding
        backBtn = findViewById(R.id.back_button)
        editText = findViewById(R.id.edit_text_id)
        clearBtn = findViewById(R.id.clear_btn)

        recyclerView = findViewById(R.id.tracks_recycler_view)
        emptyContainer = findViewById(R.id.empty_container)
        errorContainer = findViewById(R.id.error_container)
        retryBtn = findViewById(R.id.btn_retry)

        // Recycler
        adapter = TrackAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Навигация назад
        backBtn.setOnClickListener { finish() }

        // Очистка поля
        clearBtn.setOnClickListener {
            // отменим возможный активный поиск
            searchJob?.cancel()

            editText.text.clear()
            editText.clearFocus()
            hideKeyboard(editText)

            // очистим экран
            render()
            lastQuery = ""
        }

        // Показ/скрытие кнопки очистки
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString().orEmpty()
                clearBtn.visibility = if (currentSearchText.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // Клавиша Done запускает поиск
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(editText.text.toString())
                true
            } else false
        }

        // Retry на плейсхолде ошибки
        retryBtn.setOnClickListener {
            if (lastQuery.isNotBlank()) performSearch(lastQuery)
        }
    }

    /** Единый рендер состояний экрана */
    private fun render(
        list: List<Track> = emptyList(),
        showEmpty: Boolean = false,
        showError: Boolean = false
    ) {
        adapter.submitList(list)

        recyclerView.visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
        emptyContainer.visibility = if (showEmpty) View.VISIBLE else View.GONE
        errorContainer.visibility = if (showError) View.VISIBLE else View.GONE
    }

    /** Поиск по iTunes API */
    private fun performSearch(queryRaw: String) {
        val query = queryRaw.trim()
        lastQuery = query

        if (query.isBlank()) {
            render()
            return
        }

        // перед новым запросом скрываем плейсхолды и отменяем старый
        render()
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            try {
                val response = Network.api.search(query)
                val tracks = response.results.map { it.toDomain() }
                if (tracks.isEmpty()) {
                    render(showEmpty = true)
                } else {
                    render(list = tracks)
                }
            } catch (_: Exception) {
                render(showError = true)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Сохранение/восстановление текста поиска
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        editText.setText(restoredText)
        currentSearchText = restoredText
        clearBtn.visibility = if (currentSearchText.isEmpty()) View.GONE else View.VISIBLE
    }
}
