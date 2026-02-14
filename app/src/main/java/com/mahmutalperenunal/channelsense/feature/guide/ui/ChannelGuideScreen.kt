package com.mahmutalperenunal.channelsense.feature.guide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.mahmutalperenunal.channelsense.feature.guide.ChannelGuideProvider
import com.mahmutalperenunal.channelsense.feature.guide.model.GuideStep
import com.mahmutalperenunal.channelsense.feature.guide.model.RouterBrand
import com.mahmutalperenunal.channelsense.util.NetworkUtils
import com.mahmutalperenunal.channelsense.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelGuideScreen(
    channel: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var selectedBrand by remember { mutableStateOf(RouterBrand.OTHER) }

    val steps = remember(context, channel, selectedBrand) {
        ChannelGuideProvider.getSteps(context, selectedBrand, channel)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guide_title_channel, channel)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text(
                text = stringResource(R.string.guide_selected_channel, channel),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.guide_select_brand_instructions),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            RouterBrandSelector(
                selectedBrand = selectedBrand,
                onBrandSelected = { brand ->
                    selectedBrand = brand
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { NetworkUtils.openRouterPage(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.guide_open_router_interface))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.guide_step_by_step),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuideStepsList(steps = steps)
        }
    }
}

@Composable
private fun RouterBrandSelector(
    selectedBrand: RouterBrand,
    onBrandSelected: (RouterBrand) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.guide_router_brand_label),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BrandChip(
                brand = RouterBrand.TP_LINK,
                selected = selectedBrand == RouterBrand.TP_LINK,
                onClick = { onBrandSelected(RouterBrand.TP_LINK) }
            )
            BrandChip(
                brand = RouterBrand.ASUS,
                selected = selectedBrand == RouterBrand.ASUS,
                onClick = { onBrandSelected(RouterBrand.ASUS) }
            )
            BrandChip(
                brand = RouterBrand.ZYXEL,
                selected = selectedBrand == RouterBrand.ZYXEL,
                onClick = { onBrandSelected(RouterBrand.ZYXEL) }
            )
            BrandChip(
                brand = RouterBrand.KEENETIC,
                selected = selectedBrand == RouterBrand.KEENETIC,
                onClick = { onBrandSelected(RouterBrand.KEENETIC) }
            )
            BrandChip(
                brand = RouterBrand.HUAWEI,
                selected = selectedBrand == RouterBrand.HUAWEI,
                onClick = { onBrandSelected(RouterBrand.HUAWEI) }
            )
            BrandChip(
                brand = RouterBrand.OTHER,
                selected = selectedBrand == RouterBrand.OTHER,
                onClick = { onBrandSelected(RouterBrand.OTHER) }
            )
        }
    }
}

@Composable
private fun BrandChip(
    brand: RouterBrand,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(brand.nameRes)) }
    )
}

@Composable
private fun GuideStepsList(
    steps: List<GuideStep>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(steps) { step ->
            GuideStepItem(step = step)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GuideStepItem(
    step: GuideStep
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.guide_step_label, step.order),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}