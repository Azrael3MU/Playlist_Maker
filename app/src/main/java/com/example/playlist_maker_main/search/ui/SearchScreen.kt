package com.example.playlist_maker_main.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.playlist_maker_main.App
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.search.domain.model.Track

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit
) {
    val state by viewModel.state.observeAsState(initial = viewModel.state.value ?: SearchScreenState.Idle)

    var searchText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val app = context.applicationContext as App
    val isDarkTheme = app.darkTheme

    val ypBlack = Color(0xFF1A1B22)
    val ypWhite = Color(0xFFFFFFFF)
    val ypGrey = Color(0xFFE6E8EB)
    val ypTextGrey = Color(0xFFAEAFB4)
    val ypBlue = Color(0xFF3772E7)

    val backgroundColor = if (isDarkTheme) ypBlack else ypWhite
    val textColor = if (isDarkTheme) ypWhite else ypBlack
    val searchFieldBgColor = if (isDarkTheme) Color.White else ypGrey
    val searchIconTint = if (isDarkTheme) Color.Black else ypTextGrey

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(top = 14.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(id = R.string.search),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Панель поиска
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(36.dp)
                .background(
                    color = searchFieldBgColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.search_img),
                contentDescription = "Search icon",
                tint = searchIconTint,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (searchText.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.search),
                        color = ypTextGrey,
                        fontSize = 16.sp
                    )
                }

                BasicTextField(
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                        viewModel.onQueryChanged(newText)
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(ypBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.onSearchSubmitted()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (searchText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.clear_search),
                    contentDescription = "Clear",
                    tint = searchIconTint,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable {
                            searchText = ""
                            viewModel.onQueryChanged("")
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val currentState = state) {
                is SearchScreenState.Loading -> {
                    CircularProgressIndicator(
                        color = ypBlue,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is SearchScreenState.Content -> {
                    TrackList(tracks = currentState.tracks, textColor = textColor, onTrackClick = onTrackClick)
                }

                is SearchScreenState.EmptyResult -> {
                    MessageState(
                        imageRes = R.drawable.notfound,
                        text = stringResource(id = R.string.nothing_found),
                        textColor = textColor
                    )
                }

                is SearchScreenState.Error -> {
                    MessageState(
                        imageRes = R.drawable.enternet,
                        text = stringResource(id = R.string.errors_connection) + "\n\n" + stringResource(id = R.string.cheack_connection),
                        textColor = textColor,
                        buttonText = stringResource(id = R.string.update),
                        onButtonClick = { viewModel.onRetry() }
                    )
                }

                is SearchScreenState.History -> {
                    if (currentState.items.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = stringResource(id = R.string.you_search),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(currentState.items) { track ->
                                    TrackItem(track = track, textColor = textColor, onClick = { onTrackClick(track) })
                                }
                            }

                            Button(
                                onClick = { viewModel.onClearHistoryClicked() },
                                colors = ButtonDefaults.buttonColors(containerColor = ypBlue),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 24.dp)
                            ) {
                                Text(text = stringResource(id = R.string.clear_history), color = Color.White)
                            }
                        }
                    }
                }

                is SearchScreenState.Idle -> { }
            }
        }
    }
}

@Composable
fun TrackList(tracks: List<Track>, textColor: Color, onTrackClick: (Track) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks) { track ->
            TrackItem(track = track, textColor = textColor, onClick = { onTrackClick(track) })
        }
    }
}

@Composable
fun MessageState(
    imageRes: Int,
    text: String,
    textColor: Color,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
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
        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3772E7))
            ) {
                Text(text = buttonText, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun TrackItem(track: Track, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.artworkUrl100)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = R.drawable.placeholder),
            error = painterResource(id = R.drawable.placeholder),
            contentDescription = "Обложка трека",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.trackName ?: "",
                fontSize = 16.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.artistName ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFFAEAFB4),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = Color(0xFFAEAFB4)
                )

                Text(
                    text = track.durationStr() ?: "00:00",
                    fontSize = 12.sp,
                    color = Color(0xFFAEAFB4)
                )
            }
        }
    }
}