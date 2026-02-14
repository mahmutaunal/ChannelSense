package com.mahmutalperenunal.channelsense.feature.guide.model

import androidx.annotation.StringRes
import com.mahmutalperenunal.channelsense.R

enum class RouterBrand(@StringRes val nameRes: Int) {
    TP_LINK(R.string.router_brand_tp_link),
    ASUS(R.string.router_brand_asus),
    ZYXEL(R.string.router_brand_zyxel),
    KEENETIC(R.string.router_brand_keenetic),
    HUAWEI(R.string.router_brand_huawei),
    OTHER(R.string.router_brand_other);
}