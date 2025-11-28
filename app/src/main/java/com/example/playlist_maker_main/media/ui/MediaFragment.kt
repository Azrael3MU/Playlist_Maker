package com.example.playlist_maker_main.media.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentMediaBinding
import com.example.playlist_maker_main.media.ui.adapter.MediaPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment(R.layout.fragment_media) {

    private val viewModel: MediaViewModel by viewModel()

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMediaBinding.bind(view)

        val tabLayout: TabLayout = binding.tabLayoutMedia
        val viewPager: ViewPager2 = binding.viewPagerMedia

        viewPager.adapter = MediaPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorites_tracks)
                else -> getString(R.string.playlists)
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
