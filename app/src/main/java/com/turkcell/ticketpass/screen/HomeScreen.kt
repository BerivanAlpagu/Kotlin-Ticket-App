package com.turkcell.ticketpass.screen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.turkcell.ticketpass.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.turkcell.core.domain.event.Event

@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface (modifier= Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 24.dp)) {
            Text("Yaklaşan Etkinlikler")
            Spacer(Modifier.height(8.dp))

            EventsRow(
                isLoading = state.isEventsLoading,
                error = state.eventsError,
                events = state.events,
                onEventClick = onEventClick
            )


            Spacer(Modifier.height(8.dp))
            Text("Satın Alınmış Biletler")
        }
    }
}

@Composable
private fun EventsRow(
    isLoading: Boolean,
    error: String?,
    events: List<Event>,
    onEventClick: (String) -> Unit
) {
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)){
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Text(error)
        }
        events.isEmpty() -> {
            Text(text="Şimdilik hiç bir etkinlik yok.", style= MaterialTheme.typography.bodyMedium)
        }
        else -> {
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp)) {
                items(items=events, key = {it.id}) { event ->
                    EventCard(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit)
{
    Card(
        onClick = onClick,
        modifier = Modifier.width(260.dp).height(280.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = event.name.take(1).uppercase().ifBlank { "?" },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(event.name)
            Text(event.venue)
            Text(event.description)

        }
    }
}
