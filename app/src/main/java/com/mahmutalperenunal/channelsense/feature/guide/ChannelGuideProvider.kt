package com.mahmutalperenunal.channelsense.feature.guide

import android.content.Context
import com.mahmutalperenunal.channelsense.R
import com.mahmutalperenunal.channelsense.feature.guide.model.GuideStep
import com.mahmutalperenunal.channelsense.feature.guide.model.RouterBrand

object ChannelGuideProvider {

    fun getSteps(
        context: Context,
        brand: RouterBrand,
        channel: Int
    ): List<GuideStep> {
        val baseSteps = when (brand) {
            RouterBrand.TP_LINK -> tpLinkSteps(context, channel)
            RouterBrand.ASUS -> asusSteps(context, channel)
            RouterBrand.ZYXEL -> zyxelSteps(context, channel)
            RouterBrand.KEENETIC -> keeneticSteps(context, channel)
            RouterBrand.HUAWEI -> huaweiSteps(context, channel)
            RouterBrand.OTHER -> genericSteps(context, channel)
        }

        val noteStep = GuideStep(
            order = baseSteps.size + 1,
            text = context.getString(R.string.guide_note_remember_restart)
        )

        return baseSteps + noteStep
    }

    private fun tpLinkSteps(context: Context, channel: Int): List<GuideStep> = listOf(
        GuideStep(1, context.getString(R.string.guide_step_tplink_1)),
        GuideStep(2, context.getString(R.string.guide_step_tplink_2)),
        GuideStep(3, context.getString(R.string.guide_step_tplink_3)),
        GuideStep(4, context.getString(R.string.guide_step_tplink_4)),
        GuideStep(5, context.getString(R.string.guide_step_tplink_5, channel)),
        GuideStep(6, context.getString(R.string.guide_step_tplink_6))
    )

    private fun asusSteps(context: Context, channel: Int): List<GuideStep> = listOf(
        GuideStep(1, context.getString(R.string.guide_step_asus_1)),
        GuideStep(2, context.getString(R.string.guide_step_asus_2)),
        GuideStep(3, context.getString(R.string.guide_step_asus_3)),
        GuideStep(4, context.getString(R.string.guide_step_asus_4)),
        GuideStep(5, context.getString(R.string.guide_step_asus_5, channel)),
        GuideStep(6, context.getString(R.string.guide_step_asus_6))
    )

    private fun zyxelSteps(context: Context, channel: Int): List<GuideStep> = listOf(
        GuideStep(1, context.getString(R.string.guide_step_zyxel_1)),
        GuideStep(2, context.getString(R.string.guide_step_zyxel_2)),
        GuideStep(3, context.getString(R.string.guide_step_zyxel_3)),
        GuideStep(4, context.getString(R.string.guide_step_zyxel_4)),
        GuideStep(5, context.getString(R.string.guide_step_zyxel_5, channel)),
        GuideStep(6, context.getString(R.string.guide_step_zyxel_6))
    )

    private fun keeneticSteps(context: Context, channel: Int): List<GuideStep> = listOf(
        GuideStep(1, context.getString(R.string.guide_step_keenetic_1)),
        GuideStep(2, context.getString(R.string.guide_step_keenetic_2)),
        GuideStep(3, context.getString(R.string.guide_step_keenetic_3)),
        GuideStep(4, context.getString(R.string.guide_step_keenetic_4)),
        GuideStep(5, context.getString(R.string.guide_step_keenetic_5, channel)),
        GuideStep(6, context.getString(R.string.guide_step_keenetic_6))
    )

    private fun huaweiSteps(context: Context, channel: Int): List<GuideStep> = listOf(
        GuideStep(1, context.getString(R.string.guide_step_huawei_1)),
        GuideStep(2, context.getString(R.string.guide_step_huawei_2)),
        GuideStep(3, context.getString(R.string.guide_step_huawei_3)),
        GuideStep(4, context.getString(R.string.guide_step_huawei_4)),
        GuideStep(5, context.getString(R.string.guide_step_huawei_5, channel)),
        GuideStep(6, context.getString(R.string.guide_step_huawei_6))
    )

    private fun genericSteps(context: Context, channel: Int): List<GuideStep> = listOf(
        GuideStep(1, context.getString(R.string.guide_step_generic_1)),
        GuideStep(2, context.getString(R.string.guide_step_generic_2)),
        GuideStep(3, context.getString(R.string.guide_step_generic_3)),
        GuideStep(4, context.getString(R.string.guide_step_generic_4)),
        GuideStep(5, context.getString(R.string.guide_step_generic_5, channel)),
        GuideStep(6, context.getString(R.string.guide_step_generic_6))
    )
}