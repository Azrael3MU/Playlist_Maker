package com.example.playlist_maker_main.media.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.media.ui.favorites.FavoritesState
import com.example.playlist_maker_main.media.ui.favorites.FavoritesViewModel
import com.example.playlist_maker_main.media.ui.playlists.PlaylistsViewModel
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.ui.TrackList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaScreen(
    favoritesViewModel: FavoritesViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onNavigateToNewPlaylist: () -> Unit,
    onNavigateToPlayer: (Track) -> Unit,
    onNavigateToPlaylistDetails: (Int) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val isDarkTheme = app.darkTheme

    val ypBlack = Color(0xFF1A1B22)
    val ypWhite = Color(0xFFFFFFFF)
    val ypBlue = Color(0xFF3772E7)

    val backgroundColor = if (isDarkTheme) ypBlack else ypWhite
    val textColor = if (isDarkTheme) ypWhite else ypBlack
    val tabUnselectedColor = textColor

    LaunchedEffect(Unit) {
        favoritesViewModel.fillData()
        playlistsViewModel.fillData()
    }

    val tabs = listOf(
        stringResource(id = R.string.favorites_tracks),
        stringResource(id = R.string.playlists)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 14.dp)
    ) {
        Text(
            text = stringResource(id = R.string.media),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = backgroundColor,
            contentColor = textColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = ypBlue,
                    height = 2.dp
                )
            },
            divider = { }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (pagerState.currentPage == index) textColor else tabUnselectedColor
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> FavoritesTab(
                    viewModel = favoritesViewModel,
                    textColor = textColor,
                    onTrackClick = onNavigateToPlayer
                )
                1 -> PlaylistsTab(
                    viewModel = playlistsViewModel,
                    textColor = textColor,
                    onNewPlaylistClick = onNavigateToNewPlaylist,
                    onPlaylistClick = onNavigateToPlaylistDetails
                )
            }
        }
    }
}

@Composable
fun FavoritesTab(
    viewModel: FavoritesViewModel,
    textColor: Color,
    onTrackClick: (Track) -> Unit
) {
    val state by viewModel.state.observeAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val currentState = state) {
            is FavoritesState.Loading -> {
                CircularProgressIndicator(
                    color = Color(0xFF3772E7),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is FavoritesState.Empty, null -> {
                MediaPlaceholder(
                    imageRes = R.drawable.media_placeholder,
                    text = stringResource(id = R.string.empty_tracks),
                    textColor = textColor
                )
            }
            is FavoritesState.Content -> {
                TrackList(
                    tracks = currentState.tracks,
                    textColor = textColor,
                    onTrackClick = onTrackClick
                )
            }
        }
    }
}

@Composable
fun PlaylistsTab(
    viewModel: PlaylistsViewModel,
    textColor: Color,
    onNewPlaylistClick: () -> Unit,
    onPlaylistClick: (Int) -> Unit
) {
    val playlists by viewModel.playlists.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNewPlaylistClick,
            colors = ButtonDefaults.buttonColors(containerColor = textColor),
            shape = RoundedCornerShape(54.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.new_playlist),
                color = if (textColor == Color.White) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (playlists.isEmpty()) {
            MediaPlaceholder(
                imageRes = R.drawable.media_placeholder,
                text = stringResource(id = R.string.empty_playlists),
                textColor = textColor
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(playlists) { playlist ->
                    PlaylistGridItem(
                        playlist = playlist,
                        textColor = textColor,
                        onClick = { onPlaylistClick(playlist.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaPlaceholder(imageRes: Int, text: String, textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun PlaylistGridItem(playlist: Playlist, textColor: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlist.imagePath?.replace("http://", "https://"))
                .build(),
            placeholder = painterResource(id = R.drawable.placeholder),
            error = painterResource(id = R.drawable.placeholder),
            fallback = painterResource(id = R.drawable.placeholder),
            contentDescription = "Обложка плейлиста",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = playlist.name,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${playlist.tracksCount} треков",
            fontSize = 11.sp,
            color = Color(0xFFAEAFB4),
            maxLines = 1
        )
    }
}