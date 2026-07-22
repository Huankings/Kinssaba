package org.BsXinQin.kinswathe;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import org.jetbrains.annotations.NotNull;

public class KinsWatheVoiceChatPlugin implements VoicechatPlugin {

    @Override public String getPluginId() {return KinsWathe.MOD_ID;}
    @Override public void initialize(@NotNull VoicechatApi api) {VoicechatPlugin.super.initialize(api);}
}
